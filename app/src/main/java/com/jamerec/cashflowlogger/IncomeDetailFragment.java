package com.jamerec.cashflowlogger;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailFragment extends Fragment
        implements TextView.OnEditorActionListener, View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    // Text Fields
    private AutoCompleteTextView mIncomeSourceSelection;
    private PhCurrencyInput mIncomeAmountInput;

    // Buttons
    private Button mBtnAllocateAuto;
    private Button mBtnAllocateManual;
    private Button mBtnCancel;

    private OnSubmitIncomeDetailListener submitListener;
    private String[] mIncomeSources;

    // Income Details
    private String mIncomeSource;
    private PhCurrency mIncomeAmount;

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
                    + " must implement OnSubmitIncomeDetailListener.submitIncomeDetailListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Income source dummy data fetched from DB
        // To be used for selection for income source data input.
        String[] incomeSource = {
                "Youtube",
                "TuloyPoKayo.com",
                "Salary"
        };
        mIncomeSources = incomeSource;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_detail, container, false);

        // Autocomplete category suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mIncomeSources);
        mIncomeSourceSelection = (AutoCompleteTextView) view.findViewById(R.id.input_income_source);

        // Setup listener to show all options
        mIncomeSourceSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIncomeSourceSelection.showDropDown();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mIncomeSourceSelection.setThreshold(1);
        mIncomeSourceSelection.setAdapter(catAdapter);
        mIncomeSourceSelection.setOnEditorActionListener(this);

        mIncomeAmountInput = (PhCurrencyInput) view.findViewById(R.id.input_amount);
        mIncomeAmountInput.setOnEditorActionListener(this);

        mBtnAllocateAuto = (Button) view.findViewById(R.id.btn_allocate_auto);
        mBtnAllocateManual = (Button) view.findViewById(R.id.btn_allocate_man);
        mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);

        // Setup button click listeners
        mBtnAllocateAuto.setOnClickListener(this);
        mBtnAllocateManual.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
            mIncomeSource = mIncomeSourceSelection.getText().toString();
            mIncomeAmount = mIncomeAmountInput.getAmount();

            Log.d(TAG, "Income source: " + mIncomeSource + "\t\tIncome amount: " + mIncomeAmount);

            if (!mIncomeSource.equals("") && mIncomeAmount.isNotZero()) {
                mBtnAllocateAuto.setEnabled(true);
                mBtnAllocateManual.setEnabled(true);
            } else {
                mBtnAllocateAuto.setEnabled(false);
                mBtnAllocateManual.setEnabled(false);
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int btnID = v.getId();

        if (btnID == R.id.btn_cancel) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            return;
        }

        if (!mIncomeSource.equals("") && mIncomeAmount.isNotZero())
            submitListener.submitIncomeDetailListener(mIncomeSource, mIncomeAmount, btnID);
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
        public void submitIncomeDetailListener(String incomeSource, PhCurrency incomeAmount, int btnID);
    }
}