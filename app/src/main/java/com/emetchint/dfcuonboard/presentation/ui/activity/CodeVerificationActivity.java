package com.emetchint.dfcuonboard.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.data.network.LoginUser;
import com.emetchint.dfcuonboard.helpers.InputValidator;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.User;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;

public class CodeVerificationActivity extends AppCompatActivity implements
        LoginUser.SuccessfulCodeVerificationCallBack {
    private static final String TAG = CodeVerificationActivity.class.getSimpleName();
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    public static CodeVerificationActivity codeVerificationActivity;
    private Button btnVerify;
    private EditText inputCode;
    private ProgressDialog pDialog;
    private SessionManager session;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification);
        codeVerificationActivity = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        inputCode = findViewById(R.id.edit_text_verify_code);
        btnVerify = findViewById(R.id.verify_code_btn);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        // Session manager
        session = new SessionManager(getApplicationContext());
        session.saveCurrentTheme("Light");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Validate button Click Event
        btnVerify.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //first check if we have an internet connection
                if (isNetworkAvailable()) {
                    //attempt to verify 10 digit code on server
                    attemptCodeVerification();
                }else {
                    // show user that they may not be having an internet connection
                    Toast.makeText(getApplicationContext(),
                            "Something is not right, try checking your internet connection.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

    }//close

    public static CodeVerificationActivity getInstance() {
        return codeVerificationActivity;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptCodeVerification() {

        // Reset errors.
        inputCode.setError(null);

        // Store values at the time of the login attempt.
        String code = inputCode.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();

        //check if email field is empty
        if (TextUtils.isEmpty(code)) {
            Toast toast = Toast.makeText(this, "The code is required",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputCode;
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isCodeValid(code)) {
            Toast toast = Toast.makeText(this, "The code should be 10 digits",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputCode;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt validate and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            pDialog.setMessage("Verifying. Please wait ...");
            showDialog();

            //Calls viewmodel method
            loginRegisterActivityViewModel.verifyUserCode(code);
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

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //@Override
    public void onCodeVerified(Boolean isCodeVerified, User user, String message) {
        hideDialog();
        if (isCodeVerified){
            if (user.getIs_registered() == 0){
                this.hideDialog();
                Log.d(TAG, "Code verified. Proceed to register");
                pDialog.setMessage(message);
                showDialog();

                Toast.makeText(CodeVerificationActivity.this, message,
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(CodeVerificationActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }else{
                Log.d(TAG, "Successful login");
                //insert user to the local db
                loginRegisterActivityViewModel.insert(user);

                // user successfully logged in
                // Create login session
                session.createLoginSession(user.getUser_id(), user.getEmp_id(), user.getName(),
                        user.getEmail(), user.getDate_of_birth(), user.getRole(),
                        user.getProfile_pic(), user.getCreated_at());

                Toast.makeText(CodeVerificationActivity.this, "Welcome "+user.getName(), Toast.LENGTH_LONG).show();

                //go to main activity
                Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }else{
            this.hideDialog();
            pDialog.setMessage(message);
            showDialog();
            Toast.makeText(CodeVerificationActivity.this, message,
                    Toast.LENGTH_LONG).show();
            btnVerify.setClickable(true);
        }
    }
}
