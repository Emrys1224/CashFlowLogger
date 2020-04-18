package com.jamerec.cashflowlogger;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllocationSettingFragment
        extends Fragment
        implements FundListAdapter.FundItemClickListener, FundSettingDialog.FundSetListener {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private CFLoggerOpenHelper mDB;
    private List<FundAllocationPercentage> mAllocationDB;       // Contains all the funds DB including inactive funds
    private List<FundAllocationPercentage> mActiveAllocation;   // Contains only the active funds

    // Display widgets
    private RecyclerView mAllocationRV;
    private TextView mTotalPercentageTV;
    private TextView mErrAllocationTV;

    // Button widgets
    private Button mSaveBtn;
    private Button mCancelBtn;

    public AllocationSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allocation_setting, container, false);

        mContext = getContext();
        mDB = new CFLoggerOpenHelper(mContext);
        mAllocationDB = mDB.getFundsAllocationPercentage();
        mActiveAllocation = new ArrayList<>();
        List<String> fundNameList = new ArrayList<>();     // to be used as suggestion item for fund name input

        for (FundAllocationPercentage fundAllocation : mAllocationDB) {
            // Active funds only
            if (fundAllocation.getPercentAllocation() > 0)
                mActiveAllocation.add(fundAllocation);

            // Add all fund names including the inactive funds
            fundNameList.add(fundAllocation.getFundName());
        }

        // Initialize display widgets
        mAllocationRV = view.findViewById(R.id.disp_allocation);
        mTotalPercentageTV = view.findViewById(R.id.disp_total_percentage);
        mErrAllocationTV = view.findViewById(R.id.err_allocation);

        // Initialize button widgets
        mSaveBtn = view.findViewById(R.id.btn_save);
        mCancelBtn = view.findViewById(R.id.btn_cancel);

        // Fund allocation percentage settings
        LinearLayoutManager allocationLayoutMgr = new LinearLayoutManager(mContext);
        mAllocationRV.setLayoutManager(allocationLayoutMgr);
        FundListAdapter adapter = new FundListAdapter(
                mContext, mActiveAllocation, this, R.layout.list_item_allocation_percentage);
        mAllocationRV.setAdapter(adapter);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onFundItemClicked(View v, int position) {
        String fundName = mActiveAllocation.get(position).getFundName();
        int percentage = mActiveAllocation.get(position).getPercentAllocation();

        FundSettingDialog fundSettingDialog = new FundSettingDialog();
        fundSettingDialog.show(
                Objects.requireNonNull(getFragmentManager()), "Tag");
    }

    @Override
    public void setFundAllocation() {

    }
}
