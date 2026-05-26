package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DjRepository(private val djDao: DjDao) {

    val allTracks: Flow<List<Track>> = djDao.getAllTracks()
    val allHistory: Flow<List<DjsHistory>> = djDao.getAllHistory()
    val allMilestones: Flow<List<GoalMilestone>> = djDao.getAllMilestones()

    fun getTracksByGenre(genre: String): Flow<List<Track>> {
        return djDao.getTracksByGenre(genre)
    }

    suspend fun insertTrack(track: Track) {
        djDao.insertTrack(track)
    }

    suspend fun updateTrack(track: Track) {
        djDao.updateTrack(track)
    }

    suspend fun deleteTrack(track: Track) {
        djDao.deleteTrack(track)
    }

    suspend fun insertHistory(history: DjsHistory) {
        djDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: DjsHistory) {
        djDao.deleteHistory(history)
    }

    suspend fun updateMilestone(milestone: GoalMilestone) {
        djDao.updateMilestone(milestone)
    }

    suspend fun insertMilestones(milestones: List<GoalMilestone>) {
        djDao.insertMilestones(milestones)
    }

    // --- PRE-POPULATION LOGIC ON STARTUP ---
    suspend fun checkAndPrePopulate() {
        val tracksList = djDao.getAllTracks().first()
        if (tracksList.isEmpty()) {
            val defaultTracks = listOf(
                Track(
                    title = "Subliminal Frequency",
                    artist = "Hi-Tech Shaman",
                    bpm = 165.0,
                    harmonicKey = "2A",
                    genre = "High-Tech",
                    energy = 9,
                    durationSeconds = 390,
                    tagString = "dark, trippy, atmospheric",
                    mood = "Trippy"
                ),
                Track(
                    title = "Cyber Overdrive",
                    artist = "Charlotte de Witte",
                    bpm = 132.0,
                    harmonicKey = "8A",
                    genre = "Techno",
                    energy = 7,
                    durationSeconds = 420,
                    tagString = "industrial, warehouse, heavy",
                    mood = "Industrial"
                ),
                Track(
                    title = "Astral Gateway",
                    artist = "Astrix",
                    bpm = 142.0,
                    harmonicKey = "11A",
                    genre = "Psytrance",
                    energy = 8,
                    durationSeconds = 480,
                    tagString = "cosmic, dynamic, melodic",
                    mood = "Cosmic"
                ),
                Track(
                    title = "Dark Matter Vortex",
                    artist = "Amelie Lens",
                    bpm = 135.0,
                    harmonicKey = "5A",
                    genre = "Techno",
                    energy = 8,
                    durationSeconds = 360,
                    tagString = "industrial, relentless, acid",
                    mood = "Industrial"
                ),
                Track(
                    title = "Hypnotic Dreamer",
                    artist = "Liquid Soul",
                    bpm = 138.0,
                    harmonicKey = "12A",
                    genre = "Psytrance",
                    energy = 6,
                    durationSeconds = 512,
                    tagString = "progressive, euphoric, floaty",
                    mood = "Euphoric"
                ),
                Track(
                    title = "Neurotransmitter",
                    artist = "Infected Mushroom",
                    bpm = 145.0,
                    harmonicKey = "9A",
                    genre = "Psytrance",
                    energy = 9,
                    durationSeconds = 404,
                    tagString = "psychedelic, acoustic-organic, heavy",
                    mood = "Acidic",
                    isFavorite = true
                ),
                Track(
                    title = "Infinite Velocity",
                    artist = "Speed Freak",
                    bpm = 178.0,
                    harmonicKey = "6A",
                    genre = "High-Tech",
                    energy = 10,
                    durationSeconds = 325,
                    tagString = "underground, ultra-fast, aggressive",
                    mood = "Aggressive"
                ),
                Track(
                    title = "Acid Catalyst",
                    artist = "Vini Vici",
                    bpm = 138.0,
                    harmonicKey = "3B",
                    genre = "Psytrance",
                    energy = 9,
                    durationSeconds = 450,
                    tagString = "tribal, chant, high-energy",
                    mood = "Acidic"
                ),
                Track(
                    title = "Subtle Shift",
                    artist = "Boris Brejcha",
                    bpm = 125.0,
                    harmonicKey = "1A",
                    genre = "Techno",
                    energy = 5,
                    durationSeconds = 540,
                    tagString = "high-tech minimal, melodic, sleek",
                    mood = "Melodic",
                    isFavorite = true
                ),
                Track(
                    title = "Acoustic Overload",
                    artist = "Mandragora",
                    bpm = 170.0,
                    harmonicKey = "10A",
                    genre = "High-Tech",
                    energy = 9,
                    durationSeconds = 310,
                    tagString = "bouncy, high-speed, synthetic",
                    mood = "Trippy"
                )
            )
            djDao.insertTracks(defaultTracks)
        }

        val historyList = djDao.getAllHistory().first()
        if (historyList.isEmpty()) {
            val defaultHistory = listOf(
                DjsHistory(
                    setTitle = "Infected Hive - Festival Stream",
                    durationSeconds = 3600,
                    setType = "Club/Festival Sets",
                    performanceSetup = "Hybrid Set",
                    totalTracksPlayed = 15,
                    averageBpm = 145.0,
                    energyRating = 5,
                    description = "Peak time hybrid Psytrance/High-Tech mix played live using MIDI mapping and virtual drum sequencing."
                ),
                DjsHistory(
                    setTitle = "Radio Pulsar EP.34 - Acid Special",
                    durationSeconds = 1800,
                    setType = "Radio Sets",
                    performanceSetup = "Live Set",
                    totalTracksPlayed = 8,
                    averageBpm = 135.0,
                    energyRating = 4,
                    description = "Sleek industrial and acid techno broadcast. Structured with custom voicings."
                ),
                DjsHistory(
                    setTitle = "Warehouse Club Re-open Warmup",
                    durationSeconds = 7200,
                    setType = "Club/Festival Sets",
                    performanceSetup = "Turntablism / Scratch Set",
                    totalTracksPlayed = 24,
                    averageBpm = 128.5,
                    energyRating = 3,
                    description = "Extended minimal techno and psy-ambient set. Vinyl scratch highlights during transitions."
                )
            )
            for (history in defaultHistory) {
                djDao.insertHistory(history)
            }
        }

        val milestonesList = djDao.getAllMilestones().first()
        if (milestonesList.isEmpty()) {
            val defaultMilestones = listOf(
                GoalMilestone(
                    title = "Techno Beatmatching Drill",
                    description = "Mix 5 Techno tracks manually without visual assist.",
                    targetValue = 5.0f,
                    currentValue = 5.0f,
                    isAchieved = true,
                    category = "BPM Master",
                    rewardIcon = "BPM"
                ),
                GoalMilestone(
                    title = "High-Tech Velocity Run",
                    description = "Maintain a high-speed High-Tech set above 165 BPM.",
                    targetValue = 60.0f,
                    currentValue = 35.0f,
                    isAchieved = false,
                    category = "Hours Played",
                    rewardIcon = "SPEED"
                ),
                GoalMilestone(
                    title = "Psytrance Harmonic Fusion",
                    description = "Achieve perfect Camelot key transitions in recorded sets.",
                    targetValue = 10.0f,
                    currentValue = 6.0f,
                    isAchieved = false,
                    category = "Beatmatch Master",
                    rewardIcon = "HARMONIC"
                ),
                GoalMilestone(
                    title = "Timecode DVS Calibration",
                    description = "Successfully connect and calibrate external physical turntables.",
                    targetValue = 1.0f,
                    currentValue = 1.0f,
                    isAchieved = true,
                    category = "Hardware Setup",
                    rewardIcon = "DVS"
                ),
                GoalMilestone(
                    title = "Live Synthesizer Mapping",
                    description = "Map and trigger synth loops live via physical MIDI controllers.",
                    targetValue = 3.0f,
                    currentValue = 1.0f,
                    isAchieved = false,
                    category = "Scratch Exercises",
                    rewardIcon = "MIDI"
                )
            )
            djDao.insertMilestones(defaultMilestones)
        }
    }
}
