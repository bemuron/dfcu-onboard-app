package com.emetchint.dfcuonboard.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import android.database.Cursor;

import com.emetchint.dfcuonboard.models.User;

/**
 * Created by BE on 2/3/2018.
 */

@Dao
public interface UsersDao {

    /*
    insert user into db
     */
    @Insert
    void insertUser(User user);

    @Query("Delete from user")
    void deleteUser();

    @Query("SELECT * from user")
    //HashMap<String, String> getUserDetails();
    //Cursor getUserDetails();
    LiveData<User> getUserDetails();

    //update user details
    @Query("UPDATE user SET role = :role, " +
            "date_of_birth = :date_of_birth WHERE user_id = :user_id")
    void updateProfile(int user_id, int role, String date_of_birth);
}
