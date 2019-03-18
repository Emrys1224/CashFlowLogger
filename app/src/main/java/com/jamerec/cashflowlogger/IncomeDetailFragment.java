package com.jamerec.cashflowlogger;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private String[] mIncomeSource;

    public IncomeDetailFragment() {
        // Required empty public constructor
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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_detail, container, false);

        mIncomeSource = incomeSource;

        // Autocomplete category suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mIncomeSource);
        final AutoCompleteTextView incomeSourceSelection = (AutoCompleteTextView) view.findViewById(R.id.input_income_source);

        // Setup listeners to show all options
        incomeSourceSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                incomeSourceSelection.showDropDown();
                return false;
            }
        });

        incomeSourceSelection.setThreshold(1);
        incomeSourceSelection.setAdapter(catAdapter);


        return view;
    }


}