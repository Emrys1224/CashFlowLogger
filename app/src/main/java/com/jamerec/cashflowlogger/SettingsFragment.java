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
public class SettingsFragment extends Fragment {

    private Context mContext;
    private CFLoggerOpenHelper mDB;
    private RecyclerView mAllocationRV;
    private List<FundAllocationPercentage> mAllocation;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mContext = getContext();
        mDB = new CFLoggerOpenHelper(mContext);

        mAllocation = mDB.getFundsAllocationPercentage();

        mAllocationRV = view.findViewById(R.id.disp_allocation);
        LinearLayoutManager allocationLayoutMgr = new LinearLayoutManager(mContext);
        mAllocationRV.setLayoutManager(allocationLayoutMgr);
        FundListAdapter adapter = new FundListAdapter(mContext, mAllocation, null);
        mAllocationRV.setAdapter(adapter);

        return view;
    }
}
