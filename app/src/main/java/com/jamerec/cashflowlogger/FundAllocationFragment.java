package com.jamerec.cashflowlogger;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment {

    String[] categories = {
            "Basic Necessity",
            "Education",
            "Investment",
            "Health",
            "Retirement",
            "Leisure"
    };

    public FundAllocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fund_allocation, container, false);

        // Autocomplete category suggestions
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, categories);
        AutoCompleteTextView catSelection = (AutoCompleteTextView) view.findViewById(R.id.input_category);
        catSelection.setAdapter(catAdapter);

        return view;
    }

}
