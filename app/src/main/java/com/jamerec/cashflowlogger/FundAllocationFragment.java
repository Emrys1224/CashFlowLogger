package com.jamerec.cashflowlogger;


import android.os.Bundle;
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
public class FundAllocationFragment extends Fragment {

    private String[] mCategories;

    public FundAllocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fund_allocation, container, false);

        mCategories = getArguments().getStringArray("categories");

        // Autocomplete category suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mCategories);
        final AutoCompleteTextView catSelection = (AutoCompleteTextView) view.findViewById(R.id.input_category);

        // Setup listeners to show all options
        catSelection.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    catSelection.showDropDown();
            }
        });
        catSelection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                catSelection.showDropDown();
                return false;
            }
        });

        catSelection.setThreshold(1);
        catSelection.setAdapter(catAdapter);

        return view;
    }

}
