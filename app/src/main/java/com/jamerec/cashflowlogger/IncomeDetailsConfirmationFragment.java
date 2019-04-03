package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailsConfirmationFragment extends Fragment
        implements View.OnClickListener {

    private final static String TAG = "DetailsConfirmFragment";

    private RecyclerView mCategoriesRV;
    private Button mBtnLog;
    private Button mBtnEdit;

    private Context mContext;
    private ArrayList<FundItem> mFundList;
    private OnConfirmIncomeLogListener logListener;

    public IncomeDetailsConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnConfirmIncomeLogListener) {
            logListener = (OnConfirmIncomeLogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnConfirmIncomeLogListener.confirmIncomeLog");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_income_details_confirmation, container, false);

        mContext = getContext();

        // Setup display
        String incomeSource = getArguments().getString("incomeSource");
        PhCurrency incomeAmount = getArguments().getParcelable("incomeAmount");
        TextView incomeSourceTV = view.findViewById(R.id.disp_source);
        TextView incomeAmountTV = view.findViewById(R.id.disp_amount);
        incomeSourceTV.setText(incomeSource);
        incomeAmountTV.setText(incomeAmount.toString());

        // Populate allocation list
        mFundList = getArguments().getParcelableArrayList("fundAllocation");
        mCategoriesRV = view.findViewById(R.id.list_funds);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(mContext);
        mCategoriesRV.setLayoutManager(layoutMgr);
        FundListAdapterRV adapter = new FundListAdapterRV(mContext, mFundList, null);
        mCategoriesRV.setAdapter(adapter);

        mBtnLog = view.findViewById(R.id.btn_allocate);
        mBtnEdit= view.findViewById(R.id.btn_edit);

        mBtnLog.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int btnID = view.getId();
        logListener.confirmIncomeLog(btnID);
    }

    public interface OnConfirmIncomeLogListener {
        void confirmIncomeLog(int buttonID);
    }
}
