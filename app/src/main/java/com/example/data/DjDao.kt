package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DjDao {
    // --- TRACKS IN DATABASE ---
    @Query("SELECT * FROM tracks ORDER BY id DESC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY id DESC")
    fun getTracksByGenre(genre: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Int): Track?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Update
    suspend fun updateTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    // --- PLAY PERFORMANCE HISTORY ---
    @Query("SELECT * FROM djs_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DjsHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DjsHistory)

    @Delete
    suspend fun deleteHistory(history: DjsHistory)

    // --- GOAL MILESTONES ---
    @Query("SELECT * FROM goal_milestones ORDER BY isAchieved ASC, title ASC")
    fun getAllMilestones(): Flow<List<GoalMilestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<GoalMilestone>)

    @Update
    suspend fun updateMilestone(milestone: GoalMilestone)
}
