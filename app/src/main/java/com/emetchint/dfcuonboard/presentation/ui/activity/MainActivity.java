package com.emetchint.dfcuonboard.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.presentation.ui.fragment.StaffFragment;
import com.emetchint.dfcuonboard.presentation.ui.fragment.StatisticsFragment;
import com.emetchint.dfcuonboard.presentation.ui.fragment.UserProfileFragment;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        StaffFragment.OnEmployeeListInteractionListener, UserProfileFragment.OnLogOutClicked,
UserProfileFragment.OnChangeThemeClicked, UserProfileFragment.OnEditProfileClicked{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_FRAGMENT_PROFILE = "tag_frag_profile";
    private static final String TAG_FRAGMENT_STAFF = "tag_frag_staff";
    private static final String TAG_FRAGMENT_ANALYTICS = "tag_frag_analytics";
    private BottomNavigationView bottomNavigationView;
    private List<Fragment> fragments = new ArrayList<>(3);
    private Toolbar toolbar;
    private SessionManager session;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private static MainActivity mainActivity;
    private ProgressDialog pDialog;
    private int userId,currentFragment;
    private SharedPreferences prefs;
    private static boolean isAppThemeChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, (ViewModelProvider.Factory) factory).get(LoginRegisterActivityViewModel.class);

        // session manager
        session = new SessionManager(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //set current them to dark
        if (session.getCurrentTheme() != null) {
            Log.e("MainActivity", "Current theme is "+session.getCurrentTheme());
            if(session.getCurrentTheme().equals("Light")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else if(session.getCurrentTheme().equals("Dark")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }else{
            session.saveCurrentTheme("Light");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // session manager
        session = new SessionManager(getApplicationContext());
        userId = session.getUserId();

        setupToolbar();

        setUpBottomNavigation();

        buildFragmentList();

        /*if (isAppThemeChange){
            currentFragment = 0;
            switchFragment(0, TAG_FRAGMENT_PROFILE);
            isAppThemeChange = false;
        }else{
            //set the 0th Fragment to be displayed by default
            currentFragment = 0;
            switchFragment(0, TAG_FRAGMENT_PROFILE);
        }*/
        switchFragment(0, TAG_FRAGMENT_PROFILE);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.contentHomeFrame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottom_navigation);

                if(bottomNavigationView.getSelectedItemId () != R.id.navigation_profile){
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                }
                else {
                    finish();
                }
            }
        });
    }

    public static MainActivity getActivityInstance(){
        return mainActivity;
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    //save the current fragment position of the bottom nav
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragPos", currentFragment);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentFragment = savedInstanceState.getInt("currentFragPos");
        switch (currentFragment){
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                break;
            case 1:
                bottomNavigationView.setSelectedItemId(R.id.navigation_staff);
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.navigation_analytics);
                break;
        }
    }

    /*@Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottom_navigation);

        if(bottomNavigationView.getSelectedItemId () != R.id.navigation_profile){
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        }
        else {
            super.onBackPressed();
            this.finish();
        }
    }*/

    //method to setup bottom navigation
    private void setUpBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setUpNavigationContent(bottomNavigationView);
    }

    private void setUpNavigationContent(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_profile){
                    currentFragment = 0;
                    switchFragment(0, TAG_FRAGMENT_PROFILE);

                }else if(itemId == R.id.navigation_staff){
                    currentFragment = 1;
                    switchFragment(1, TAG_FRAGMENT_STAFF);

                }else if(itemId == R.id.navigation_analytics){
                    currentFragment = 2;
                    switchFragment(2, TAG_FRAGMENT_ANALYTICS);
                }
                return false;
            }
        });
        /*bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        return false;
                    }
                });*/
    }

    private void switchFragment(int pos, String tag) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentHomeFrame, fragments.get(pos), tag)
                .commit();
    }

    //method to build list of fragments
    private void buildFragmentList() {
        UserProfileFragment userProfileFragment = buildUserProfileFragment();
        StaffFragment staffFragment = buildStaffFragment();
        StatisticsFragment statisticsFragment = buildStatisticsFragment();

        fragments.add(userProfileFragment);
        fragments.add(staffFragment);
        fragments.add(statisticsFragment);
    }

    private UserProfileFragment buildUserProfileFragment() {
        UserProfileFragment fragment;
        fragment = UserProfileFragment.newInstance();
        return fragment;
    }

    //build the fragment
    private StaffFragment buildStaffFragment() {
        StaffFragment fragment;
        fragment = StaffFragment.newInstance();

        return fragment;
    }

    //build the fragment
    private StatisticsFragment buildStatisticsFragment() {
        StatisticsFragment fragment;
        fragment = StatisticsFragment.newInstance("Analytics", "userId");
        return fragment;
    }

    @Override
    public void onEmployeeInteraction(int userId, String empId, String empName, String dob,
                                      String email, int role, String profilePic, String regDate) {
        //if current user is admin go and edit these employee details
        if(session.getUserRole() == 1){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.putExtra("editor","editor");
            intent.putExtra("userId",userId);
            intent.putExtra("empId",empId);
            intent.putExtra("empName",empName);
            intent.putExtra("dob",dob);
            intent.putExtra("email",email);
            intent.putExtra("role",role);
            intent.putExtra("profilePic",profilePic);
            intent.putExtra("regDate",regDate);
            startActivity(intent);
        }else{
            informUserRights();
        }

    }

    @Override
    public void onLogOutClick() {
        confirmLogoutDialog();
    }

    @Override
    public void onChangeThemeClick() {

        if (session.getCurrentTheme() != null) {
            if(session.getCurrentTheme().equals("Light")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else if(session.getCurrentTheme().equals("Dark")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }else{
            session.saveCurrentTheme("Dark");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    @Override
    public void goToEditProfileClick() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.putExtra("editor","owner");
        startActivity(intent);
    }

    //inform user of insufficient rights to edit profile
    private void informUserRights(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        alertDialog.setTitle("Insufficient rights");
        alertDialog.setMessage("Your current role is not able to edit staff details. " +
                "Contact admin for more information");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

    //user confirm log out
    private void confirmLogoutDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        alertDialog.setTitle("Log Out");
        alertDialog.setMessage("Are you sure you want to log out?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //clear from prefs
                //delete user from db
                pDialog.setMessage("Logging Out ...");
                showDialog();
                //deletes user from sqlite db - room
                loginRegisterActivityViewModel.delete();
                session.clearPrefs();

                Intent i = new Intent(MainActivity.this, EmailVerificationActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Add new Flag to start new Activity
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Starting Login Activity
                startActivity(i);
                finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
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
}