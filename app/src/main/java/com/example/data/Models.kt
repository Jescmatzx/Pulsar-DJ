package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val bpm: Double,
    val harmonicKey: String, // e.g. "8A", "9A", "11B" (Camelot scale)
    val genre: String, // "Techno", "Psytrance", "High-Tech"
    val energy: Int, // 1 - 10 scale
    val durationSeconds: Int,
    val isOffline: Boolean = true,
    val tagString: String = "", // e.g. "melodic, acid, heavy"
    val mood: String = "Energetic", // "Industrial", "Euphoric", "Cosmic", "Aggressive", "Acidic", "Trippy"
    val isFavorite: Boolean = false
) : Serializable

@Entity(tableName = "djs_history")
data class DjsHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long,
    val setType: String, // "Club/Festival Sets", "Mobile/Event Sets", "Radio Sets", "Open Format Sets"
    val performanceSetup: String, // "Live Set", "Hybrid Set", "Turntablism / Scratch Set"
    val totalTracksPlayed: Int,
    val averageBpm: Double,
    val energyRating: Int, // 1 to 5 stars
    val description: String = ""
) : Serializable

@Entity(tableName = "goal_milestones")
data class GoalMilestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val targetValue: Float,
    val currentValue: Float,
    val isAchieved: Boolean = false,
    val category: String, // "Hours Played", "BPM Master", "Sets Recorded", "Scratch Exercises"
    val rewardIcon: String // Name of graphic descriptor / asset identifier
) : Serializable
