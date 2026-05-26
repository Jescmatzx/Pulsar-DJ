package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DecksScreen
import com.example.ui.screens.HardwareScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PulsarDjMain(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Decks, 1: Library, 2: Hardware, 3: Analytics
    var activeGoalAlert by remember { mutableStateOf<String?>(null) }

    // Listen to goal milestone achievements to display glorious celebratory alerts!
    LaunchedEffect(Unit) {
        viewModel.goalAlertMessage.collectLatest { msg ->
            activeGoalAlert = msg
            delay(5000) // Toast displays for 5 seconds
            activeGoalAlert = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            Column {
                // Top aesthetic branding bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(BackgroundDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberCyan)
                                .border(1.5.dp, PsyPink, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "PULSAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "DJ DECK",
                            fontWeight = FontWeight.Light,
                            fontSize = 18.sp,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    // Simulated real-time UTC Clock / Connection indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ToxicGreen)
                        )
                        Text(
                            text = "ONLINE [SYNC ENABLED]",
                            color = ToxicGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .testTag("pulsar_navigation_bar"),
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                listOf(
                    Triple("MIX CONSOLE", Icons.Default.MusicVideo, "tab_decks"),
                    Triple("TRACK LIBRARY", Icons.Default.FolderOpen, "tab_library"),
                    Triple("HARDWARE STG", Icons.Default.Piano, "tab_hardware"),
                    Triple("ANALYTICS STAT", Icons.Default.Analytics, "tab_analytics")
                ).forEachIndexed { index, (label, icon, tag) ->
                    val selected = selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) Color.Black else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                                color = if (selected) CyberCyan else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyberCyan,
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.White,
                            selectedTextColor = CyberCyan,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen contents
            when (selectedTab) {
                0 -> DecksScreen(viewModel)
                1 -> LibraryScreen(viewModel)
                2 -> HardwareScreen(viewModel)
                3 -> AnalyticsScreen(viewModel)
            }

            // CELEBRATIVE MOTIVATIONAL OVERLAY ALERT
            AnimatedVisibility(
                visible = activeGoalAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                if (activeGoalAlert != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.5.dp, ToxicGreen),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("glorious_goal_toast")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ToxicGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = "Cup", tint = ToxicGreen)
                            }
                            Column {
                                Text(
                                    text = "MILESTONE COMPLETED",
                                    fontWeight = FontWeight.Black,
                                    color = ToxicGreen,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = activeGoalAlert!!,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
