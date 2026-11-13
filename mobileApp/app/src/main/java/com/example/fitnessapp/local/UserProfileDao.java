package com.example.fitnessapp.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfileEntity profile);

    @Query("SELECT * FROM user_profile WHERE id = :userId")
    UserProfileEntity getProfile(String userId);

    @Query("DELETE FROM user_profile")
    void clear();
}