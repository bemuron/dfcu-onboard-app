package com.emetchint.dfcuonboard.presentation.ui.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegisterActivityViewModel;
import com.emetchint.dfcuonboard.presentation.viewmodel.LoginRegistrationViewModelFactory;
import com.emetchint.dfcuonboard.utilities.InjectorUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {
    private static final String LOG_TAG = StaffFragment.class.getSimpleName();
    private LoginRegisterActivityViewModel mViewModel;
    public static StatisticsFragment statisticsFragment;
    private EditText logSearchET;
    private Button btnRetrieve;
    private TextView totalReqTv, regReqTv,retReqTv,updateReqTv;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatisticsFragment.
     */
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
        statisticsFragment = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("API Performance");
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getAllWidgets(view);

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(getActivity().getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(LoginRegisterActivityViewModel.class);
        return view;
    }

    private void getAllWidgets(View view){
        totalReqTv = view.findViewById(R.id.total_requests_value);
        regReqTv = view.findViewById(R.id.reg_api_value_tv);
        retReqTv = view.findViewById(R.id.retrieval_api_value);
        updateReqTv = view.findViewById(R.id.update_api_title);

        logSearchET = view.findViewById(R.id.edit_text_log_search);
        btnRetrieve = view.findViewById(R.id.search_log_btn);
        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String logDate = logSearchET.getText().toString().trim();
                String mysqlDate = null;
                //convert the date coming in to the one mysql expects
                SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

                try{
                    Date d = sdf.parse(logDate);
                    mysqlDate = mysqlDateFormat.format(d);
                }catch (Exception e){
                    e.printStackTrace();
                }
                fetchLogs(mysqlDate);
            }
        });
    }

    private void fetchLogs(String empId){
        /*mViewModel.getApiLogs(empId).(getActivity(), userJobsList -> {
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
        });*/

    }
}