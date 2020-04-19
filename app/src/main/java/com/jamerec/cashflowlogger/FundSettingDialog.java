package com.jamerec.cashflowlogger;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

public class FundSettingDialog
        extends AppCompatDialogFragment
        implements View.OnClickListener {

    private boolean mIsActionEdit;

    private TextView mFundNameTV;
    private EditText mFundNameET;
    private EditText mPercentageET;
    private TextView mErrAllocationTV;
    private FundSetListener mListener;

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build view
        AlertDialog.Builder fundDialogBuilder =
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater fundDialogInflater = getActivity().getLayoutInflater();
        View view = fundDialogInflater.inflate(R.layout.dialog_fund_setting, null);
        fundDialogBuilder.setView(view);

        // Initialize widgets
        TextView actionTitleTV = view.findViewById(R.id.title_fund_dialog);
        mFundNameTV = view.findViewById(R.id.fund_name);
        mFundNameET = view.findViewById(R.id.input_fund_name);
        mPercentageET = view.findViewById(R.id.input_percent_allocation);
        mErrAllocationTV = view.findViewById(R.id.err_allocation);
        Button cancelBtn = view.findViewById(R.id.btn_cancel);
        Button actionBtn = view.findViewById(R.id.btn_action);

        // Get and display details for editing of the fund allocation
        Bundle fundInfo = getArguments();
        if (fundInfo != null) {
            String fundName = fundInfo.getString(AllocationSettingFragment.FUND_NAME);
            int percentage = fundInfo.getInt(AllocationSettingFragment.PERCENTAGE);

            // Set text displayed for editing
            actionTitleTV.setText(R.string.title_edit_allocation);
            mFundNameTV.setText(fundName);
            mPercentageET.setText(String.valueOf(percentage));
            actionBtn.setText(R.string.btn_apply);

            // Hide EditText and show with TextView
            mFundNameTV.setVisibility(View.VISIBLE);
            mFundNameET.setVisibility(View.INVISIBLE);

            mIsActionEdit = true;

        } else {
            // Set text displayed for adding fund
            actionTitleTV.setText(R.string.title_add_fund);
            actionBtn.setText(R.string.btn_add);

            mIsActionEdit = false;
        }

        cancelBtn.setOnClickListener(this);
        actionBtn.setOnClickListener(this);

        return fundDialogBuilder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            this.dismiss();
            return;
        }

        String fundName = mIsActionEdit ?
                mFundNameTV.getText().toString() : mFundNameET.getText().toString();
        int percentage = mPercentageET.getText().length() > 0 ?
                Integer.parseInt(mPercentageET.getText().toString()) : 0;

        // Check for error
        StringBuilder errMsg = new StringBuilder();
        if (fundName.isEmpty())
            errMsg.append(getString(R.string.err_msg_fund_name_empty));
        if (percentage <= 0 || percentage > 100)
            errMsg.append(getString(R.string.err_msg_allocation_range));

        // Display error message
        if (errMsg.length() != 0) {
            mErrAllocationTV.setText(errMsg);
            mErrAllocationTV.setVisibility(View.VISIBLE);
            return;
        }

        // Pass value to AllocationSettingFragment
        mListener.setFundAllocation(fundName, percentage);

        this.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (FundSetListener) context;

        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() + "must implement FundSetListener");
        }
    }

    public interface FundSetListener {
        void setFundAllocation(String fundName, int allocationPercentage);
    }
}
