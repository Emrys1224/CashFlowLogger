package com.jamerec.cashflowlogger;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private OnSubmitIncomeDetailListener submitListener;
    private String[] mIncomeSource;

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
        mIncomeSource = incomeSource;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_detail, container, false);

        // Autocomplete category suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mIncomeSource);
        final AutoCompleteTextView incomeSourceSelection = (AutoCompleteTextView) view.findViewById(R.id.input_income_source);

        // Setup listener to show all options
        incomeSourceSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                incomeSourceSelection.showDropDown();
                return false;
            }
        });

        incomeSourceSelection.setThreshold(1);
        incomeSourceSelection.setAdapter(catAdapter);

        final PhCurrencyInput incomeAmountInput = (PhCurrencyInput) view.findViewById(R.id.input_amount);

        Button btnAllocateAuto = (Button) view.findViewById(R.id.btn_allocate_auto);
        Button btnAllocateManual = (Button) view.findViewById(R.id.btn_allocate_man);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        // Setup button click listeners
        btnAllocateAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int btnID = v.getId();
                String incomeSource = incomeSourceSelection.getText().toString();
                PhCurrency incomeAmount = incomeAmountInput.getAmount();

                submitListener.submitIncomeDetailListener(incomeSource, incomeAmount, btnID);
            }
        });
        btnAllocateManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int btnID = v.getId();
                String incomeSource = incomeSourceSelection.getText().toString();
                PhCurrency incomeAmount = incomeAmountInput.getAmount();

                submitListener.submitIncomeDetailListener(incomeSource, incomeAmount, btnID);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * This interface submits the income details for further processing...
     */
    public interface OnSubmitIncomeDetailListener {
        /**
         * Submits the income details
         * @param incomeSource source of income
         * @param incomeAmount income amount
         * @param btnID id of the button that calls this method
         */
        public void submitIncomeDetailListener(String incomeSource, PhCurrency incomeAmount, int btnID);
    }
}