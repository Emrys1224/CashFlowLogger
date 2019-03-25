package com.jamerec.cashflowlogger;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailFragment extends Fragment
        implements TextView.OnEditorActionListener, View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    // Text Fields
    private AutoCompleteTextView mIncomeSourceSelection;
    private PhCurrencyInput mIncomeAmountInput;

    // Buttons
    private Button mBtnAllocateAuto;
    private Button mBtnAllocateManual;
    private Button mBtnCancel;

    private OnSubmitIncomeDetailListener submitListener;
    private String[] mIncomeSources;

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
        mContext = getContext();

        // Income source dummy data fetched from DB
        // To be used for selection for income source data input.
        String[] incomeSources = {
                "Youtube",
                "TuloyPoKayo.com",
                "Salary"
        };
        mIncomeSources = incomeSources;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_detail, container, false);

        // Autocomplete income source suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mIncomeSources);
        mIncomeSourceSelection = view.findViewById(R.id.input_income_source);

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

        mIncomeAmountInput = view.findViewById(R.id.input_amount);
        mIncomeAmountInput.setOnEditorActionListener(this);

        mBtnAllocateAuto = view.findViewById(R.id.btn_allocate_auto);
        mBtnAllocateManual = view.findViewById(R.id.btn_allocate_man);
        mBtnCancel = view.findViewById(R.id.btn_cancel);

        // Setup button click listeners
        mBtnAllocateAuto.setOnClickListener(this);
        mBtnAllocateManual.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // For when edit is selected in IncomeDetailsConfirmationFragment
        if (getArguments() != null) {
            String incomeSource = getArguments().getString("incomeSource", "");
            PhCurrency incomeAmount = getArguments().getParcelable("incomeAmount");

            mIncomeSourceSelection.setText(incomeSource);
            mIncomeAmountInput.setAmount(incomeAmount);

            mBtnAllocateAuto.setEnabled(true);
            mBtnAllocateManual.setEnabled(true);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {

            if (!mIncomeSourceSelection.getText().toString().equals("")
                    && !mIncomeAmountInput.getAmount().isZero()) {
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

        // Exit IncomeLogActivity
        if (btnID == R.id.btn_cancel) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            return;
        }

        // Income details
        String  incomeSource = mIncomeSourceSelection.getText().toString();
        PhCurrency incomeAmount = mIncomeAmountInput.getAmount();

        if (!incomeSource.equals("") && !incomeAmount.isZero())
            submitListener.submitIncomeDetails(incomeSource, incomeAmount, btnID);

        else {
            mBtnAllocateAuto.setEnabled(false);
            mBtnAllocateManual.setEnabled(false);

            Toast.makeText(mContext,
                    "An input detail is missing.\nPlease completely fill up the form.",
                    Toast.LENGTH_LONG).show();
        }
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
}