package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.DjApplication
import com.example.data.DjRepository
import com.example.data.DjsHistory
import com.example.data.GoalMilestone
import com.example.data.Track
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.UUID

data class DeckState(
    val deckId: String, // "Deck A", "Deck B"
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val bpm: Double = 130.0,
    val targetBpm: Double = 130.0,
    val pitchShiftPercent: Float = 0.0f, // -10% to +10%
    val harmonicKeyShift: Int = 0, // Semitones offset (-5 to +5)
    val lowEq: Float = 0.0f, // -12dB to +6dB
    val midEq: Float = 0.0f, // -12dB to +6dB
    val highEq: Float = 0.0f, // -12dB to +6dB
    val gain: Float = 1.0f, // 0.0 to 1.5
    val progress: Float = 0.0f, // 0.0 to 1.0 (playhead)
    val vinylRotationAngle: Float = 0.0f,
    val isScratching: Boolean = false,
    val isDvsCalibrated: Boolean = false,
    val waveformPoints: List<Float> = emptyList(), // Pre-computed mock waveform
    val beatGridOffset: Float = 0.0f
) {
    val currentBpm: Double
        get() = bpm * (1.0 + pitchShiftPercent / 100.0)

    val currentHarmonicKey: String
        get() {
            if (track == null) return "-"
            val base = track.harmonicKey
            if (harmonicKeyShift == 0) return base
            // Parse Camelot key: e.g. "8A" -> number=8, suffix='A'
            val regex = "(\\d+)([AB])".toRegex()
            val match = regex.matchEntire(base) ?: return base
            val number = match.groupValues[1].toInt()
            val suffix = match.groupValues[2]
            // Scale shift: add shift and wrap between 1 and 12
            var shiftedNum = (number + harmonicKeyShift) % 12
            if (shiftedNum <= 0) shiftedNum += 12
            return "$shiftedNum$suffix"
        }
}

class DjViewModel(
    application: Application,
    private val repository: DjRepository
) : AndroidViewModel(application) {

    // --- DECKS STATE ---
    private val _deckA = MutableStateFlow(DeckState("Deck A"))
    val deckA: StateFlow<DeckState> = _deckA.asStateFlow()

    private val _deckB = MutableStateFlow(DeckState("Deck B"))
    val deckB: StateFlow<DeckState> = _deckB.asStateFlow()

    // --- MASTER CONTROLS ---
    private val _crossfader = MutableStateFlow(0.0f) // -1.0 to +1.0
    val crossfader: StateFlow<Float> = _crossfader.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordDurationSeconds = MutableStateFlow(0)
    val recordDurationSeconds: StateFlow<Int> = _recordDurationSeconds.asStateFlow()

    // --- EFFECTS MODIFIER SYSTEM ---
    private val _activeEffectsType = MutableStateFlow("Delay") // "Delay", "Reverb", "Flanger", "LowPass Filter"
    val activeEffectsType: StateFlow<String> = _activeEffectsType.asStateFlow()

    private val _effectsXY = MutableStateFlow(Pair(0.5f, 0.5f)) // X: frequency/rate, Y: feedback/depth
    val effectsXY: StateFlow<Pair<Float, Float>> = _effectsXY.asStateFlow()

    private val _effectsWetDry = MutableStateFlow(0.3f)
    val effectsWetDry: StateFlow<Float> = _effectsWetDry.asStateFlow()

    // --- HARDWARE / SETTINGS PREFERENCES ---
    private val _djSetType = MutableStateFlow("Club/Festival Sets")
    val djSetType: StateFlow<String> = _djSetType.asStateFlow()

    private val _performanceSetup = MutableStateFlow("Live Set")
    val performanceSetup: StateFlow<String> = _performanceSetup.asStateFlow()

    private val _isDvsEnabled = MutableStateFlow(false)
    val isDvsEnabled: StateFlow<Boolean> = _isDvsEnabled.asStateFlow()

    private val _dvsTargetDeck = MutableStateFlow("Deck A")
    val dvsTargetDeck: StateFlow<String> = _dvsTargetDeck.asStateFlow()

    private val _cloudSyncEnabled = MutableStateFlow(true)
    val cloudSyncEnabled: StateFlow<Boolean> = _cloudSyncEnabled.asStateFlow()

    private val _cloudSyncProgress = MutableStateFlow("Synced Today at 15:44 UTC")
    val cloudSyncProgress: StateFlow<String> = _cloudSyncProgress.asStateFlow()

    private val _offlineModeEnabled = MutableStateFlow(true)
    val offlineModeEnabled: StateFlow<Boolean> = _offlineModeEnabled.asStateFlow()

    // --- TEXT SEARCH & FILTERS ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _genreFilter = MutableStateFlow("All")
    val genreFilter: StateFlow<String> = _genreFilter.asStateFlow()

    // --- AUTOMATED MOOD PLAYLIST SYSTEM ---
    private val _currentMoodTheme = MutableStateFlow<String?>(null)
    val currentMoodTheme: StateFlow<String?> = _currentMoodTheme.asStateFlow()

    // --- REALTIME ALERTS AND MILESTONES GOAL PROGRESS FEEDBACK ---
    private val _goalAlertMessage = MutableSharedFlow<String>()
    val goalAlertMessage = _goalAlertMessage.asSharedFlow()

    // --- REPOSITORY DATABASE BINDINGS ---
    val tracksCollection: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val djsHistoryList: StateFlow<List<DjsHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalMilestones: StateFlow<List<GoalMilestone>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DERIVED STATES ---
    val activePlaylist: StateFlow<List<Track>> = combine(
        tracksCollection,
        searchQuery,
        genreFilter,
        currentMoodTheme
    ) { collection, query, genre, mood ->
        var list = collection
        if (query.isNotEmpty()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
        }
        if (genre != "All") {
            list = list.filter { it.genre.equals(genre, ignoreCase = true) }
        }
        if (mood != null) {
            // Apply automated smart playlist logic based on mood
            list = list.filter {
                when (mood) {
                    "Industrial Strobe" -> it.genre == "Techno" && it.energy >= 7
                    "Cosmic Portal" -> it.genre == "Psytrance" && it.mood == "Cosmic"
                    "Hyper-Drive" -> it.genre == "High-Tech" && it.bpm >= 165
                    "Deep Melodic Acid" -> it.tagString.contains("acid") || it.genre == "Techno"
                    else -> true
                }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Playback progress ticker loop
        viewModelScope.launch {
            while (isActive) {
                delay(50) // Balanced 20Hz tick rate for optimal responsiveness without thread choke
                updatePlaybackTick(50)
            }
        }

        // Recording duration ticker
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_isRecording.value) {
                    _recordDurationSeconds.update { it + 1 }
                }
            }
        }
    }

    // --- CONTROLLER EVENTS (KEYBOARD/SHORTCUT SIMULATION) ---
    fun handleKeyboardShortcut(char: Char) {
        when (char.uppercaseChar()) {
            'Q' -> togglePlayback("Deck A")
            'W' -> togglePlayback("Deck B")
            'A' -> triggerSyncBpm("Deck A")
            'S' -> triggerSyncBpm("Deck B")
            'D' -> adjustPitch("Deck A", 0.5f)
            'F' -> adjustPitch("Deck A", -0.5f)
            'J' -> adjustPitch("Deck B", 0.5f)
            'K' -> adjustPitch("Deck B", -0.5f)
            'Z' -> _crossfader.update { (it - 0.1f).coerceIn(-1f, 1f) }
            'X' -> _crossfader.update { (it + 0.1f).coerceIn(-1f, 1f) }
            'C' -> _crossfader.update { 0.0f }
        }
    }

    // --- DECK ACTION HANDLERS ---
    fun loadTrackIntoDeck(deckId: String, track: Track) {
        val randomWaveform = List(150) { (20..95).random().toFloat() / 100f }
        if (deckId == "Deck A") {
            _deckA.update {
                it.copy(
                    track = track,
                    bpm = track.bpm,
                    targetBpm = track.bpm,
                    progress = 0.0f,
                    pitchShiftPercent = 0.0f,
                    harmonicKeyShift = 0,
                    waveformPoints = randomWaveform,
                    isPlaying = false,
                    isDvsCalibrated = _isDvsEnabled.value && _dvsTargetDeck.value == "Deck A"
                )
            }
        } else {
            _deckB.update {
                it.copy(
                    track = track,
                    bpm = track.bpm,
                    targetBpm = track.bpm,
                    progress = 0.0f,
                    pitchShiftPercent = 0.0f,
                    harmonicKeyShift = 0,
                    waveformPoints = randomWaveform,
                    isPlaying = false,
                    isDvsCalibrated = _isDvsEnabled.value && _dvsTargetDeck.value == "Deck B"
                )
            }
        }
    }

    fun togglePlayback(deckId: String) {
        if (deckId == "Deck A") {
            val trackExist = _deckA.value.track != null
            if (trackExist) {
                _deckA.update { it.copy(isPlaying = !it.isPlaying) }
                incrementMilestoneProgress("Hours Played", 0.05f)
            }
        } else {
            val trackExist = _deckB.value.track != null
            if (trackExist) {
                _deckB.update { it.copy(isPlaying = !it.isPlaying) }
                incrementMilestoneProgress("Hours Played", 0.05f)
            }
        }
    }

    fun adjustPitch(deckId: String, deltaPercent: Float) {
        if (deckId == "Deck A") {
            _deckA.update { it.copy(pitchShiftPercent = (it.pitchShiftPercent + deltaPercent).coerceIn(-10.0f, 10.0f)) }
        } else {
            _deckB.update { it.copy(pitchShiftPercent = (it.pitchShiftPercent + deltaPercent).coerceIn(-10.0f, 10.0f)) }
        }
    }

    fun adjustKeyShift(deckId: String, steps: Int) {
        if (deckId == "Deck A") {
            _deckA.update { it.copy(harmonicKeyShift = (it.harmonicKeyShift + steps).coerceIn(-5, 5)) }
        } else {
            _deckB.update { it.copy(harmonicKeyShift = (it.harmonicKeyShift + steps).coerceIn(-5, 5)) }
        }
    }

    fun adjustEq(deckId: String, eqBand: String, value: Float) {
        // Value range: -12.0f to +6.0f
        val clamped = value.coerceIn(-12.0f, 6.0f)
        if (deckId == "Deck A") {
            _deckA.update {
                when (eqBand) {
                    "LOW" -> it.copy(lowEq = clamped)
                    "MID" -> it.copy(midEq = clamped)
                    "HIGH" -> it.copy(highEq = clamped)
                    else -> it
                }
            }
        } else {
            _deckB.update {
                when (eqBand) {
                    "LOW" -> it.copy(lowEq = clamped)
                    "MID" -> it.copy(midEq = clamped)
                    "HIGH" -> it.copy(highEq = clamped)
                    else -> it
                }
            }
        }
    }

    fun adjustGain(deckId: String, value: Float) {
        val clamped = value.coerceIn(0.0f, 1.5f)
        if (deckId == "Deck A") {
            _deckA.update { it.copy(gain = clamped) }
        } else {
            _deckB.update { it.copy(gain = clamped) }
        }
    }

    fun updateCrossfader(value: Float) {
        _crossfader.update { value.coerceIn(-1.0f, 1.0f) }
    }

    // --- TIMED PLAYBACK ADVANCEMENT ---
    private fun updatePlaybackTick(millisPassed: Int) {
        updateSingleDeckPlaybackTick(_deckA, millisPassed)
        updateSingleDeckPlaybackTick(_deckB, millisPassed)
    }

    private fun updateSingleDeckPlaybackTick(deckStateFlow: MutableStateFlow<DeckState>, millisPassed: Int) {
        val state = deckStateFlow.value
        if (state.track == null || !state.isPlaying) return

        if (state.isScratching) return // Scratch controls overriding continuous playhead

        val totalSeconds = state.track.durationSeconds.toDouble()
        if (totalSeconds <= 0) return

        // 1.0 is full track. Progression increment = (seconds / totalSeconds)
        val rateMultiplier = 1.0 + (state.pitchShiftPercent / 100.0)
        val deltaSeconds = (millisPassed / 1000.0) * rateMultiplier
        var newProgress = (state.progress + (deltaSeconds / totalSeconds)).toFloat()

        var accumulatedRotation = state.vinylRotationAngle + (360f * (state.currentBpm.toFloat() / 60f) * (millisPassed / 1000f))
        accumulatedRotation %= 360f

        if (newProgress >= 1.0f) {
            newProgress = 0.0f
            deckStateFlow.update { it.copy(progress = 0.0f, isPlaying = false) }
        } else {
            deckStateFlow.update {
                it.copy(
                    progress = newProgress,
                    vinylRotationAngle = accumulatedRotation
                )
            }
        }
    }

    // --- VINYL EMULATION DRAG/SCRATCH ---
    fun onPlatterScratchAction(deckId: String, dragOffsetDegrees: Float, active: Boolean) {
        if (deckId == "Deck A") {
            val state = _deckA.value
            val currentAngle = (state.vinylRotationAngle + dragOffsetDegrees) % 360f
            var computedProgress = state.progress
            if (active && state.track != null) {
                // Manipulate playhead directly based on drag rotation delta representing pitch/timecode seek
                val secondsDelta = (dragOffsetDegrees / 360.0) * 1.5 // 1 rotation = 1.5 seconds seeking
                computedProgress = (computedProgress + (secondsDelta / state.track.durationSeconds)).toFloat().coerceIn(0.0f, 1.0f)
            }
            _deckA.update {
                it.copy(
                    vinylRotationAngle = currentAngle,
                    isScratching = active,
                    progress = computedProgress
                )
            }
            if (active) {
                incrementMilestoneProgress("Scratch Exercises", 0.02f)
            }
        } else {
            val state = _deckB.value
            val currentAngle = (state.vinylRotationAngle + dragOffsetDegrees) % 360f
            var computedProgress = state.progress
            if (active && state.track != null) {
                val secondsDelta = (dragOffsetDegrees / 360.0) * 1.5
                computedProgress = (computedProgress + (secondsDelta / state.track.durationSeconds)).toFloat().coerceIn(0.0f, 1.0f)
            }
            _deckB.update {
                it.copy(
                    vinylRotationAngle = currentAngle,
                    isScratching = active,
                    progress = computedProgress
                )
            }
            if (active) {
                incrementMilestoneProgress("Scratch Exercises", 0.02f)
            }
        }
    }

    // --- BPM MATCH & HARMONIC SYNC AUTOMATED ENGINE ---
    fun triggerSyncBpm(targetDeckId: String) {
        if (targetDeckId == "Deck A") {
            val src = _deckB.value
            val dst = _deckA.value
            if (src.track != null && dst.track != null) {
                // Find percentage shift required to match target bpm
                val requiredBpm = src.currentBpm
                val requiredShift = ((requiredBpm / dst.bpm) - 1.0) * 100.0
                _deckA.update {
                    it.copy(
                        pitchShiftPercent = requiredShift.toFloat().coerceIn(-10f, 10f),
                        progress = src.progress // Phase sync alignment
                    )
                }
                incrementMilestoneProgress("BPM Master", 1.0f)
            }
        } else {
            val src = _deckA.value
            val dst = _deckB.value
            if (src.track != null && dst.track != null) {
                val requiredBpm = src.currentBpm
                val requiredShift = ((requiredBpm / dst.bpm) - 1.0) * 100.0
                _deckB.update {
                    it.copy(
                        pitchShiftPercent = requiredShift.toFloat().coerceIn(-10f, 10f),
                        progress = src.progress
                    )
                }
                incrementMilestoneProgress("BPM Master", 1.0f)
            }
        }
    }

    fun triggerKeySync(targetDeckId: String) {
        if (targetDeckId == "Deck A") {
            val src = _deckB.value
            val dst = _deckA.value
            if (src.track != null && dst.track != null) {
                // Find matching harmonic key
                val srcKeyStr = src.currentHarmonicKey
                val dstBaseKeyStr = dst.track.harmonicKey

                // Simple semitone key sync evaluation
                val regex = "(\\d+)([AB])".toRegex()
                val srcMatch = regex.matchEntire(srcKeyStr)
                val dstMatch = regex.matchEntire(dstBaseKeyStr)

                if (srcMatch != null && dstMatch != null) {
                    val srcNum = srcMatch.groupValues[1].toInt()
                    val dstNum = dstMatch.groupValues[1].toInt()
                    val srcSuffix = srcMatch.groupValues[2]
                    val dstSuffix = dstMatch.groupValues[2]

                    var diff = srcNum - dstNum
                    if (diff > 6) diff -= 12
                    if (diff < -6) diff += 12

                    // If mode matches, just shift diff. If not, match mode first then shift
                    val finalShift = if (srcSuffix == dstSuffix) diff else diff + 1
                    _deckA.update { it.copy(harmonicKeyShift = finalShift.coerceIn(-5, 5)) }
                    incrementMilestoneProgress("Beatmatch Master", 1.0f)
                }
            }
        } else {
            val src = _deckA.value
            val dst = _deckB.value
            if (src.track != null && dst.track != null) {
                val srcKeyStr = src.currentHarmonicKey
                val dstBaseKeyStr = dst.track.harmonicKey

                val regex = "(\\d+)([AB])".toRegex()
                val srcMatch = regex.matchEntire(srcKeyStr)
                val dstMatch = regex.matchEntire(dstBaseKeyStr)

                if (srcMatch != null && dstMatch != null) {
                    val srcNum = srcMatch.groupValues[1].toInt()
                    val dstNum = dstMatch.groupValues[1].toInt()
                    val srcSuffix = srcMatch.groupValues[2]
                    val dstSuffix = dstMatch.groupValues[2]

                    var diff = srcNum - dstNum
                    if (diff > 6) diff -= 12
                    if (diff < -6) diff += 12

                    val finalShift = if (srcSuffix == dstSuffix) diff else diff + 1
                    _deckB.update { it.copy(harmonicKeyShift = finalShift.coerceIn(-5, 5)) }
                    incrementMilestoneProgress("Beatmatch Master", 1.0f)
                }
            }
        }
    }

    // --- EFFECTS HANDLERS ---
    fun updateActiveEffectType(type: String) {
        _activeEffectsType.update { type }
    }

    fun updateEffectsXY(x: Float, y: Float) {
        _effectsXY.update { Pair(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f)) }
    }

    fun updateEffectsWetDry(value: Float) {
        _effectsWetDry.update { value.coerceIn(0f, 1f) }
    }

    // --- PREFERENCES / FORMAT SETTINGS ---
    fun updateSetType(type: String) {
        _djSetType.update { type }
    }

    fun updatePerformanceSetup(setup: String) {
        _performanceSetup.update { setup }
    }

    fun toggleDvsEnabled() {
        val targetState = !_isDvsEnabled.value
        _isDvsEnabled.update { targetState }
        _deckA.update { it.copy(isDvsCalibrated = targetState && _dvsTargetDeck.value == "Deck A") }
        _deckB.update { it.copy(isDvsCalibrated = targetState && _dvsTargetDeck.value == "Deck B") }
        if (targetState) {
            incrementMilestoneProgress("Hardware Setup", 1.0f)
        }
    }

    fun updateDvsTargetDeck(deckId: String) {
        _dvsTargetDeck.update { deckId }
        _deckA.update { it.copy(isDvsCalibrated = _isDvsEnabled.value && deckId == "Deck A") }
        _deckB.update { it.copy(isDvsCalibrated = _isDvsEnabled.value && deckId == "Deck B") }
    }

    fun toggleCloudSync() {
        _cloudSyncEnabled.update { !_cloudSyncEnabled.value }
    }

    fun triggerCloudSyncNow() {
        viewModelScope.launch {
            _cloudSyncProgress.update { "Syncing collection..." }
            delay(1500)
            _cloudSyncProgress.update { "Synced Database: ${tracksCollection.value.size} tracks, ${djsHistoryList.value.size} sets backed up." }
        }
    }

    fun toggleOfflineMode() {
        _offlineModeEnabled.update { !_offlineModeEnabled.value }
    }

    // --- TEXT SEARCH BINDINGS ---
    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun updateGenreFilter(filter: String) {
        _genreFilter.update { filter }
        _currentMoodTheme.update { null } // Reset mood filter on regular genre selection
    }

    // --- SMART MOOD-BASED AUTO PLAYLIST GENERATOR ---
    fun selectMoodPlaylist(moodName: String?) {
        _currentMoodTheme.update { moodName }
        if (moodName != null) {
            _genreFilter.update { "All" } // Priority to mood filtering
        }
    }

    // --- DATABASE ACTIONS (TRACK ORGANIZER METADATA TAGGER) ---
    fun createAndInsertTrack(title: String, artist: String, bpm: Double, key: String, genre: String, mood: String, tags: String) {
        viewModelScope.launch {
            val newTrack = Track(
                title = title.ifEmpty { "New Techno Dub" },
                artist = artist.ifEmpty { "Hardware Virtuoso" },
                bpm = if (bpm > 0) bpm else 135.0,
                harmonicKey = key.ifEmpty { "8A" },
                genre = genre,
                energy = (6..10).random(),
                durationSeconds = (240..600).random(),
                tagString = tags,
                mood = mood
            )
            repository.insertTrack(newTrack)
            incrementMilestoneProgress("Sets Recorded", 0.5f)
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    fun toggleTrackFavorite(track: Track) {
        viewModelScope.launch {
            repository.updateTrack(track.copy(isFavorite = !track.isFavorite))
        }
    }

    // --- LIVE MIX RECORDING SAVING & AUDIO EXPORT ---
    fun toggleRecording() {
        val wasRecording = _isRecording.value
        _isRecording.update { !it }
        if (wasRecording) {
            // Save recording to History Database Log
            val dur = _recordDurationSeconds.value.toLong()
            if (dur > 0) {
                saveRecordedSetToHistory(dur)
            }
            _recordDurationSeconds.value = 0
        }
    }

    private fun saveRecordedSetToHistory(duration: Long) {
        viewModelScope.launch {
            val currentTracksPlayedCount = if (_deckA.value.track != null && _deckB.value.track != null) 2 else 1
            val computedAvgBpm = ((_deckA.value.currentBpm + _deckB.value.currentBpm) / 2.0)
            val randomTitle = "Live set - " + UUID.randomUUID().toString().substring(0, 5).uppercase()

            val newHistory = DjsHistory(
                setTitle = randomTitle,
                durationSeconds = duration,
                setType = _djSetType.value,
                performanceSetup = _performanceSetup.value,
                totalTracksPlayed = currentTracksPlayedCount + (duration / 200).toInt().coerceAtLeast(1),
                averageBpm = computedAvgBpm,
                energyRating = (3..5).random(),
                description = "Self-contained continuous recording session with customized EQs and active DSP effect: ${_activeEffectsType.value}."
            )
            repository.insertHistory(newHistory)
            incrementMilestoneProgress("Sets Recorded", 1.0f)
        }
    }

    fun deleteHistoryRecord(history: DjsHistory) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    // --- INCREMENT MILESTONE MOTIVATIONAL ALERTS ---
    private fun incrementMilestoneProgress(category: String, amt: Float) {
        viewModelScope.launch {
            val currentList = repository.allMilestones.first()
            val milestone = currentList.find { it.category == category && !it.isAchieved } ?: return@launch

            val nextVal = (milestone.currentValue + amt).coerceAtMost(milestone.targetValue)
            val isNewlyAchieved = nextVal >= milestone.targetValue

            val updated = milestone.copy(
                currentValue = nextVal,
                isAchieved = isNewlyAchieved
            )
            repository.updateMilestone(updated)

            if (isNewlyAchieved) {
                _goalAlertMessage.emit("Goal Unlocked! 🏆 ${milestone.title}: ${milestone.description}")
            }
        }
    }
}

class DjViewModelFactory(
    private val application: Application,
    private val repository: DjRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DjViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DjViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
