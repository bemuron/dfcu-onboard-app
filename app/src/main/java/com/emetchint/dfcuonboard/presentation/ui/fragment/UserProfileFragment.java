package com.emetchint.dfcuonboard.presentation.ui.fragment;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.helpers.CircleTransform;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.IdPic;
import com.emetchint.dfcuonboard.models.User;
import com.emetchint.dfcuonboard.presentation.adapters.IdPicsAdapter;
import com.emetchint.dfcuonboard.presentation.ui.activity.EmailVerificationActivity;
import com.emetchint.dfcuonboard.presentation.ui.activity.MainActivity;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment implements IdPicsAdapter.IdPicsAdapterListener {
    private static final String LOG_TAG = UserProfileFragment.class.getSimpleName();

    public static UserProfileFragment profileFragment;
    private SessionManager session;
    private String name, email, profile_pic,dob,empId,
            created_on, imageFilePath = "non", profilePicEdited;
    private int userId,userRole;
    private TextView userNameTv, registerDateTv, emailTv, dobTv, roleTv, empIdTv;
    private RecyclerView idImagesRV;
    private ImageView userProfilePicIv;
    private IdPicsAdapter idPicsAdapter;
    private List<IdPic> idPicList = new ArrayList<IdPic>();;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private OnLogOutClicked mListener;
    private OnChangeThemeClicked onChangeThemeClicked;
    private OnEditProfileClicked onEditProfileClicked;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        profileFragment = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogOutClicked) {
            mListener = (OnLogOutClicked) context;
            onChangeThemeClicked = (OnChangeThemeClicked) context;
            onEditProfileClicked = (OnEditProfileClicked) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UserProfileFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        getAllWidgets(view);

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(getActivity().getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        // session manager
        session = new SessionManager(getActivity());
        if (!session.isLoggedIn()) {
            showLoginNoticeDialog();
        }else{
            userId = session.getUserId();
            userRole = session.getUserRole();

            HashMap<String, String> user = session.getUserDetails();
            name = user.get("name");
            email = user.get("email");
            profile_pic = user.get("profile_pic");
            created_on = user.get("created_on");
            dob = user.get("dob");
            empId = user.get("empId");
        }
        //get the id images
        loginRegisterActivityViewModel.getIdPics(userId).observe(getActivity(), idPics -> {
            idPicList = idPics;
            if (idPicList != null){
                idPicsAdapter.refreshImageList(idPicList);
            }else{
                setUpIdPicsAdapter();
            }
        });
        setUpIdPicsAdapter();
        populateViews();
        return view;
    }

    private void getAllWidgets(View view){
        idImagesRV = view.findViewById(R.id.profile_id_images_rv);
        userProfilePicIv = view.findViewById(R.id.profile_pic);
        userProfilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get image to upload
            }
        });
        userNameTv = view.findViewById(R.id.profile_user_name);
        registerDateTv = view.findViewById(R.id.profile_member_since);
        dobTv = view.findViewById(R.id.profile_dob_value_tv);
        emailTv = view.findViewById(R.id.profile_email_value_tv);
        roleTv = view.findViewById(R.id.profile_role_value_tv);
        empIdTv = view.findViewById(R.id.profile_emp_number_tv);
        idImagesRV = view.findViewById(R.id.profile_id_images_rv);

    }

    //set up the list adapter to handle the images the user adds for upload
    private void setUpIdPicsAdapter(){
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(getActivity(), 2);
        idImagesRV.setLayoutManager(layoutManager);
        idPicsAdapter = new IdPicsAdapter(getActivity(), idPicList,"my_profile",this);
        idImagesRV.setAdapter(idPicsAdapter);
    }

    //method to handle population of the views with the content
    private void populateViews() {
        userNameTv.setText(name);
        emailTv.setText(email);
        empIdTv.setText(empId);
        if(userRole == 1){
            roleTv.setText(R.string.admin_role);
        }else{
            roleTv.setText(R.string.staff_role);
        }
        formatDate();

        //set user profile pic
        try {
            if (profile_pic != null) {
                userProfilePicIv.setImageDrawable(null);
                Glide.with(getActivity())
                        .load("https://www.emtechint.com/dfcu/public/assets/images/profile_pics/" + profile_pic)
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .apply(new RequestOptions().fitCenter().timeout(6000)
                                .transform(new CircleTransform(getActivity())).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(userProfilePicIv);
                userProfilePicIv.setColorFilter(null);
            } else {
                userProfilePicIv.setImageResource(R.drawable.img_profile_layer);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not load image");
            e.printStackTrace();
        }
    }

    //method to convert from the mysql date format to a more readable one
    private void formatDate(){
        String memberSince = null;
        String finalDob = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.ENGLISH);

        try{
            Date date = mysqlDateTimeFormat.parse(created_on);
            memberSince = sdf.format(date);

        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Date dobDate = mysqlDateFormat.parse(dob);
            finalDob = sdf.format(dobDate);
            Log.e(LOG_TAG, "DOB in try "+finalDob);
        }catch (Exception e){
            e.printStackTrace();
        }

        registerDateTv.setText(memberSince);
        dobTv.setText(finalDob);
    }

    public interface OnLogOutClicked {
        void onLogOutClick();
    }

    public interface OnChangeThemeClicked {
        void onChangeThemeClick();
    }

    public interface OnEditProfileClicked {
        void goToEditProfileClick();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // clear all current menu items
        //menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.my_profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit_profile){
            //go to edit profile
            onEditProfileClicked.goToEditProfileClick();
        }else if (id == R.id.action_change_theme){
            //change the theme
            onChangeThemeClicked.onChangeThemeClick();
        }else if (id == R.id.action_log_out){
            //log out the user
            mListener.onLogOutClick();
        }

        return super.onOptionsItemSelected(item);
    }

    //prompt user to login/register if not yet
    private void showLoginNoticeDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogTheme);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Login or Register");
        alertDialog.setMessage("Login or register to be able to see your profile");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //session.logoutUser();
                Intent i = new Intent(getActivity(), EmailVerificationActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Add new Flag to start new Activity
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Starting Login Activity
                startActivity(i);
                getActivity().finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onImageClicked(int position, String imageName) {

    }
}