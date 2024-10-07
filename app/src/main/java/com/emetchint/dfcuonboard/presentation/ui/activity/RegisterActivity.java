package com.emetchint.dfcuonboard.presentation.ui.activity;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;

import android.Manifest;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.emetchint.dfcuonboarding.BuildConfig;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import android.content.ClipData;

import com.emetchint.dfcuonboard.BuildConfig;
import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.data.network.Result;
import com.emetchint.dfcuonboard.data.network.api.APIService;
import com.emetchint.dfcuonboard.data.network.api.LocalRetrofitApi;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.UploadPic;
import com.emetchint.dfcuonboard.models.User;
import com.emetchint.dfcuonboard.presentation.adapters.UploadImagesAdapter;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements
        UploadImagesAdapter.UploadImagesAdapterListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private static final int READ_MEDIA_CODE = 58;
    private static final int CAMERA_PERMISSION_CODE = 56;
    private static final int STORAGE_PERMISSION_CODE = 57;
    private static final int M_MAX_ENTRIES = 5;
    private static final int SELECT_IMAGE_REQUEST_CODE =25 ;
    public static final int REQUEST_CAMERA_CAPTURE = 4;
    private Button btnRegister;
    private EditText inputSurname;
    private EditText inputOtherName;
    private EditText inputEmail;
    private int positionToDelete;
    private EditText dob;
    private Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog mDatePickerDialog;
    private ProgressDialog pDialog;
    private SessionManager session;
    private int[] resImg = {R.mipmap.ic_camera, R.drawable.ic_menu_gallery};
    private String[] title = {"Camera", "Gallery"};
    private ArrayList<UploadPic> uploadImageArrayList;
    private ArrayList<File> imageFilesList = new ArrayList<>();
    String[] projection = {MediaStore.MediaColumns.DATA};
    private File imageFile, mPhotoFile;
    private Boolean isCamera = false;
    private RadioGroup radioGroupUserRole;
    private RadioButton userRoleRadio;
    private Uri imageUri;
    private String mCurrentPhotoPath,email,userEditing,selectedUserRole,
            editEmpName,editDob,editEmail,mysqlDOB = null;
    private UploadPic imageModel;
    private UploadImagesAdapter uploadImagesAdapter;
    private RecyclerView recyclerView;
    int editUserId, editUserRole, role = 1;
    public static RegisterActivity registerActivity;
    private TextView userRoleTitleTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerActivity = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        // Session manager
        session = new SessionManager(getApplicationContext());
        email = session.getUserEmail();

        setUpCalendar(); //set up the calendar

        recyclerView = findViewById(R.id.recyclerview_id_images);
        uploadImageArrayList = new ArrayList<>();
        setImageAdapter();
        inputSurname = findViewById(R.id.surname_edittext);
        inputOtherName = findViewById(R.id.other_names_edittext);
        userRoleTitleTV = findViewById(R.id.user_role_title);
        inputEmail = findViewById(R.id.edit_text_register_email);
        inputEmail.setText(email);
        dob = findViewById(R.id.date_of_birth);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show a calendar when clicked
                mDatePickerDialog.show();
            }
        });
        btnRegister = findViewById(R.id.btnRegister);
        radioGroupUserRole = findViewById(R.id.radioGroupUserRole);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);



        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            //get passed intent
            //check if the admin is here to edit the profile
            userEditing = getIntent().getStringExtra("editor");
            if(userEditing != null && userEditing.equals("editor")){
                //get the details to edit
                editUserId = getIntent().getIntExtra("userId",0);
                editUserRole = getIntent().getIntExtra("role",0);
                editDob = getIntent().getStringExtra("dob");
                editEmail = getIntent().getStringExtra("email");
                editEmpName = getIntent().getStringExtra("empName");
                populateViews(editEmpName, editDob, editEmail, editUserRole);
            }else{
                // User is already logged in. Take him to main activity
                Intent intent = new Intent(RegisterActivity.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }

        }

        // Register Button Click event
        //get the info the user has typed in displaying errors where necessary
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //if the user is editing
                if(userEditing != null && userEditing.equals("editor")){
                    int radioButtonId = radioGroupUserRole.getCheckedRadioButtonId();
                    userRoleRadio = radioGroupUserRole.findViewById(radioButtonId);

                    String birth_date = dob.getText().toString().trim();
                    if (TextUtils.isEmpty(birth_date)) {
                        dob.setError("Date of birth is required");
                    }

                    if (userRoleRadio != null) {
                        selectedUserRole = (String) userRoleRadio.getText();
                        if (TextUtils.isEmpty(selectedUserRole)) {
                            userRoleRadio.setError("Please select the user role");
                        }
                    }

                    if (!selectedUserRole.isEmpty() && !birth_date.isEmpty()) {
                        if (!imageFilesList.isEmpty()){
                            updateUser(editUserId, selectedUserRole, birth_date, imageFilesList);
                        }else{
                            Toast.makeText(getApplicationContext(),
                                            "Please add ID images!", Toast.LENGTH_LONG)
                                    .show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),
                                        "Please enter all your details!", Toast.LENGTH_LONG)
                                .show();
                    }

                }else{
                    String surName = inputSurname.getText().toString().trim();

                    if (TextUtils.isEmpty(surName)) {
                        inputSurname.setError("Please enter your surname");
                    }

                    String otherName = inputOtherName.getText().toString().trim();
                    if (TextUtils.isEmpty(otherName)) {
                        inputOtherName.setError("Please enter your other name");
                    }

                    String birth_date = dob.getText().toString().trim();
                    if (TextUtils.isEmpty(birth_date)) {
                        dob.setError("Please enter your date of birth");
                    }

                    if (!surName.isEmpty() && !otherName.isEmpty()  && !birth_date.isEmpty()) {
                        String fullName = surName + " " + otherName;
                        if (!imageFilesList.isEmpty()){
                            registerUser(fullName, birth_date, imageFilesList);
                        }else{
                            Toast.makeText(getApplicationContext(),
                                            "Please enter your ID images!", Toast.LENGTH_LONG)
                                    .show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),
                                        "Please enter all your details!", Toast.LENGTH_LONG)
                                .show();
                    }
                }

            }
        });

    }//close onCreate()

    public RegisterActivity getInstance() {
        return registerActivity;
    }

    //set up the calendar to capture the user input d.o.b
    private void setUpCalendar() {
        mDatePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(year, monthOfYear, dayOfMonth);
            updateLabel();
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
    }

    //populate views for edit
    private void populateViews(String empName, String empDob,
                               String email, int role){
        String[] nameArray = empName.trim().split("\\s+");
        StringBuilder otherNameString = new StringBuilder();
        for(int i = 0; i < nameArray.length; i++){
            if(i == 0){
                inputSurname.setText(nameArray[i]);
            }else{
                otherNameString.append(" ").append(nameArray[i]);
            }
        }
        userRoleTitleTV.setVisibility(View.VISIBLE);
        radioGroupUserRole.setVisibility(View.VISIBLE);
        inputOtherName.setText(otherNameString.toString());
        inputSurname.setEnabled(false);
        inputOtherName.setEnabled(false);
        inputEmail.setText(email);
        dob.setText(empDob);
        if(role == 1){
            radioGroupUserRole.check(R.id.radioAdmin);
        }else{
            radioGroupUserRole.check(R.id.radioStaff);
        }
        btnRegister.setText("Save Edit");

    }

    //update the edit text with the selected date
    private void updateLabel(){
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        //update the edit text view with the time selected
        dob.setText(sdf.format(myCalendar.getTime()));
    }

    //setting up the recycler view adapter
    private void setImageAdapter()
    {
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        uploadImagesAdapter = new UploadImagesAdapter(this, uploadImageArrayList,this,"post_ad_details");
        recyclerView.setAdapter(uploadImagesAdapter);
        setImagePickerList();
    }

    // Add Camera and Folder in ArrayList
    public void setImagePickerList(){
        for (int i = 0; i < resImg.length; i++) {
            UploadPic imageModel = new UploadPic();
            imageModel.setResImg(resImg[i]);
            imageModel.setTitle(title[i]);
            uploadImageArrayList.add(i, imageModel);
        }
        uploadImagesAdapter.notifyDataSetChanged();
    }

    public void takePicture(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created;
        File photoFile = createImageFile();
        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else{
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);
        }
    }

    public File createImageFile() {
        // Create an image file name
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "IDIMG_" + formattedDate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File mFile = null;

            try {
                //imageFile = createImageFile();
                File storageDir = this.getExternalFilesDir(DIRECTORY_PICTURES);
                mFile = File.createTempFile(imageFileName,".jpg",storageDir);
                //Log.e(LOG_TAG,"Cam image File in try block = "+ mFile);
            }catch (Exception e){
                e.printStackTrace();
            }
            assert mFile != null;
            mPhotoFile = mFile;
            mCurrentPhotoPath = mFile.getAbsolutePath();
            //addImageToUploadList(mCurrentPhotoPath);
            //Log.e(LOG_TAG,"Abs Path = "+ mFile.getAbsolutePath());
            //mCurrentPhotoPath = file.getAbsolutePath();
            //Log.e(LOG_TAG,"Cam image File = "+ mFile);
            return mFile;
        }else {
            File storageDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            try {
                imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPhotoFile = imageFile;
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
            //addImageToUploadList(mCurrentPhotoPath);
            //Log.e(LOG_TAG,"Path = "+mCurrentPhotoPath);
            ///Log.e(LOG_TAG,"Cam image File = "+ imageFile);
            return imageFile;
        }
    }

    public void onAdImagesGot(Boolean isImagesGot, List<UploadPic> adPicsList){
        if (isImagesGot){
            uploadImageArrayList.addAll(adPicsList);
            uploadImagesAdapter.imageListForEdit(adPicsList, true);
        }else{
            Toast.makeText(registerActivity, "Failed to get Id images. Probably check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to call viewmodel method to post user reg details to database
     * */
    private void registerUser(final String fullName, final String dob,
                              ArrayList<File> imageFilesList) {

        //disable clicks on the register button during registration process
        btnRegister.setClickable(false);

        pDialog.setMessage("Registering ...");
        showDialog();

        String mysqlDOB = null;
        //convert the date coming in to the one mysql expects
        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        try{
            Date d = sdf.parse(dob);
            mysqlDOB = mysqlDateFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[imageFilesList.size()];

        try {
            for (int pos = 0; pos < imageFilesList.size(); pos++) {
                //parsing any media file
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFilesList.get(pos));

                fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                        imageFilesList.get(pos).getName(), requestBody);
            }
        }catch (Exception e){
            Log.e(TAG,"Error when parsing image list array");
            e.printStackTrace();
        }

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();
        String token = "Bearer "+session.getUserToken();

        //defining the call
        Call<Result> call = service.createUser(token, fullName, mysqlDOB, email, fileToUpload, fileToUpload.length);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                hideDialog();
                try {
                    if (response.body() != null && !response.body().getError()) {
                        hideDialog();
                        Log.d(TAG, response.body().getMessage());

                        int user_id = response.body().getUser().getUser_id();
                        Log.e(TAG, "Registered user id is " + user_id);

                        Toast toast = Toast.makeText(RegisterActivity.this,
                                "Registration successful", Toast.LENGTH_LONG);
                        toast.show();

                        Log.d(TAG, "Successful Registration");

                        // user successfully logged in
                        // Create login session
                        //create new user object
                        User user = new User();
                        user.setUser_id(response.body().getUser().getUser_id());
                        user.setEmp_id(response.body().getUser().getEmp_id());
                        user.setName(response.body().getUser().getName());
                        user.setDate_of_birth(response.body().getUser().getDate_of_birth());
                        user.setEmail(response.body().getUser().getEmail());
                        user.setRole(response.body().getUser().getRole());
                        user.setProfile_pic(response.body().getUser().getProfile_pic());
                        user.setIs_registered(response.body().getUser().getIs_registered());
                        user.setCreated_at(response.body().getUser().getCreated_at());

                        //insert user to the local db
                        loginRegisterActivityViewModel.insert(user);

                        session.createLoginSession(user.getUser_id(), user.getEmp_id(), user.getName(),
                                user.getEmail(), user.getDate_of_birth(), user.getRole(),
                                user.getProfile_pic(), user.getCreated_at());

                        Toast.makeText(RegisterActivity.this, "Welcome "+user.getName(), Toast.LENGTH_LONG).show();

                        //go and verify the user's phone
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        //intent.putExtra("user_id", user_id);
                        startActivity(intent);
                        finish();

                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "Error msg from inside response body: "+e.getMessage());
                    btnRegister.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                //Log.e(TAG, t.getMessage());
                Toast.makeText(RegisterActivity.this, "Failed to register. Please try again", Toast.LENGTH_SHORT).show();

                btnRegister.setClickable(true);
            }
        });

    }

    /**
     * Method to call viewmodel method to post user reg details to database
     * */
    private void updateUser(int userId, final String selectedRole, final String dob,
                              ArrayList<File> imageFilesList) {

        if(selectedRole.equals("Staff")){
            role = 0;
        }

        //disable clicks on the register button during registration process
        btnRegister.setClickable(false);

        pDialog.setMessage("Updating ...");
        showDialog();

        //convert the date coming in to the one mysql expects
        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        try{
            Date d = sdf.parse(dob);
            mysqlDOB = mysqlDateFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[imageFilesList.size()];

        try {
            for (int pos = 0; pos < imageFilesList.size(); pos++) {
                //parsing any media file
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFilesList.get(pos));

                fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                        imageFilesList.get(pos).getName(), requestBody);
            }
        }catch (Exception e){
            Log.e(TAG,"Error when parsing image list array");
            e.printStackTrace();
        }

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();
        String token = "Bearer "+session.getUserToken();

        //defining the call
        Call<Result> call = service.updateUser(token, userId, mysqlDOB, role, fileToUpload, fileToUpload.length);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                hideDialog();
                try {
                    if (response.body() != null && !response.body().getError()) {
                        hideDialog();
                        Log.d(TAG, response.body().getMessage());

                        Toast toast = Toast.makeText(RegisterActivity.this,
                                "Update successful", Toast.LENGTH_LONG);
                        toast.show();
                        if(editUserId == session.getUserId()){
                            session.setUserRole(role);
                            session.setUserDob(mysqlDOB);
                            loginRegisterActivityViewModel.updateUser(editUserId, role, mysqlDOB);
                        }
                        successfulUpdateAlert();

                        //TDOD if user has edited their own details update them here
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "Error msg from inside response body: "+e.getMessage());
                    btnRegister.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                //Log.e(TAG, t.getMessage());
                Toast.makeText(RegisterActivity.this, "Failed to update. Please try again", Toast.LENGTH_SHORT).show();

                btnRegister.setClickable(true);
            }
        });

    }

    //inform user of insufficient rights to edit profile
    private void successfulUpdateAlert(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        alertDialog.setTitle("Successful Update");
        alertDialog.setMessage("The employee details have been updated successfully");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        /*alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });*/
        alertDialog.show();
    }

    @Override
    public void onDeleteImageClicked(int position) {
        positionToDelete = position;
        int picId = uploadImageArrayList.get(position).getPic_id();
        if (picId > 0){
            //delete the image stored in the db
            loginRegisterActivityViewModel.deleteIDPic(picId);
            pDialog.setMessage("Removing ...");
            showDialog();
        }else{
            uploadImageArrayList.remove(position);
            uploadImagesAdapter.notifyItemRemoved(position);
            uploadImagesAdapter.notifyDataSetChanged();
        }
    }

    public void onAdPicDeleted(Boolean isPicDeleted, String msg) {
        hideDialog();
        if (isPicDeleted){
            uploadImageArrayList.remove(positionToDelete);
            uploadImagesAdapter.notifyItemRemoved(positionToDelete);
            uploadImagesAdapter.notifyDataSetChanged();
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageClicked(int position, String imagePath, String imageName) {
        if (position == 0) {
            if(checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)){
                takePicture();
            }
        } else if (position == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(checkPermission(Manifest.permission.READ_MEDIA_IMAGES, READ_MEDIA_CODE)){
                    selectAdImage();
                }
            }else{
                if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                    selectAdImage();
                }
            }
        }else{
            //show full screen image
            Log.e(TAG, "Image at Position clicked " +position);
            //mImageClickedCallback.onUploadImageClicked(imagePath, imageName);

        }
    }

    //handles user selection of image
    private void selectAdImage(){
        Intent intent = new Intent();
        //intent.putExtra("image_pos", jobImage);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for our request code
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e(TAG, "URI when getClipData not null: " + uri);
                        getFileToAdd(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    Log.e(TAG, "URI when getData not null: " + uri);
                    getFileToAdd(uri);
                }
            } else if (requestCode == REQUEST_CAMERA_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    addImageToUploadList(mCurrentPhotoPath, mPhotoFile);
                    //saveJobPic(mPhotoFile);
                    isCamera = true;
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            //the user cancelled the operation
        }
    }

    private void getFileToAdd(Uri uri){
        Cursor returnCursor = this.getContentResolver().query(uri,null,
                null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        createTempFile(name, uri);
        Log.e(TAG, "Name of file = "+name);
        //return name;
    }

    //creating a temp file
    private File createTempFile(String name,Uri uri){
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "ADIMG_" + formattedDate;
        File file = null;
        try {
            File storageDir = this.getExternalFilesDir(DIRECTORY_PICTURES);
            file = File.createTempFile(imageFileName, ".jpg", storageDir);
        }catch (IOException e){
            e.printStackTrace();
        }
        saveContentToFile(uri, file);
        return file;
    }

    //save the temp file using Okio
    private void saveContentToFile(Uri uri, File file){
        try {
            InputStream stream = this.getContentResolver().openInputStream(uri);
            BufferedSource source = Okio.buffer(Okio.source(stream));
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(source);
            sink.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        imageFile = file;
        imageUri = uri;
        addImageToUploadList(String.valueOf(imageUri), file);
    }

    private void addImageToUploadList(String filePath, File file){
        imageFilesList.add(file);
        imageModel = new UploadPic();
        imageModel.setImage(filePath);
        imageModel.setImageFile(file);
        imageModel.setSelected(false);
        uploadImageArrayList.add(imageModel);
        uploadImagesAdapter.refreshImageList(uploadImageArrayList);
    }

    public void onAdPostedUpdated(Boolean isAdPosted, String message) {
        hideDialog();
        if (isAdPosted) {
            //Log.e(LOG_TAG, "Ad posted successfully ");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            this.finish();
        }else {
            //Log.e(LOG_TAG, "Ad not posted");
            Toast.makeText(this, "Something went wrong. \n "+message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onImageLongClicked(int position) {

    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            //Toast.makeText(getActivity(), "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
