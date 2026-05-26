package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Track
import com.example.ui.DeckState
import com.example.ui.DjViewModel
import com.example.ui.components.VinylTurntable
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.XYPadEffects
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen
import com.example.ui.theme.BurningOrange
import com.example.ui.theme.TextMuted

@Composable
fun DecksScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val deckA by viewModel.deckA.collectAsState()
    val deckB by viewModel.deckB.collectAsState()
    val crossfader by viewModel.crossfader.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recDuration by viewModel.recordDurationSeconds.collectAsState()
    val activeEffect by viewModel.activeEffectsType.collectAsState()
    val effectsXY by viewModel.effectsXY.collectAsState()

    var showLoadQuickDialog by remember { mutableStateOf<String?>(null) } // "Deck A" or "Deck B"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. HEADER ROW: recording control and master status Indicators
        item {
            RecordingHeaderRow(
                isRecording = isRecording,
                recDuration = recDuration,
                djSetType = viewModel.djSetType.collectAsState().value,
                performanceSetup = viewModel.performanceSetup.collectAsState().value,
                onRecToggle = { viewModel.toggleRecording() }
            )
        }

        // 2. MAIN CONSOLE BOX: Adaptive layout containing Deck A, Mixer utilities, Deck B
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row for the dual decks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // DECK A COLUMN (Cyan theme)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DeckHeader(
                                deckId = "Deck A",
                                track = deckA.track,
                                bpm = deckA.currentBpm,
                                key = deckA.currentHarmonicKey,
                                isDvsCalibrated = deckA.isDvsCalibrated,
                                accentColor = CyberCyan,
                                onLoadRequest = { showLoadQuickDialog = "Deck A" }
                            )

                            // Waveform component
                            WaveformVisualizer(
                                progress = deckA.progress,
                                waveformPoints = deckA.waveformPoints,
                                isDeckA = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp)
                            )

                            // Platter scratcher & quick tactile controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pitch adjustment sliders/buttons
                                PitchControlColumn(
                                    deckState = deckA,
                                    accentColor = CyberCyan,
                                    onPitchChange = { delta -> viewModel.adjustPitch("Deck A", delta) }
                                )

                                VinylTurntable(
                                    rotationAngle = deckA.vinylRotationAngle,
                                    isPlaying = deckA.isPlaying,
                                    trackTitle = deckA.track?.title,
                                    isDeckA = true,
                                    onScratch = { delta, active -> viewModel.onPlatterScratchAction("Deck A", delta, active) }
                                )

                                // Key adjustment steps
                                KeyControlColumn(
                                    deckId = "Deck A",
                                    shift = deckA.harmonicKeyShift,
                                    accentColor = CyberCyan,
                                    onKeySync = { viewModel.triggerKeySync("Deck A") },
                                    onKeyDelta = { steps -> viewModel.adjustKeyShift("Deck A", steps) }
                                )
                            }

                            // Eq fader bands Low Mid High & Play/Pause buttons
                            EqPlayControlsRow(
                                deckState = deckA,
                                accentColor = CyberCyan,
                                onEqUpdate = { band, valDb -> viewModel.adjustEq("Deck A", band, valDb) },
                                onGainUpdate = { gain -> viewModel.adjustGain("Deck A", gain) },
                                onPlayClick = { viewModel.togglePlayback("Deck A") },
                                onSyncClick = { viewModel.triggerSyncBpm("Deck A") }
                            )
                        }

                        // DECK B COLUMN (Pink theme)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DeckHeader(
                                deckId = "Deck B",
                                track = deckB.track,
                                bpm = deckB.currentBpm,
                                key = deckB.currentHarmonicKey,
                                isDvsCalibrated = deckB.isDvsCalibrated,
                                accentColor = PsyPink,
                                onLoadRequest = { showLoadQuickDialog = "Deck B" }
                            )

                            // Waveform component
                            WaveformVisualizer(
                                progress = deckB.progress,
                                waveformPoints = deckB.waveformPoints,
                                isDeckA = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp)
                            )

                            // Platter scratcher & quick tactile controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pitch adjustment sliders/buttons
                                PitchControlColumn(
                                    deckState = deckB,
                                    accentColor = PsyPink,
                                    onPitchChange = { delta -> viewModel.adjustPitch("Deck B", delta) }
                                )

                                VinylTurntable(
                                    rotationAngle = deckB.vinylRotationAngle,
                                    isPlaying = deckB.isPlaying,
                                    trackTitle = deckB.track?.title,
                                    isDeckA = false,
                                    onScratch = { delta, active -> viewModel.onPlatterScratchAction("Deck B", delta, active) }
                                )

                                // Key adjustment steps
                                KeyControlColumn(
                                    deckId = "Deck B",
                                    shift = deckB.harmonicKeyShift,
                                    accentColor = PsyPink,
                                    onKeySync = { viewModel.triggerKeySync("Deck B") },
                                    onKeyDelta = { steps -> viewModel.adjustKeyShift("Deck B", steps) }
                                )
                            }

                            // Eq fader bands Low Mid High & Play/Pause buttons
                            EqPlayControlsRow(
                                deckState = deckB,
                                accentColor = PsyPink,
                                onEqUpdate = { band, valDb -> viewModel.adjustEq("Deck B", band, valDb) },
                                onGainUpdate = { gain -> viewModel.adjustGain("Deck B", gain) },
                                onPlayClick = { viewModel.togglePlayback("Deck B") },
                                onSyncClick = { viewModel.triggerSyncBpm("Deck B") }
                            )
                        }
                    }

                    // MASTER CROSSFADER CENTERPIECE BAR
                    MasterCrossfaderContainer(
                        crossfaderVal = crossfader,
                        onFaderChange = { viewModel.updateCrossfader(it) }
                    )
                }
            }
        }

        // 3. DSP FX XY GRAPH CONTROL CENTER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Tune, contentDescription = "FX Generator", tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "REAL-TIME FX MULTI-TOUCH ENGINE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Effects selection buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Delay", "Reverb", "Flanger", "Filter").forEach { fxName ->
                                val selected = activeEffect == fxName
                                Button(
                                    onClick = { viewModel.updateActiveEffectType(fxName) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) CyberCyan else SurfaceDark,
                                        contentColor = if (selected) Color.Black else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("fx_button_$fxName")
                                ) {
                                    Text(text = fxName.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Interactive Drag XY pad
                        XYPadEffects(
                            activeEffect = activeEffect,
                            xVal = effectsXY.first,
                            yVal = effectsXY.second,
                            onValueChange = { x, y -> viewModel.updateEffectsXY(x, y) },
                            modifier = Modifier.weight(1.5f)
                        )

                        // Auxiliary effect level slider
                        Column(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(130.dp)
                                .background(BackgroundDark, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "WET/DRY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            val wetDryVal by viewModel.effectsWetDry.collectAsState()
                            Slider(
                                value = wetDryVal,
                                onValueChange = { viewModel.updateEffectsWetDry(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("wet_dry_slider")
                            )
                            Text(
                                text = "${(wetDryVal * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Overlay track selection Dialog
    if (showLoadQuickDialog != null) {
        val targetDeck = showLoadQuickDialog!!
        val tracks by viewModel.tracksCollection.collectAsState()

        AlertDialog(
            onDismissRequest = { showLoadQuickDialog = null },
            title = { Text(text = "Load track into $targetDeck", color = Color.White, fontSize = 16.sp) },
            containerColor = SurfaceDark,
            confirmButton = {
                TextButton(onClick = { showLoadQuickDialog = null }) {
                    Text("Cancel", color = CyberCyan)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tracks) { track ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("load_track_${track.title.replace(" ", "_").lowercase()}")
                                .clickable {
                                    viewModel.loadTrackIntoDeck(targetDeck, track)
                                    showLoadQuickDialog = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = track.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = track.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${track.bpm.toInt()} BPM",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = track.harmonicKey,
                                        color = ToxicGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

// --- SUB-ELEMENT DRAWS ---

@Composable
fun RecordingHeaderRow(
    isRecording: Boolean,
    recDuration: Int,
    djSetType: String,
    performanceSetup: String,
    onRecToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141620), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Glowing pulsing record indicator
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) {
                            Color.Red.copy(alpha = alpha)
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        }
                    )
            )

            Text(
                text = if (isRecording) {
                    val minutes = recDuration / 60
                    val seconds = recDuration % 60
                    "REC: ${String.format("%02d:%02d", minutes, seconds)}"
                } else {
                    "STBY (MIX LOGGER READY)"
                },
                color = if (isRecording) Color.Red else Color.LightGray,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Selected mode chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            BadgeValueItem("FORMAT", djSetType.replace(" Sets", "").uppercase())
            BadgeValueItem("SYSTEM", performanceSetup.replace(" Set", "").uppercase())

            IconButton(
                onClick = onRecToggle,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red else Color.White.copy(alpha = 0.1f))
                    .testTag("record_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Circle else Icons.Default.FiberManualRecord,
                    contentDescription = "Mix recorder trigger",
                    tint = if (isRecording) Color.White else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BadgeValueItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DeckHeader(
    deckId: String,
    track: Track?,
    bpm: Double,
    key: String,
    isDvsCalibrated: Boolean,
    accentColor: Color,
    onLoadRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deckId,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = track?.title ?: "TAP LOAD TO ASSIGN TRACK",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track?.artist ?: "NO SOURCE SELECTED",
                color = Color.LightGray.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DVS Hardware calibration small status icon
            if (isDvsCalibrated) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "DVS External timecode active",
                    tint = ToxicGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Numeric BPM
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f", bpm),
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "BPM",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Numeric Harmonic Camelot key standard
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = key,
                    fontWeight = FontWeight.Black,
                    color = ToxicGreen,
                    fontSize = 14.sp
                )
                Text(
                    text = "KEY",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onLoadRequest,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("load_button_" + deckId.replace(" ", "_").lowercase())
            ) {
                Text(text = "EJECT", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PitchControlColumn(
    deckState: DeckState,
    accentColor: Color,
    onPitchChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { onPitchChange(0.2f) },
            modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Speed pitch up", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        Text(
            text = "PITCH",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f)
        )
        Text(
            text = String.format("%+.1f%%", deckState.pitchShiftPercent),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
        )

        IconButton(
            onClick = { onPitchChange(-0.2f) },
            modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Speed pitch down", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun KeyControlColumn(
    deckId: String,
    shift: Int,
    accentColor: Color,
    onKeySync: () -> Unit,
    onKeyDelta: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { onKeyDelta(1) },
            modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Harmonic key semitone up", tint = Color.White, modifier = Modifier.size(14.dp))
        }

        Button(
            onClick = onKeySync,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14161F)),
            contentPadding = PaddingValues(2.dp),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .height(20.dp)
                .width(42.dp)
                .testTag("key_sync_" + deckId.replace(" ", "_").lowercase())
        ) {
            Text("K-SYNC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ToxicGreen)
        }

        Text(
            text = String.format("%+d ST", shift),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = ToxicGreen
        )

        IconButton(
            onClick = { onKeyDelta(-1) },
            modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Harmonic key semitone down", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun EqPlayControlsRow(
    deckState: DeckState,
    accentColor: Color,
    onEqUpdate: (String, Float) -> Unit,
    onGainUpdate: (Float) -> Unit,
    onPlayClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channels Gains and EQs
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            EqDialKnobItem("GAIN", deckState.gain, 0.0f, 1.5f, accentColor, onValueChange = onGainUpdate)
            EqDialKnobItem("LOW", deckState.lowEq, -12.0f, 6.0f, accentColor, onValueChange = { onEqUpdate("LOW", it) })
            EqDialKnobItem("MID", deckState.midEq, -12.0f, 6.0f, accentColor, onValueChange = { onEqUpdate("MID", it) })
            EqDialKnobItem("HIGH", deckState.highEq, -12.0f, 6.0f, accentColor, onValueChange = { onEqUpdate("HIGH", it) })
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play/Pause and Sync triggers
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onSyncClick,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F212F))
                    .testTag("sync_" + deckState.deckId.replace(" ", "_").lowercase())
            ) {
                Text(text = "SYNC", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ToxicGreen)
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (deckState.isPlaying) BurningOrange else accentColor)
                    .testTag("play_pause_" + deckState.deckId.replace(" ", "_").lowercase())
            ) {
                Icon(
                    imageVector = if (deckState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Trigger channel deck state",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EqDialKnobItem(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            // High-fidelity small slider to simulate EQs rotary dial drag!
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
        Text(text = label, fontSize = 7.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
        Text(
            text = if (label == "GAIN") String.format("%.1fx", value) else String.format("%+.0fdB", value),
            fontSize = 7.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MasterCrossfaderContainer(
    crossfaderVal: Float,
    onFaderChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1117), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "DECK A", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CyberCyan)
            Text(text = "DYNAMIC CROSSFADER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
            Text(text = "DECK B", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PsyPink)
        }

        Slider(
            value = crossfaderVal,
            onValueChange = onFaderChange,
            valueRange = -1.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = PsyPink,
                inactiveTrackColor = CyberCyan
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("master_crossfader")
        )
    }
}
