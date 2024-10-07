package com.emetchint.dfcuonboard.data;

import androidx.lifecycle.LiveData;

import android.util.Log;

import com.emetchint.dfcuonboard.data.database.UsersDao;
import com.emetchint.dfcuonboard.models.IdPic;
import com.emetchint.dfcuonboard.utilities.AppExecutors;
import com.emetchint.dfcuonboard.app.MyApplication;
import com.emetchint.dfcuonboard.data.network.LoginUser;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.User;

import java.io.File;
import java.util.List;

/**
 * the DFCUOnboardRepository is a singleton.
 */

public class DFCUOnboardRepository{
    private static final String LOG_TAG = DFCUOnboardRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static DFCUOnboardRepository sInstance;
    private AppExecutors mExecutors;
    private LoginUser mLoginUser;
    private boolean mInitialized = false;
    private UsersDao mUsersDao;
    private LiveData<User> mUserDetail;
    private SessionManager sessionManager;

    private DFCUOnboardRepository(UsersDao usersDao,
                             LoginUser loginUser, AppExecutors executors) {
        mUsersDao = usersDao;
        mLoginUser = loginUser;
        mExecutors = executors;
        // Session manager
        sessionManager = new SessionManager(MyApplication.getInstance().getApplicationContext());
    }

    public synchronized static DFCUOnboardRepository getInstance(
            UsersDao usersDao, LoginUser loginUser, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new DFCUOnboardRepository(usersDao,
                        loginUser, executors);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    public AppExecutors getExecutors(){
        return mExecutors;
    }

    //user updates their profile
    //update user details
    /*public void updateUserProfile(int user_id, String username, String location, String gender, String email,
                                  String dob, String aboutUser, MyProfileActivity activity){
        Log.e(LOG_TAG, "calling method to update user details");
        mExecutors.diskIO().execute(() -> mLoginUser.updateUserDetails(user_id, username,location, gender, email,
                dob, aboutUser,activity));
    }

    //save the user's new profile pic
    public void saveProfilePic(int user_id,File profilePic, MyProfileActivity activity){
        Log.e(LOG_TAG, "calling method to save user profile pic");
        mExecutors.diskIO().execute(() -> mLoginUser.saveProfilePic(user_id, profilePic,activity));
    }

    //log out the user
    public void logOutUser(int user_id){
        mExecutors.diskIO().execute(() -> mLoginUser.UserLogout(user_id));
    }

    //save the user's nin pic
    public void saveNinPic(int user_id,File ninPic, MyProfileActivity activity){
        Log.e(LOG_TAG, "calling method to save user nin pic");
        mExecutors.diskIO().execute(() -> mLoginUser.saveNinPic(user_id, ninPic,activity));
    }

    //delete the user's nin pic
    public void deleteNinPic(int user_id,int pic_id, MyProfileActivity activity){
        Log.e(LOG_TAG, "calling method to delete user nin pic");
        mExecutors.diskIO().execute(() -> mLoginUser.deleteNinPic(user_id, pic_id,activity));
    }*/

    /*public void getUserNinImages(int user_id){
        mExecutors.diskIO().execute(() -> mLoginUser.GetUserNINImages(user_id));
    }*/

    /*public void getUserNin(int user_id){
        mExecutors.diskIO().execute(() -> mLoginUser.GetUserNIN(user_id));
    }*/

    public LiveData<User> getUserDetails(){
        mUserDetail = mUsersDao.getUserDetails();
        Log.d(LOG_TAG, "Getting user details from db");
        return mUserDetail;
    }

    //a wrapper for the insert() method. Must be called on a non UI thread
    //or the app will crash
    public void insertUser (User user){
        mExecutors.diskIO().execute(() ->{
            mUsersDao.insertUser(user);

            Log.d(LOG_TAG, user.getEmail()+" user inserted into db");
        });
    }

    //delete user details from db
    //required when user logs out of app
    public void deleteUser (){
        mExecutors.diskIO().execute(() -> {
            mUsersDao.deleteUser();
        });
    }

    //updates the user details in the db
    public void updateProfile(int user_id, int role, String date_of_birth){
        mExecutors.diskIO().execute(() ->{
            mUsersDao.updateProfile(user_id, role, date_of_birth);

            Log.d(LOG_TAG, " user profile updated");
        });
    }

    //method to validate user's email
    public void validateUserEmail(String email){
        mExecutors.diskIO().execute(() -> mLoginUser.UserEmailValidation(email) );
    }

    //method to make network call to verify user code
    public void verifyUserCode(String code){
        mExecutors.diskIO().execute(() -> mLoginUser.UserCodeVerification(code) );
    }

    //get id pics for edit
    public void getIDPics(int ad_id){
        mExecutors.diskIO().execute(() -> mLoginUser.GetAdImagesForEdit(ad_id));
        //return mLoginUser.getPortImages();
    }

    public void getApiLogs(String logDate){
        mExecutors.diskIO().execute(() -> mLoginUser.GetAPILogs(logDate));
        //return mLoginUser.getPortImages();
    }

    //getting id images for an employee
    public LiveData<List<IdPic>> getJobImages(int user_id){
        mExecutors.diskIO().execute(() -> mLoginUser.GetJobImages(user_id));
        return mLoginUser.getJobImages();
    }

    //delete the ad image
    public void deleteIDPic(int pic_id){
        Log.e(LOG_TAG, "calling method to delete ad pic");
        mExecutors.diskIO().execute(() -> mLoginUser.deleteAdPic(pic_id));
    }

    //getting all employees
    public LiveData<List<User>> getAllEmployees(String user_id){
        Log.d(LOG_TAG, "calling bg method to get employee list");
        mExecutors.diskIO().execute(() -> mLoginUser.getAllEmployees(user_id));
        return mLoginUser.getStaffList();
    }

    //returning if login is successful or not
    public void OnSuccessfulLogin(Boolean isLoginSuccessful){

    }

    //method to register user in database
    //calls service
    /*public void registerFixAppUser(String name, String date_of_birth, String gender, String email, String password,String phoneNumber){
        mRegisterUser.startRegisterUserService(name, date_of_birth, gender, email, password, phoneNumber);
    }*/

}
