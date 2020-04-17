package com.jamerec.cashflowlogger;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllocationSettingFragment extends Fragment {

    private Context mContext;
    private List mAllocation;
    private RecyclerView mAllocationRV;
    private CFLoggerOpenHelper mDB;

    public AllocationSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allocation_setting, container, false);

        mContext = getContext();
        mDB = new CFLoggerOpenHelper(mContext);
        mAllocation = mDB.getFundsAllocationPercentage();

        mAllocationRV = view.findViewById(R.id.disp_allocation);

        // Fund allocation percentage settings
        LinearLayoutManager allocationLayoutMgr = new LinearLayoutManager(mContext);
        mAllocationRV.setLayoutManager(allocationLayoutMgr);
        FundListAdapter adapter = new FundListAdapter(
                mContext, mAllocation, null, R.layout.list_item_allocation_percentage);
        mAllocationRV.setAdapter(adapter);

        return view;
    }
}
