package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
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
import com.example.ui.DjViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.PsyPink
import com.example.ui.theme.ToxicGreen

@Composable
fun LibraryScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val activeTracks by viewModel.activePlaylist.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genreFilter by viewModel.genreFilter.collectAsState()
    val selectedMood by viewModel.currentMoodTheme.collectAsState()
    val offlineEnabled by viewModel.offlineModeEnabled.collectAsState()

    var showLoadDestinationTrack by remember { mutableStateOf<Track?>(null) }
    var showAddMetadataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- SEARCH INPUT CARD ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search title, artist, key...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = BentoBorder,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input")
        )

        // --- EXPLICIT GENRE FILTER ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("All", "Techno", "Psytrance", "High-Tech").forEach { genre ->
                val active = genreFilter == genre && selectedMood == null
                FilterChip(
                    selected = active,
                    onClick = { viewModel.updateGenreFilter(genre) },
                    label = { Text(genre, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceCard,
                        labelColor = Color.LightGray,
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color.Black
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = active,
                        borderColor = BentoBorder,
                        selectedBorderColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("filter_genre_$genre")
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add track manual trigger
            IconButton(
                onClick = { showAddMetadataDialog = true },
                modifier = Modifier
                    .size(34.dp)
                    .background(CyberCyan, RoundedCornerShape(8.dp))
                    .testTag("add_track_icon_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create track metadata", tint = Color.Black)
            }
        }

        // --- AUTOMATED MOOD SMART PLAYLIST SELECTORS ---
        Text(
            text = "AUTOMATED MOOD PLAYLIST GENERATOR",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val moodsList = listOf(
                Pair("Industrial Strobe", "Techno ⚡"),
                Pair("Cosmic Portal", "Psytrance 🪐"),
                Pair("Hyper-Drive", "High-Tech ☄️"),
                Pair("Deep Melodic Acid", "Acid Loops ☣️")
            )

            items(moodsList) { (moodName, emojiDesc) ->
                val active = selectedMood == moodName
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) CyberCyan.copy(alpha = 0.18f) else SurfaceCard
                    ),
                    border = if (active) BorderStroke(1.dp, CyberCyan) else BorderStroke(1.dp, BentoBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable {
                            if (active) {
                                viewModel.selectMoodPlaylist(null) // Reset
                            } else {
                                viewModel.selectMoodPlaylist(moodName)
                            }
                        }
                        .testTag("mood_card_${moodName.replace(" ", "_").lowercase()}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = emojiDesc, fontSize = 11.sp, color = if (active) CyberCyan else Color.White, fontWeight = FontWeight.Bold)
                        Text(text = moodName, fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }

        // --- OFFLINE MODE ADVISORY BANNER ---
        if (offlineEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ToxicGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, ToxicGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.WifiOff, contentDescription = "Offline compilation active", tint = ToxicGreen, modifier = Modifier.size(16.dp))
                Text(
                    text = "Offline Mode Active: Local track storage guarantees latency-free deck loading even in offline underground venues.",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // --- MUSIC LISTING ---
        Text(
            text = "LOADABLE SECTIONS: ${activeTracks.size} AUDIO TRACKS AVAILABLE",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(activeTracks) { track ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLoadDestinationTrack = track }
                        .testTag("library_track_card_${track.title.replace(" ", "_").lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Rounded icon based on category
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (track.genre) {
                                            "Techno" -> CyberCyan.copy(alpha = 0.15f)
                                            "Psytrance" -> PsyPink.copy(alpha = 0.15f)
                                            else -> Color(0xFF7C4DFF).copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (track.genre) {
                                        "Techno" -> Icons.Default.FlashOn
                                        "Psytrance" -> Icons.Default.AllInclusive
                                        else -> Icons.Default.Bolt
                                    },
                                    contentDescription = "Genre Tag",
                                    tint = when (track.genre) {
                                        "Techno" -> CyberCyan
                                        "Psytrance" -> PsyPink
                                        else -> Color(0xFF7C4DFF)
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = track.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (track.isOffline) {
                                        Icon(Icons.Default.CloudQueue, contentDescription = "Cached", tint = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                                    }
                                }
                                Text(text = "${track.artist}  •  ${track.genre}  •  ${track.mood}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (track.tagString.isNotEmpty()) {
                                    Text(text = track.tagString.split(",").joinToString(" #", prefix = "#"), color = CyberCyan.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Right side info parameters
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${track.bpm.toInt()} BPM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text(text = track.harmonicKey, color = ToxicGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Favorite mark star trigger
                            IconButton(
                                onClick = { viewModel.toggleTrackFavorite(track) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (track.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Track validation marker",
                                    tint = if (track.isFavorite) Color.Yellow else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // LOAD CHANNEL OVERLAY MODAL
    if (showLoadDestinationTrack != null) {
        val targetTrack = showLoadDestinationTrack!!
        AlertDialog(
            onDismissRequest = { showLoadDestinationTrack = null },
            title = { Text("Assign track to Deck", color = Color.White, fontSize = 15.sp) },
            containerColor = SurfaceDark,
            confirmButton = {},
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = targetTrack.title, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
                    Text(text = "Choose which active physical deck player to load audio track into:", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.loadTrackIntoDeck("Deck A", targetTrack)
                                showLoadDestinationTrack = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_dst_deck_a"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DECK A (CYAN)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.loadTrackIntoDeck("Deck B", targetTrack)
                                showLoadDestinationTrack = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PsyPink),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_dst_deck_b"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DECK B (ROSE)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        )
    }

    // FLOATING METADATA TAGGER MANUAL CREATOR DIALOG
    if (showAddMetadataDialog) {
        var mTitle by remember { mutableStateOf("") }
        var mArtist by remember { mutableStateOf("") }
        var mBpm by remember { mutableStateOf("140") }
        var mKey by remember { mutableStateOf("8A") }
        var mGenre by remember { mutableStateOf("Techno") }
        var mMood by remember { mutableStateOf("Energetic") }
        var mTags by remember { mutableStateOf("melodic, heavy") }

        AlertDialog(
            onDismissRequest = { showAddMetadataDialog = false },
            title = { Text("Create Metadata Tag Set", color = Color.White, fontSize = 16.sp) },
            containerColor = SurfaceDark,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createAndInsertTrack(
                            title = mTitle,
                            artist = mArtist,
                            bpm = mBpm.toDoubleOrNull() ?: 140.0,
                            key = mKey,
                            genre = mGenre,
                            mood = mMood,
                            tags = mTags
                        )
                        showAddMetadataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.testTag("submit_metadata_tag")
                ) {
                    Text("CREATE AUDIO TAG", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMetadataDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = mTitle,
                            onValueChange = { mTitle = it },
                            label = { Text("Track Title", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().testTag("meta_title")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mArtist,
                            onValueChange = { mArtist = it },
                            label = { Text("Artist Pro", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().testTag("meta_artist")
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mBpm,
                                onValueChange = { mBpm = it },
                                label = { Text("BPM", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f).testTag("meta_bpm")
                            )
                            OutlinedTextField(
                                value = mKey,
                                onValueChange = { mKey = it },
                                label = { Text("Harmonic Key (Camelot)", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f).testTag("meta_key")
                            )
                        }
                    }
                    item {
                        Text("GENRE SELECTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.4f))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Techno", "Psytrance", "High-Tech").forEach { gen ->
                                val selected = mGenre == gen
                                Button(
                                    onClick = { mGenre = gen },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (selected) CyberCyan else Color(0xFF1E212D), contentColor = if (selected) Color.Black else Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text(text = gen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = mMood,
                            onValueChange = { mMood = it },
                            label = { Text("Playlist Mood Match", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().testTag("meta_mood")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mTags,
                            onValueChange = { mTags = it },
                            label = { Text("Tags / Identifiers (comma separated)", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().testTag("meta_tags")
                        )
                    }
                }
            }
        )
    }
}
