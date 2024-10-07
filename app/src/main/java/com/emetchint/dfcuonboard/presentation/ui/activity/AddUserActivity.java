package com.emetchint.dfcuonboard.presentation.ui.activity;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emetchint.dfcuonboard.BuildConfig;
import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.data.network.Result;
import com.emetchint.dfcuonboard.data.network.api.APIService;
import com.emetchint.dfcuonboard.data.network.api.LocalRetrofitApi;
import com.emetchint.dfcuonboard.helpers.InputValidator;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserActivity extends AppCompatActivity{
    private static final String TAG = AddUserActivity.class.getSimpleName();
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private Button btnRegister;
    private EditText inputEmail;
    private ProgressDialog pDialog;
    private SessionManager session;
    private RadioGroup radioGroupUserRole;
    private RadioButton userRoleRadio;
    private Uri imageUri;
    private String selectedUserRole;
    int editUserId, editUserRole, role = 1;
    public static AddUserActivity registerActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        registerActivity = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        // Session manager
        session = new SessionManager(getApplicationContext());
        InputValidator inputValidator = new InputValidator();

        inputEmail = findViewById(R.id.edit_text_add_user_email);
        btnRegister = findViewById(R.id.btnAddUser);
        radioGroupUserRole = findViewById(R.id.radioGroupAddUserRole);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        //get the info the user has typed in displaying errors where necessary
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Please add an email");
                }

                int radioButtonId = radioGroupUserRole.getCheckedRadioButtonId();
                userRoleRadio = radioGroupUserRole.findViewById(radioButtonId);

                if (userRoleRadio != null) {
                    selectedUserRole = (String) userRoleRadio.getText();
                    if (TextUtils.isEmpty(selectedUserRole)) {
                        userRoleRadio.setError("Please select the user role");
                    }
                }

                if (!email.isEmpty() && !selectedUserRole.isEmpty()) {
                    if (inputValidator.isEmailValid(email)) {
                        if (isNetworkAvailable()) {
                            addUser(email, selectedUserRole);
                        }else {
                            // show user that they may not be having an internet connection
                            Toast.makeText(getApplicationContext(),
                                            "Try checking your internet connection.", Toast.LENGTH_LONG)
                                    .show();
                        }

                    }else{
                        Toast.makeText(AddUserActivity.this, "Please enter a valid email address",Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),
                                    "Please enter all your details!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

    }//close onCreate()

    public AddUserActivity getInstance() {
        return registerActivity;
    }

    /**
     * Method to call viewmodel method to add user email o database
     * */
    private void addUser(final String userEmail, final String userRole) {

        if(userRole.equals("Staff")){
            role = 0;
        }

        //disable clicks on the register button during registration process
        btnRegister.setClickable(false);

        pDialog.setMessage("Adding ...");
        showDialog();

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.addUser(userEmail, role);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                hideDialog();
                try {
                    if (response.body() != null && !response.body().getError()) {
                        hideDialog();
                        btnRegister.setClickable(true);

                        Toast toast = Toast.makeText(AddUserActivity.this,
                                "Successful add", Toast.LENGTH_LONG);
                        toast.show();
                        inputEmail.setText("");
                        successfulUserAddAlert();

                    }
                }catch(Exception e){
                    e.printStackTrace();
                    if (response.body() != null && response.body().getMessage() != null) {
                        unSuccessfulUserAddAlert(response.body().getMessage());
                    }

                    Log.e(TAG, "Error msg from inside response body: "+e.getMessage());
                    Toast toast = Toast.makeText(AddUserActivity.this,
                            "An error occurred. Failed to add this email", Toast.LENGTH_LONG);
                    toast.show();
                    btnRegister.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                //Log.e(TAG, t.getMessage());
                unSuccessfulUserAddAlert(null);
                Toast.makeText(AddUserActivity.this, "Failed to Add. Please try again", Toast.LENGTH_SHORT).show();

                btnRegister.setClickable(true);
            }
        });

    }

    //inform user of successful
    private void successfulUserAddAlert(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        alertDialog.setTitle("Successful Add");
        alertDialog.setMessage("Staff email and role have been saved successfully. The staff can now self on board");
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

    private void unSuccessfulUserAddAlert(String message){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        if (message == null){
            message = "An error occurred during the add. Please try again later";
        }
        alertDialog.setTitle("Failed Add");
        alertDialog.setMessage(message);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
