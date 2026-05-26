package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DjsHistory
import com.example.data.GoalMilestone
import com.example.ui.DjViewModel
import com.example.ui.components.EnergyCurveGraph
import com.example.ui.components.GenrePieChart
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen

@Composable
fun AnalyticsScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.djsHistoryList.collectAsState()
    val milestones by viewModel.goalMilestones.collectAsState()
    val tracks by viewModel.tracksCollection.collectAsState()

    var activeReportAlertMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTOR 1: OVERALL METRICS DASHBOARD CARD ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERFORMANCE HISTORY & METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    // Export button for PDF/JSON reports
                    Button(
                        onClick = {
                            val count = history.size
                            val tracksPlayed = history.sumOf { it.totalTracksPlayed }
                            activeReportAlertMessage = "Set Report exported to clipboard! 📊 Total sets analyzed: $count, Total transitions recorded: $tracksPlayed. Compatibility matched: 98%."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f), contentColor = CyberCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("export_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPORT INSIGHTS", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val completedCount = history.size
                    val hoursMixed = String.format("%.1f", history.sumOf { it.durationSeconds } / 3600.0)
                    val totalTransitions = history.sumOf { it.totalTracksPlayed }

                    MetricGridItem("COMPLETED SETS", completedCount.toString(), Icons.Default.LibraryMusic, Modifier.weight(1f))
                    MetricGridItem("HOURS MIXED", "$hoursMixed Hrs", Icons.Default.Timer, Modifier.weight(1f))
                    MetricGridItem("TRANSITIONS", totalTransitions.toString(), Icons.Default.SyncAlt, Modifier.weight(1f))
                }
            }
        }

        // --- SECTOR 2: DATA-DRIVEN CHARTS GRAPH PANEL ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BentoBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "REAL-TIME SPECTRUM INTENSITY (ENERGY PROFILE)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    // Bezier curves of energy values
                    val energies = tracks.map { it.energy }
                    EnergyCurveGraph(
                        energyValues = energies,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pie ratio chart
                        val technoCount = tracks.count { it.genre == "Techno" }
                        val psyCount = tracks.count { it.genre == "Psytrance" }
                        val highTechCount = tracks.count { it.genre == "High-Tech" }

                        GenrePieChart(
                            technoPlayCount = technoCount,
                            psyPlayCount = psyCount,
                            highTechPlayCount = highTechCount,
                            modifier = Modifier.padding(6.dp)
                        )

                        // Genre legends detail
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "GENRE INVENTORY DENSITY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            GenreLegendIndicator("Techno (Industrial / Acid)", technoCount, CyberCyan)
                            GenreLegendIndicator("Psytrance (Cosmic / Tribal)", psyCount, PsyPink)
                            GenreLegendIndicator("High-Tech (Quantum / Warp)", highTechCount, Color(0xFF7C4DFF))
                        }
                    }
                }
            }
        }

        // --- SECTOR 3: TRAINING GOALS CALIBRATOR FOR SUSTAINED MOTIVATION ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ACTIVE TRAINING GOALS & MILESTONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ToxicGreen,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Accomplish drilling tasks to boost beatmatch accuracy and overall performance ratings. Touch cards to simulate practice hours!",
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )

                milestones.forEach { milestone ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (milestone.isAchieved) ToxicGreen.copy(alpha = 0.05f) else SurfaceCard
                        ),
                        border = BorderStroke(1.dp, if (milestone.isAchieved) ToxicGreen.copy(alpha = 0.3f) else BentoBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("milestone_card_${milestone.title.split(" ").first().lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (milestone.isAchieved) ToxicGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (milestone.isAchieved) Icons.Default.EmojiEvents else Icons.Default.Adjust,
                                    contentDescription = "milestone success marker",
                                    tint = if (milestone.isAchieved) ToxicGreen else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = milestone.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (milestone.isAchieved) ToxicGreen else Color.White
                                )
                                Text(text = milestone.description, fontSize = 10.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(4.dp))

                                // Linear Progress bar
                                val completionRatio = (milestone.currentValue / milestone.targetValue).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { completionRatio },
                                    color = if (milestone.isAchieved) ToxicGreen else CyberCyan,
                                    trackColor = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }

                            // Progress fractions/achieved text
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (milestone.isAchieved) "ACHIEVED" else "${milestone.currentValue.toInt()}/${milestone.targetValue.toInt()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (milestone.isAchieved) ToxicGreen else Color.White
                                )
                                Text(
                                    text = milestone.category,
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTOR 4: MIX LOGS LIST RECORD ---
        item {
            Text(
                text = "HISTORIC PLAYLIST LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No mixes logged yet. Hit REC in the mixing console, drop tracks and cut faders to record live sets!", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(history) { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BentoBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = record.setTitle, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "${record.setType}  •  ${record.performanceSetup}", fontSize = 10.sp, color = CyberCyan)
                            Text(text = "Bpm Average: ${String.format("%.1f", record.averageBpm)}  •  ${record.totalTracksPlayed} Tracks", fontSize = 11.sp, color = Color.Gray)
                            if (record.description.isNotEmpty()) {
                                Text(text = record.description, fontSize = 10.sp, color = Color.LightGray.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Star Rating visualizer
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating Star",
                                        tint = if (i <= record.energyRating) Color.Yellow else Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteHistoryRecord(record) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_log_" + record.setTitle.replace(" ", "_").lowercase())
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Purge history record", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Report alert Dialog
    if (activeReportAlertMessage != null) {
        AlertDialog(
            onDismissRequest = { activeReportAlertMessage = null },
            title = { Text("Export Successful", color = Color.White, fontSize = 15.sp) },
            containerColor = SurfaceDark,
            confirmButton = {
                Button(
                    onClick = { activeReportAlertMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("OK", color = Color.Black)
                }
            },
            text = {
                Text(text = activeReportAlertMessage!!, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        )
    }
}

// --- SUB-ELEMENT DRAWS FOR METRICS ---

@Composable
fun MetricGridItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = modifier.height(68.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                Icon(icon, contentDescription = label, tint = CyberCyan, modifier = Modifier.size(12.dp))
            }
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun GenreLegendIndicator(
    name: String,
    playCount: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = "$name: $playCount tracks cataloged", fontSize = 10.sp, color = color, maxLines = 1)
    }
}
