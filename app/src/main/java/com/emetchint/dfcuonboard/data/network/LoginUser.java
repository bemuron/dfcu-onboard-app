package com.emetchint.dfcuonboard.data.network;

import static com.emetchint.dfcuonboard.presentation.ui.activity.CodeVerificationActivity.codeVerificationActivity;
import static com.emetchint.dfcuonboard.presentation.ui.activity.EmailVerificationActivity.emailVerificationActivity;
import static com.emetchint.dfcuonboard.presentation.ui.activity.RegisterActivity.registerActivity;

import android.content.Context;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import com.emetchint.dfcuonboard.data.network.api.APIService;
import com.emetchint.dfcuonboard.data.network.api.APIUrl;
import com.emetchint.dfcuonboard.data.network.api.LocalRetrofitApi;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.IdPic;
import com.emetchint.dfcuonboard.models.UploadPic;
import com.emetchint.dfcuonboard.models.User;
import com.emetchint.dfcuonboard.utilities.AppExecutors;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginUser {
    private static final String LOG_TAG = LoginUser.class.getSimpleName();

    private final AppExecutors mExecutors;
    SuccessfulLoginCallBack successfulLoginCallBack;
    ProfileUpdatedCallBack profileUpdatedCallBack;
    ValidationCodeSentCallBack validationCodeSentCallBack;
    SuccessfulCodeVerificationCallBack codeVerificationCallBack;
    private final MutableLiveData<List<UploadPic>> mAdImagesForEdit;
    private final MutableLiveData<List<IdPic>> mJobImages;
    private List<UploadPic> adImageList = new ArrayList<UploadPic>();
    private List<IdPic> idPicList = new ArrayList<IdPic>();
    private UploadPic adImage;
    private final MutableLiveData<List<User>> mDownloadedEmployees;
    private IdPic idPic;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static LoginUser sInstance;

    private final Context mContext;
    private User user;
    private SessionManager sessionManager;

    public LoginUser(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedEmployees = new MutableLiveData<>();
        mAdImagesForEdit = new MutableLiveData<>();
        mJobImages = new MutableLiveData<>();
        // Session manager
        sessionManager = new SessionManager(context.getApplicationContext());

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        /*try {
            successfulLoginCallBack = loginActivity.getInstance();
        } catch (ClassCastException e) {
            Log.d(LOG_TAG, e.getMessage());
            throw new ClassCastException(context.toString()
                    + " must implement onLoginSuccessful");
        }*/

    }

    /**
     * Get the singleton for this class
     */
    public static LoginUser getInstance(Context context, AppExecutors executors) {
        //successfulLoginCallBack = loginActivity.getInstance();
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new LoginUser(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    //returned list of employees
    public LiveData<List<User>> getStaffList() {
        return mDownloadedEmployees;
    }
    public LiveData<List<UploadPic>> getAdImagesForEdit() {
        return mAdImagesForEdit;
    }
    //returned list of job images
    public LiveData<List<IdPic>> getJobImages() {
        return mJobImages;
    }

    public void UserEmailValidation(String email) {
        Log.d(LOG_TAG, "User email validation started");

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging)
                .connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);

        //defining the call
        Call<Result> call = service.userEmailValidation(email);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    //count what we have in the response
                    if (!response.body().getError()) {

                        // If the code reaches this point, the 10 digit code has been sent to the user
                        Log.d(LOG_TAG, "User email validation code sent");

                        //user = new User();
                        //user.setAccess_token(response.body().getAccess_token());
                        sessionManager.saveUserToken(response.body().getAccess_token());
                        sessionManager.saveUserEmail(response.body().getEmail());

                        emailVerificationActivity.onCodeSent(true, "Validation code sent to your email");
                    }else{
                        //Log.e(LOG_TAG, "we have an error logging in");
                        emailVerificationActivity.onCodeSent(false, response.body().getMessage());
                    }
                }else{
                    //Log.e(LOG_TAG, "response.body() is null");
                    emailVerificationActivity.onCodeSent(false, "Email not recognised. Contact the admin");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                emailVerificationActivity.onCodeSent(false, "Email Validation failure. Please try again");
            }
        });
    }

    //verify the user code
    public void UserCodeVerification(String code) {
        Log.d(LOG_TAG, "Code verification started");

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging)
                .connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);
        String token = "Bearer "+sessionManager.getUserToken();
        String email = sessionManager.getUserEmail();

        //defining the call
        Call<Result> call = service.userCodeVerification(token,email,code);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    if (!response.body().getError()) {
                        String message;

                        //if user details are null, user is not yet registered
                        //create new user object
                        user = new User();
                        if(response.body().getUser().getIs_registered() == 0){
                            message = "Successful verification. Please register";
                            //user.setAccess_token(response.body().getAccess_token());
                        }else{
                            message = "Successful verification.";
                            user.setUser_id(response.body().getUser().getUser_id());
                            user.setEmp_id(response.body().getUser().getEmp_id());
                            user.setName(response.body().getUser().getName());
                            user.setDate_of_birth(response.body().getUser().getDate_of_birth());
                            user.setEmail(response.body().getUser().getEmail());
                            user.setRole(response.body().getUser().getRole());
                            user.setProfile_pic(response.body().getUser().getProfile_pic());
                            user.setIs_registered(response.body().getUser().getIs_registered());
                            user.setCreated_at(response.body().getUser().getCreated_at());

                        }

                        codeVerificationActivity.onCodeVerified(true, user, message);
                    }else{
                        //Log.e(LOG_TAG, "we have an error logging in");
                        codeVerificationActivity.onCodeVerified(false, null, "An error occurred while verifying. Try again");
                    }
                }else{
                    //Log.e(LOG_TAG, "response.body() is null");
                    codeVerificationActivity.onCodeVerified(false, null, "Invalid code. Enter correct code");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                codeVerificationActivity.onCodeVerified(false, null, "Code verification failure");
            }
        });
    }

    //get api logs
    public void GetAPILogs(String logDate) {

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.getApiLog(token,logDate);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getUploadPics() != null) {
                        //Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (adImageList != null) {
                            adImageList.clear();
                        }

                        for (int i = 0; i < response.body().getUploadPics().size(); i++) {
                            adImage = new UploadPic();
                            adImage.setImage_name(response.body().getUploadPics().get(i).getImage_name());
                            adImage.setPic_id(response.body().getUploadPics().get(i).getPic_id());

                            adImageList.add(adImage);
                        }

                        registerActivity.onAdImagesGot(true, adImageList);
                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        //mAdImagesForEdit.postValue(adImageList);
                    }
                }catch (Exception e){
                    registerActivity.onAdImagesGot(false, null);
                    //Log.e(LOG_TAG,"Could not get jobs images for job id " +job_id);
                    //e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                registerActivity.onAdImagesGot(false, null);
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    public void UserRegistration(String email) {
        Log.d(LOG_TAG, "User registration started");

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging)
                .connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);

        //defining the call
        Call<Result> call = service.userEmailValidation(email);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    //count what we have in the response
                    if (!response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //response.body().getUser();

                        // If the code reaches this point, we have successfully logged in
                        //Log.d(LOG_TAG, "Successful login");

                        //create new user object
                        user = new User();
                        user.setUser_id(response.body().getUser().getUser_id());
                        user.setEmail(response.body().getUser().getEmail());
                        user.setCreated_at(response.body().getUser().getCreated_at());
                        user.setRole(response.body().getUser().getRole());
                        user.setDescription(response.body().getUser().getDescription());
                        user.setProfile_pic(response.body().getUser().getProfile_pic());
                        user.setDate_of_birth(response.body().getUser().getDate_of_birth());
                        user.setName(response.body().getUser().getName());
                        user.setAccess_token(response.body().getAccess_token());
                        //Log.d(LOG_TAG, mFixappUser.getEmail() + " user email");

                        successfulLoginCallBack.onLoginSuccessful(true, user, "successful login");
                    }else{
                        //Log.e(LOG_TAG, "we have an error logging in");
                        successfulLoginCallBack.onLoginSuccessful(false, null, "An error occurred logging in. Try again");
                    }
                }else{
                    //Log.e(LOG_TAG, "response.body() is null");
                    successfulLoginCallBack.onLoginSuccessful(false, null, "Invalid login details");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                successfulLoginCallBack.onLoginSuccessful(false, null, "Validation failure");
            }
        });
    }

    //getting the images for a specific employee
    public void GetJobImages(int user_id) {
        Log.d(LOG_TAG, "Fetch id images started");

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.getIdImages(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getIdPics() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (idPicList != null) {
                            idPicList.clear();
                        }

                        for (int i = 0; i < response.body().getIdPics().size(); i++) {
                            idPic = new IdPic();
                            idPic.setImage_name(response.body().getIdPics().get(i).getImage_name());

                            idPicList.add(idPic);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mJobImages.postValue(idPicList);
                    }
                }catch (Exception e){
                    //Log.e(LOG_TAG,"Could not get jobs images for job id " +job_id);
                    //e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    public void GetAdImagesForEdit(int ad_id) {

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.getAdImages(ad_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getUploadPics() != null) {
                        //Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (adImageList != null) {
                            adImageList.clear();
                        }

                        for (int i = 0; i < response.body().getUploadPics().size(); i++) {
                            adImage = new UploadPic();
                            adImage.setImage_name(response.body().getUploadPics().get(i).getImage_name());
                            adImage.setPic_id(response.body().getUploadPics().get(i).getPic_id());

                            adImageList.add(adImage);
                        }

                        registerActivity.onAdImagesGot(true, adImageList);
                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        //mAdImagesForEdit.postValue(adImageList);
                    }
                }catch (Exception e){
                    registerActivity.onAdImagesGot(false, null);
                    //Log.e(LOG_TAG,"Could not get jobs images for job id " +job_id);
                    //e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                registerActivity.onAdImagesGot(false, null);
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    public void getAllEmployees(String user_id) {
        Log.d(LOG_TAG, "Fetch employee list started");

        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.getAllEmployees(token, user_id);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getEmployeeList() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mDownloadedEmployees.postValue(response.body().getEmployeeList());

                    }else{
                        mDownloadedEmployees.postValue(null);
                    }
                }catch (Exception e){
                    Log.e(LOG_TAG,"Could not get jobs");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                mDownloadedEmployees.postValue(null);
            }
        });

    }

    //delete the ad pic
    public void deleteAdPic(int pic_id){

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.deleteAdPic(token, pic_id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try{
                    if (response.body() != null && !response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //Log.e(LOG_TAG, "nin pic deleted");
                        //send data to parent activity
                        registerActivity.onAdPicDeleted(true, response.body().getMessage());
                    }else{
                        //Log.e(LOG_TAG, "nin pic NOT deleted");
                        //send data to parent activity
                        registerActivity.onAdPicDeleted(false, "Ad image not removed");
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                    //Log.e(LOG_TAG, "nin pic NOT deleted");
                    //send data to parent activity
                    registerActivity.onAdPicDeleted(false, "Ad image not removed");
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //Log.e(LOG_TAG, t.getMessage());
                registerActivity.onAdPicDeleted(false,
                        "Sorry could not remove the ad pic now");
            }
        });
    }

    //retrofit call to logout the user
    //deletes the user's fcm and access tokens from the db
    public void UserLogout(int user_id){
        APIService service = new LocalRetrofitApi().getRetrofitService();
        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.userLogout(token, user_id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                //if response body is not null, we have some data
                //successful addition
                if (response.body() != null && !response.body().getError()) {
                    //Log.e(LOG_TAG, "logged out successfully");
                    //send response data to the activity
                    //success
                    /*homeActivityInstance.logOutUser(true,
                            response.body().getMessage());*/
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //Log.e(LOG_TAG, t.getMessage());
                /*homeActivityInstance.logOutUser(true,
                        "Sorry, log out failed");*/
            }
        });
    }

    //retrofit call to update user details
    //update user details
    /*public void updateUserDetails(int user_id, String username, String location, String gender, String email,
                                  String dob, String aboutUser, MainActivity activity){
        //profileUpdatedCallBack = activity;
        String mysqlDate = null;
        //convert the date coming in to the one mysql expects
        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        try{
            Date d = sdf.parse(dob);
            mysqlDate = mysqlDateFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        //Defining retrofit api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.updateUserDetails(token, user_id, username, location, gender, email, mysqlDate,
                aboutUser);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        Log.e(LOG_TAG, "User details updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePosted(true, response.body().getMessage());
                    }else{
                        Log.e(LOG_TAG, "User details NOT updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePosted(false, "User details NOT updated");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "User details NOT updated - catch block");
                    //send data to parent activity
                    profileUpdatedCallBack.onProfilePosted(false, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                Log.e(LOG_TAG, "User details NOT updated - onFailure block");
                //send data to parent activity
                profileUpdatedCallBack.onProfilePosted(false,
                        "Something went wrong, profile not updated");
            }
        });
    }*/

    //save the nin pic the user has added
    /*public void saveNinPic(int user_id, File ninPic, MainActivity activity){
        //profileUpdatedCallBack = activity;

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), ninPic);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", ninPic.getName(), requestBody);
        RequestBody fileName = RequestBody.create(MediaType.parse("text/plain"), ninPic.getName());

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.saveNinPic(token, user_id, fileToUpload, fileName);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try{
                    if (!response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        int nin_id = response.body().getUploadPic().getPic_id();
                        //send data to parent activity
                        profileUpdatedCallBack.onNinPicSaved(true, nin_id, response.body().getMessage());
                    }else{
                        //Log.e(LOG_TAG, "nin pic NOT saved");
                        //send data to parent activity
                        profileUpdatedCallBack.onNinPicSaved(false, 0, "Nin image not saved");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //Log.e(LOG_TAG, "nin pic NOT saved");
                    //send data to parent activity
                    profileUpdatedCallBack.onNinPicSaved(false, 0, "Nin image not saved");
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                profileUpdatedCallBack.onNinPicSaved(false,0,
                        "Sorry could not save the nin pic now");
            }
        });
    }*/

    //delete the nin pic the user has added
    /*public void deleteNinPic(int user_id, int pic_id, MainActivity activity){
        //profileUpdatedCallBack = activity;

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.deleteNinPic(token, user_id, pic_id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try{
                    if (response.body() != null && !response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //Log.e(LOG_TAG, "nin pic deleted");
                        //send data to parent activity
                        profileUpdatedCallBack.onNinPicDeleted(true, response.body().getMessage());
                    }else{
                        //Log.e(LOG_TAG, "nin pic NOT deleted");
                        //send data to parent activity
                        profileUpdatedCallBack.onNinPicDeleted(false, "Nin image not removed");
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                    //Log.e(LOG_TAG, "nin pic NOT deleted");
                    //send data to parent activity
                    profileUpdatedCallBack.onNinPicDeleted(false, "Nin image not removed");
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //Log.e(LOG_TAG, t.getMessage());
                profileUpdatedCallBack.onNinPicDeleted(false,
                        "Sorry could not remove the nin pic now");
            }
        });
    }*/

    //getting the user's nin images
    /*public void GetUserNINImages(int user_id) {
        Log.d(LOG_TAG, "Fetch nin images started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.getUserNinImages(token, user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && !response.body().getError()) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (ninPicsList != null) {
                            ninPicsList.clear();
                        }

                        for (int i = 0; i < response.body().getPortfolioPics().size(); i++) {
                            ninPic = new JobPic();
                            ninPic.setPic_id(response.body().getPortfolioPics().get(i).getId());
                            ninPic.setImage_name(response.body().getPortfolioPics().get(i).getImage_name());

                            ninPicsList.add(ninPic);
                        }

                        editProfileFragment.onNinImagesGot(true, ninPicsList);

                        Log.d(LOG_TAG, "Size of nin images list: " + response.body().getPortfolioPics().size());
                    }
                }catch (Exception e){
                    editProfileFragment.onNinImagesGot(false, null);
                    Log.e(LOG_TAG,"Could not get nin images for user id " +user_id);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                editProfileFragment.onNinImagesGot(false, null);
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }*/

    /**
     * The interface that receives whether the login was successful or not
     */
    public interface SuccessfulLoginCallBack {
        void onLoginSuccessful(Boolean isLoginSuccessful, User user, String null_response_received);
    }

    public interface ValidationCodeSentCallBack {
        void onCodeSent(Boolean isCodeSent, String message);
    }

    public interface SuccessfulCodeVerificationCallBack {
        void onCodeVerified(Boolean isCodeVerified, User user,String message);
    }

    //receive status of profile save
    public interface ProfileUpdatedCallBack {
        void onProfilePosted(Boolean isProfileSaved, String message);
        void onNinPicSaved(Boolean isPicSaved, int picId, String msg);
        void onNinPicDeleted(Boolean isNinDeleted, String msg);
    }

}
