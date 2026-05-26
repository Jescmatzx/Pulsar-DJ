package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DjViewModel
import com.example.ui.components.DvsScope
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen

@Composable
fun HardwareScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val setType by viewModel.djSetType.collectAsState()
    val perfSetup by viewModel.performanceSetup.collectAsState()
    val dvsEnabled by viewModel.isDvsEnabled.collectAsState()
    val dvsTargetDeck by viewModel.dvsTargetDeck.collectAsState()
    val cloudEnabled by viewModel.cloudSyncEnabled.collectAsState()
    val cloudStatus by viewModel.cloudSyncProgress.collectAsState()

    var activeCalibTone by remember { mutableStateOf("1 kHz Sinusoid") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTOR 1: FORMAT SETTING BY VENUE ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DJ FORMAT SETTING & CLUB FORMAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Configures audio output parameters, transition timing rules, and crowd feedback profiles based on venue.",
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf(
                        Triple("Club/Festival Sets", "High-Energy", Icons.Default.Audiotrack),
                        Triple("Mobile/Event Sets", "Wedding/Request", Icons.Default.FormatListBulleted),
                        Triple("Radio Sets", "Podcast Timed", Icons.Default.Radio),
                        Triple("Open Format Sets", "Genre Fluid", Icons.Default.AutoAwesome)
                    )

                    formats.forEach { (title, subtitle, icon) ->
                        val active = setType == title
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) CyberCyan.copy(alpha = 0.15f) else SurfaceCard
                            ),
                            border = BorderStroke(1.dp, if (active) CyberCyan else BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(85.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("format_card_${title.replace(" ", "_").lowercase()}"),
                            onClick = { viewModel.updateSetType(title) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, contentDescription = title, tint = if (active) CyberCyan else Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = title.replace(" Sets", ""), fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1, color = Color.White)
                                Text(text = subtitle, fontSize = 8.sp, color = Color.Gray, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTOR 2: PERFORMANCE AND HARDWARE SETUP ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TYPES BY PERFORMANCE & HARDWARE SETUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PsyPink,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Locks deck constraints matching specialized physical audio console routing cards.",
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val setups = listOf(
                        Pair("Live Set", "Hardware Synths / Sequences"),
                        Pair("Hybrid Set", "Loop Modifiers / Live Vocals"),
                        Pair("Turntablism / Scratch Set", "Scratch manipulation / Juggling")
                    )

                    setups.forEach { (title, desc) ->
                        val active = perfSetup == title
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) PsyPink.copy(alpha = 0.15f) else SurfaceCard
                            ),
                            border = BorderStroke(1.dp, if (active) PsyPink else BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("setup_card_${title.split(" ").first().lowercase()}"),
                            onClick = { viewModel.updatePerformanceSetup(title) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = title.replace("Set", "Set / Mod"), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = desc, fontSize = 8.sp, color = Color.Gray, maxLines = 2, lineHeight = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTOR 3: DIGITAL VINYL SYSTEM (DVS) SIGNAL INTEGRATION CALIBRATION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                            Icon(Icons.Default.Radio, contentDescription = "DVS Mode", tint = ToxicGreen, modifier = Modifier.size(18.dp))
                            Text(
                                text = "DVS CO-CALIBRATION PORT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Switch(
                            checked = dvsEnabled,
                            onCheckedChange = { viewModel.toggleDvsEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ToxicGreen,
                                checkedTrackColor = ToxicGreen.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("dvs_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Scope phase visualizer drawn inside Canvas
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F1015), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            DvsScope(isCalibrated = dvsEnabled)
                        }

                        // Options sliders for hardware calibration parameters
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "ROUTING DECK SOURCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Deck A", "Deck B").forEach { deckId ->
                                    val active = dvsTargetDeck == deckId
                                    Button(
                                        onClick = { viewModel.updateDvsTargetDeck(deckId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) ToxicGreen else Color(0xFF1E212D),
                                            contentColor = if (active) Color.Black else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .weight(1f)
                                    ) {
                                        Text(text = deckId.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(text = "TIMECODE TUNING TONE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1 kHz Sinusoid", "2 kHz Sinusoid").forEach { tone ->
                                    val active = activeCalibTone == tone
                                    Button(
                                        onClick = { activeCalibTone = tone },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) ToxicGreen else Color(0xFF1E212D),
                                            contentColor = if (active) Color.Black else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .weight(1f)
                                    ) {
                                        Text(text = tone.split(" ").first() + "Hz", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = if (dvsEnabled) "STATUS: TIMECODE LOCKED & SIGNAL CALIBRATED" else "STATUS: DISCONNECTED (VIRTUAL MODE)",
                                color = if (dvsEnabled) ToxicGreen else Color.LightGray.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- SECTOR 4: INTEGRATED MIDI CONTROLLER MAPPING ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "MIDI Mapping", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "INTEGRATED MIDI MAPPING TERMINAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Map standard physical controller MIDI CC messages to knobs manually. Move sliders to simulate incoming signals:",
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )

                    var crossfaderCc by remember { mutableStateOf(8f) }
                    var channelAPitchCc by remember { mutableStateOf(2f) }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Crossfader (Mapped: CC ${crossfaderCc.toInt()})", fontSize = 9.sp, color = Color.White)
                            Slider(
                                value = crossfaderCc,
                                onValueChange = { crossfaderCc = it },
                                valueRange = 1f..127f,
                                colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Deck A Pitch (Mapped: CC ${channelAPitchCc.toInt()})", fontSize = 9.sp, color = Color.White)
                            Slider(
                                value = channelAPitchCc,
                                onValueChange = { channelAPitchCc = it },
                                valueRange = 1f..127f,
                                colors = SliderDefaults.colors(thumbColor = PsyPink, activeTrackColor = PsyPink)
                            )
                        }
                    }

                    // Virtual Trigger button simulating trigger key
                    Button(
                        onClick = { viewModel.handleKeyboardShortcut('Q') }, // Toggle play input simulation
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("simulate_midi_play")
                    ) {
                        Text("SIMULATE RECEIVED MIDI NOTE (TOGGLE PLAY DECK A)", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECTOR 5: REMOTE BACKUP & CLOUDSYNC SERVICE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                            Icon(Icons.Default.CloudQueue, contentDescription = "Cloud backup", tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "CLOUD COLLECTION BACKUP ENGINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Switch(
                            checked = cloudEnabled,
                            onCheckedChange = { viewModel.toggleCloudSync() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("cloud_switch")
                        )
                    }

                    Text(
                        text = "Synchronize tracks library collection metadata, playlist arrangements and history records automatically across Android, iOS, Windows, and macOS devices.",
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1015), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "STATUS: $cloudStatus", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ToxicGreen, modifier = Modifier.testTag("cloud_status_text"))
                        Button(
                            onClick = { viewModel.triggerCloudSyncNow() },
                            enabled = cloudEnabled,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f), contentColor = CyberCyan),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp).testTag("trigger_cloud_sync")
                        ) {
                            Text("SYNC NOW", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTOR 6: LAUNCHER CUSTOMIZABLE INTEGRATED KEYBOARD SHORTCUTS ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Manual shortcuts", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "CUSTOMIZABLE KEYBOARD SHORTCUTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Perfect for power users on Desktop platforms (ChromeOS, Windows, Mac), mappings are fully configurable:",
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )

                    val keys = listOf(
                        Pair("Q / W", "Toggle play & Pause on Deck A / Deck B respectively"),
                        Pair("A / S", "Match BPM instantly (Phase sync grids) on target Deck"),
                        Pair("D / F", "Increase / Decrease Speed Pitch manually on Deck A"),
                        Pair("J / K", "Increase / Decrease Speed Pitch manually on Deck B"),
                        Pair("Z / X", "Shift Crossfader slider position slowly Left / Right"),
                        Pair("C", "Center crossfader instantly")
                    )

                    keys.forEach { (combo, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1F222F), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = combo, fontSize = 9.sp, color = CyberCyan, fontWeight = FontWeight.Black)
                            }
                            Text(text = description, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
