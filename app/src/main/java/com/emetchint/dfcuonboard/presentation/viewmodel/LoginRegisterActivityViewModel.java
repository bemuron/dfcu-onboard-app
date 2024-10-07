package com.emetchint.dfcuonboard.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.emetchint.dfcuonboard.data.DFCUOnboardRepository;
import com.emetchint.dfcuonboard.models.IdPic;
import com.emetchint.dfcuonboard.models.User;

import java.util.List;

public class LoginRegisterActivityViewModel extends ViewModel {
    private static final String TAG = LoginRegisterActivityViewModel.class.getSimpleName();
    //private member variable to hold reference to the repository
    private DFCUOnboardRepository mRepository;

    //constructor that gets a reference to the repository and gets the categories
    public LoginRegisterActivityViewModel(DFCUOnboardRepository repository) {
        //super(application);
        mRepository = repository;
    }

    //call repository method to handle staff email validation
    public void validateUserEmail(String email){
        mRepository.validateUserEmail(email);
    }

    //handle 10 digit code verification
    public void verifyUserCode(String code){
        mRepository.verifyUserCode(code);
    }

    //returning if login is successful or not
    public void OnSuccessfulLogin(Boolean isLoginSuccessful){

    }

    //call repository method to handle posting user reg details to server
    /*public void registerUser(String name, String date_of_birth, String gender, String email, String password, String phoneNumber){
        mRepository.registerFixAppUser(name, date_of_birth, gender, email, password, phoneNumber);
    }*/

    //a getter method to get all the employees.
    // This hides the implementation from the UI
    public LiveData<List<User>> getAllEmployees(String empId){
        return mRepository.getAllEmployees(empId);
    }

    public void getApiLogs(String logDate){
        mRepository.getApiLogs(logDate);
    }

    //get a list of images attached for this employee
    public LiveData<List<IdPic>> getIdPics(int userId){
        return mRepository.getJobImages(userId);
    }

    //gt id pics for edit
    public void getIDPics(int adId){
        mRepository.getIDPics(adId);
    }

    //delete an id pic
    public void deleteIDPic(int pic_id){
        mRepository.deleteIDPic(pic_id);
    }

    public void insert(User user) { mRepository.insertUser(user); }

    public void updateUser(int userId, int role, String dob) {
        mRepository.updateProfile(userId,role,dob);
    }

    //deletes user from sqlite db - room
    public void delete() { mRepository.deleteUser();}
}
