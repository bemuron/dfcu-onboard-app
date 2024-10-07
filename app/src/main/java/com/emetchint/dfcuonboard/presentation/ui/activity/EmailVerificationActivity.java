package com.emetchint.dfcuonboard.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EmailVerificationActivity extends AppCompatActivity implements
        LoginUser.ValidationCodeSentCallBack {
    private static final String TAG = EmailVerificationActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    public static EmailVerificationActivity emailVerificationActivity;
    private Button btnValidate;
    private EditText inputEmail;
    private ProgressDialog pDialog;
    private SessionManager session;
    private User user;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        emailVerificationActivity = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        inputEmail = (EditText) findViewById(R.id.edit_text_validate_email);
        btnValidate = (Button) findViewById(R.id.validate_email_btn);

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
            Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Validate button Click Event
        btnValidate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //first check if we have an internet connection
                if (isNetworkAvailable()) {
                    //validate user input email and
                    //attempt to validate on server
                    attemptEmailValidation();
                }else {
                    // show user that they may not be having an internet connection
                    Toast.makeText(getApplicationContext(),
                            "Something is not right, try checking your internet connection.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

    }//close

    public static EmailVerificationActivity getInstance() {
        return emailVerificationActivity;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptEmailValidation() {

        // Reset errors.
        inputEmail.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();

        //check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast toast = Toast.makeText(this, "Email is required",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isEmailValid(email)) {
            Toast toast = Toast.makeText(this, "This email address is invalid",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt validate and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //super admin to add user emails
            if (email.equals("admin@email.com")){
                Intent intent = new Intent(EmailVerificationActivity.this, AddUserActivity.class);
                startActivity(intent);
            }else{
                pDialog.setMessage("Validating. Please wait ...");
                showDialog();

                //Calls viewmodel method
                loginRegisterActivityViewModel.validateUserEmail(email);
            }

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
    public void onCodeSent(Boolean isCodeSent, String message) {
        hideDialog();
        if (isCodeSent){
            pDialog.setMessage(message);
            showDialog();
            Toast.makeText(EmailVerificationActivity.this, message,
                    Toast.LENGTH_LONG).show();
            btnValidate.setClickable(true);
            //go to code verification
            Intent intent = new Intent(EmailVerificationActivity.this, CodeVerificationActivity.class);
            startActivity(intent);
            //EmailVerificationActivity.this.finish();
        }else{
            this.hideDialog();
            Log.d(TAG, "Validation code not sent");
            pDialog.setMessage(message);
            showDialog();
            Toast.makeText(EmailVerificationActivity.this, message,
                    Toast.LENGTH_LONG).show();
            btnValidate.setClickable(true);
        }
    }

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
}
