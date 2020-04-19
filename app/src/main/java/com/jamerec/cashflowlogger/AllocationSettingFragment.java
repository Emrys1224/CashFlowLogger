package com.jamerec.cashflowlogger;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
        implements FundListAdapter.FundItemClickListener {

    private final String TAG = getClass().getSimpleName();

    // Constants for sending fund info to FundSettingDialog
    static final String ACTION = "action";
    static final String FUND_NAME = "fund_name";
    static final String PERCENTAGE = "percentage";

    private Context mContext;
    private CFLoggerOpenHelper mDB;
    private List<FundAllocationPercentage> mAllocationDB;       // Contains all the funds DB including inactive funds
    private List<FundAllocationPercentage> mActiveAllocation;   // Contains only the active funds

    // Display widgets
    private RecyclerView mAllocationRV;
    private TextView mTotalPercentageTV;
    private TextView mErrAllocationTV;

    // Button widgets
    private TextView mAddFundBtn;
    private Button mSaveBtn;
    private Button mCancelBtn;

    private FundListAdapter mAdapter;
    private int mIndexFundSelected;

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

        for (FundAllocationPercentage fundAllocation : mAllocationDB) {
            // Active funds only
            if (fundAllocation.getPercentAllocation() > 0)
                mActiveAllocation.add(fundAllocation);
        }

        // Initialize display widgets
        mAllocationRV = view.findViewById(R.id.disp_allocation);
        mTotalPercentageTV = view.findViewById(R.id.disp_total_percentage);
        mErrAllocationTV = view.findViewById(R.id.err_allocation);

        // Initialize button widgets
        mAddFundBtn = view.findViewById(R.id.btn_add_fund);
        mSaveBtn = view.findViewById(R.id.btn_save);
        mCancelBtn = view.findViewById(R.id.btn_cancel);

        // Fund allocation percentage settings
        LinearLayoutManager allocationLayoutMgr = new LinearLayoutManager(mContext);
        mAllocationRV.setLayoutManager(allocationLayoutMgr);
        mAdapter = new FundListAdapter(
                mContext, mActiveAllocation, this, R.layout.list_item_allocation_percentage);
        mAllocationRV.setAdapter(mAdapter);

        // Plus icon for 'Add a Fund' button
        mAddFundBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add,0,0,0);
        mAddFundBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                mIndexFundSelected = -1;

                FundSettingDialog fundSettingDialog = new FundSettingDialog();
                fundSettingDialog.show(
                        Objects.requireNonNull(getFragmentManager()), "Tag");
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onFundItemClicked(View v, int position) {

        // Remove this fund allocation
        if (v.getId() == R.id.btn_delete) {
            mIndexFundSelected = -1;
            return;
        }

        mIndexFundSelected = position;

        // Details of fund to edit
        String fundName = mActiveAllocation.get(position).getFundName();
        int percentage = mActiveAllocation.get(position).getPercentAllocation();
        Bundle fundInfo = new Bundle();
        fundInfo.putString(FUND_NAME, fundName);
        fundInfo.putInt(PERCENTAGE, percentage);

        FundSettingDialog fundSettingDialog = new FundSettingDialog();
        fundSettingDialog.setArguments(fundInfo);
        fundSettingDialog.show(
                Objects.requireNonNull(getFragmentManager()), "Tag");
    }

    public void updateFund(String fundName, int allocationPercentage) {
        Log.d(TAG, "Fund Name: " + fundName + "\nPercent Allocation: " + allocationPercentage);

        if (mIndexFundSelected >= 0) {
            FundAllocationPercentage fundSetting = mActiveAllocation.get(mIndexFundSelected);
            fundSetting.updatePercentAllocation(allocationPercentage);

        } else {
            mActiveAllocation.add(new FundAllocationPercentage(fundName, allocationPercentage));
        }

        mAdapter.notifyDataSetChanged();

        int percentTotal = 0;
        for (FundAllocationPercentage fundAllocation : mActiveAllocation) {
            percentTotal += fundAllocation.getPercentAllocation();
        }

        mTotalPercentageTV.setText(String.format("%s%%", String.valueOf(percentTotal)));

        if (percentTotal == 100)
            mTotalPercentageTV.setTextColor(getResources().getColor(R.color.colorPrimary));
        else
            mTotalPercentageTV.setTextColor(getResources().getColor(R.color.colorAccent));
    }
}
