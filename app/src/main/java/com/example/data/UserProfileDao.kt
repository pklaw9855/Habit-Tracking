package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUserProfileSync(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC")
    suspend fun getAllProfilesSync(): List<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE user_profiles SET isLoggedIn = 1 WHERE userId = :userId")
    suspend fun setLoggedInUser(userId: String)

    @Query("UPDATE user_profiles SET role = :role WHERE userId = :userId")
    suspend fun updateUserRole(userId: String, role: String)
}
