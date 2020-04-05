package com.jamerec.cashflowlogger;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailFragment extends Fragment
        implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private final boolean FAILED = false;
    private final boolean PASSED = true;

    // Text Fields
    private AutoCompleteTextView mIncomeSourceATV;
    private PhCurrencyInput mIncomeAmountPCI;
    private TextView mFromErrMsgTV;
    private TextView mAmountErrMsgTV;

    // Buttons
    private Button mBtnAllocateAuto;
    private Button mBtnAllocateManual;
    private Button mBtnCancel;

    private Context mContext;
    private OnSubmitIncomeDetailListener submitListener;

    // Income Details
    String mIncomeSource;
    PhCurrency mIncomeAmount;


    public IncomeDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSubmitIncomeDetailListener) {
            submitListener = (OnSubmitIncomeDetailListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnSubmitIncomeDetailListener.submitIncomeDetails");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_detail, container, false);

        mContext = getContext();
        mIncomeSource = "";
        mIncomeAmount = new PhCurrency();

        // Initialize widgets
        mIncomeSourceATV = view.findViewById(R.id.input_income_source);
        mIncomeAmountPCI = view.findViewById(R.id.input_amount);
        mFromErrMsgTV = view.findViewById(R.id.err_msg_from);
        mAmountErrMsgTV = view.findViewById(R.id.err_msg_amount);
        mBtnAllocateAuto = view.findViewById(R.id.btn_allocate_auto);
        mBtnAllocateManual = view.findViewById(R.id.btn_allocate_man);
        mBtnCancel = view.findViewById(R.id.btn_cancel);

        // Income source data fetched from DB
        // To be used for selection for income source data input.
        CFLoggerOpenHelper db = new CFLoggerOpenHelper(mContext);
        List<String> incomeSources = db.getIncomeSourceList();

        // Autocomplete income source suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, incomeSources);
        mIncomeSourceATV.setThreshold(1);
        mIncomeSourceATV.setAdapter(catAdapter);

        // Set/update income source
        mIncomeSourceATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mIncomeSource = mIncomeSourceATV.getText().toString();
                return validateIncomeSource() == FAILED;
            }
        });
        mIncomeSourceATV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mIncomeSource = mIncomeSourceATV.getText().toString();
                    validateIncomeSource();
                }
            }
        });

        // Set/update income amount
        mIncomeAmountPCI.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mIncomeAmount.setValue(mIncomeAmountPCI.getAmount());
                return validateIncomeAmount() == FAILED;
            }
        });
        mIncomeAmountPCI.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mIncomeAmount.setValue(mIncomeAmountPCI.getAmount());
                    validateIncomeAmount();
                }
            }
        });

        // Clear error message.
        mFromErrMsgTV.setText("");
        mAmountErrMsgTV.setText("");

        // Setup button click listeners
        mBtnAllocateAuto.setOnClickListener(this);
        mBtnAllocateManual.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // For when edit is selected in IncomeDetailsConfirmationFragment
        if (getArguments() != null) {
            mIncomeSource = getArguments().getString("incomeSource", "");
            mIncomeAmount = getArguments().getParcelable("incomeAmount");

            mIncomeSourceATV.setText(mIncomeSource);
            if (mIncomeAmount != null)
                mIncomeAmountPCI.setAmount(mIncomeAmount);
        }

    }

    @Override
    public void onClick(@NonNull View v) {
        int btnID = v.getId();

        // Exit IncomeLogActivity
        if (btnID == R.id.btn_cancel) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            return;
        }

        // Check income details and show if error occurs
        if (validateIncomeSource() == FAILED && validateIncomeAmount() == FAILED) {
            return;
        }

        // Proceed to allocation of income into funds as per the button pressed
        submitListener.submitIncomeDetails(mIncomeSource, mIncomeAmount, btnID);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        submitListener = null;
    }

    /**
     * This interface submits the income details for further processing...
     */
    public interface OnSubmitIncomeDetailListener {
        /**
         * Submits the income details
         *
         * @param incomeSource source of income
         * @param incomeAmount income amount
         * @param btnID        id of the button that calls this method
         */
        void submitIncomeDetails(String incomeSource, PhCurrency incomeAmount, int btnID);
    }

    /**
     * Validate that there is a value set for income source and update the error status
     *
     * @return FAILED if there is no value set;
     *         PASSED if the value is set
     */
    private boolean validateIncomeSource() {
        if (mIncomeSource.isEmpty()) {
            mFromErrMsgTV.setText(R.string.err_msg_from_isEmpty);
            mFromErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message
        mFromErrMsgTV.setText("");
        mFromErrMsgTV.setVisibility(View.GONE);

        return PASSED;
    }

    /**
     * Validate the income amount set is not zero and update the error status
     *
     * @return FAILED if the value set is zero;
     *         PASSED if the value is not zero;
     */
    private boolean validateIncomeAmount() {
        if (mIncomeAmount.isZero()) {
            mAmountErrMsgTV.setText(R.string.err_msg_amountIsZero);
            mAmountErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message
        mAmountErrMsgTV.setText("");
        mAmountErrMsgTV.setVisibility(View.GONE);

        return PASSED;
    }

}