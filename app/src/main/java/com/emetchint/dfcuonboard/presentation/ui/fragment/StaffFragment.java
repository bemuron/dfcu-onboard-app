package com.emetchint.dfcuonboard.presentation.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.app.MyApplication;
import com.emetchint.dfcuonboard.helpers.SessionManager;
import com.emetchint.dfcuonboard.models.User;
import com.emetchint.dfcuonboard.presentation.adapters.EmployeeListAdapter;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StaffFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StaffFragment extends Fragment implements EmployeeListAdapter.EmployeeListAdapterListener {
    private static final String LOG_TAG = StaffFragment.class.getSimpleName();
    private TextView emptyView;
    private ProgressBar progressBar;
    private SessionManager session;
    private List<User> employeeList = new ArrayList<User>();
    private SwipeRefreshLayout swipeRefreshLayout;
    public static StaffFragment staffFragment;
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private EditText empSearchET;
    private Button btnRetrieve;
    private EmployeeListAdapter employeeListAdapter;
    private OnEmployeeListInteractionListener mListener;
    private LoginRegisterActivityViewModel mViewModel;

    public StaffFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StaffFragment.
     */
    public static StaffFragment newInstance() {
        StaffFragment fragment = new StaffFragment();
        staffFragment = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Employees");
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
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Employees");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // session manager
        session = new SessionManager(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_staff, container, false);
        getAllWidgets(view);
        setAdapter();

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(getActivity().getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);

        fetchAllStaff("0");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEmployeeListInteractionListener) {
            mListener = (OnEmployeeListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StaffFragment");
        }
    }

    private void getAllWidgets(View view){
        swipeRefreshLayout = view.findViewById(R.id.my_tasks_list_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!MyApplication.isNetworkAvailable(getActivity().getApplicationContext())){
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
                    hideBar();
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    refreshTasksList();
                }
            }
        });

        // Progress bar
        progressBar = view.findViewById(R.id.my_jobs_progress_bar);
        showBar();

        recyclerView = view.findViewById(R.id.my_jobs_recycler_view);
        emptyView = view.findViewById(R.id.empty_jobs_list_view);

        empSearchET = view.findViewById(R.id.edit_text_emp_search);
        btnRetrieve = view.findViewById(R.id.search_emp_btn);
        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String empId = empSearchET.getText().toString().trim();
                fetchAllStaff(empId);
            }
        });
    }

    public interface OnEmployeeListInteractionListener {
        void onEmployeeInteraction(int userId, String empId, String empName, String dob,
                                   String email, int role, String profilePic, String regDate);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        employeeListAdapter = new EmployeeListAdapter(getActivity(), employeeList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(employeeListAdapter);
    }

    private void fetchAllStaff(String empId){
        mViewModel.getAllEmployees(empId).observe(getActivity(), userJobsList -> {
            employeeList = userJobsList;
            if (employeeList != null){
                employeeListAdapter.setList(userJobsList);

                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            }else{
                emptyView.setText(R.string.empty_all_staff_list);
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            swipeRefreshLayout.setRefreshing(false);
            hideBar();
        });

    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        /*getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void refreshTasksList() {
        fetchAllStaff("0");
    }

    private void displayEmptyListMessage() {
        emptyView.setText(R.string.empty_all_staff_list);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onEmployeeRowClicked(int position) {
        User employee = employeeList.get(position);
        employeeList.set(position, employee);
        if (mListener != null) {
            //send to the parent activity then call the activity to display details
            mListener.onEmployeeInteraction(employee.getUser_id(), employee.getEmp_id(),
                    employee.getName(), employee.getDate_of_birth(),employee.getEmail(),employee.getRole(),
                    employee.getProfile_pic(), employee.getCreated_at());
        }
    }
}