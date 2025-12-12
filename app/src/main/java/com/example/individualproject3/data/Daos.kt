package com.example.individualproject3.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE isParent = 0")
    fun getAllChildren(): Flow<List<User>>
}

@Dao
interface GameDao {
    @Insert
    suspend fun insertSession(session: GameSession)

    @Query("SELECT * FROM game_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSessionsForUser(userId: Int): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE userId = :userId")
    suspend fun getSessionsForUserSync(userId: Int): List<GameSession>

    @Query("SELECT levelName, COUNT(*) as attemptCount FROM game_sessions GROUP BY levelName")
    suspend fun getLevelAttemptStats(): List<LevelAttemptStats>
}

data class LevelAttemptStats(
    val levelName: String,
    val attemptCount: Int
)
