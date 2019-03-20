package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;



/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private ListView mFundAllocationLV;
    private Spinner mCatSelection;

    private Context mContext;
    private ArrayList<SimpleEntry<String, PhCurrency>> mFundList;
    private String[] mFundNames;

    public FundAllocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fund_allocation, container, false);

        mContext = getContext();
        mFundList = new ArrayList<>();

        // Categories dummy data from preference setting
        // To be used for selection in allocating funds from the income.
        String[] fundNames = {
                "Basic Necessity",
                "Education",
                "Investment",
                "Health",
                "Retirement",
                "Leisure"
        };
        mFundNames = fundNames;

        for (String fundName: fundNames) {
            mFundList.add(new SimpleEntry<>(fundName, new PhCurrency()));
        }

        // Setup fund allocation ListView
        FundListAdapter adapter = new FundListAdapter(mContext, mFundList);
        mFundAllocationLV = view.findViewById(R.id.list_funds);
        mFundAllocationLV.setAdapter(adapter);
        mFundAllocationLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        setListViewHeightBasedOnChildren(mFundAllocationLV);

        // Autocomplete category suggestions
        mCatSelection = view.findViewById(R.id.selection_fund_name);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(
                mContext, R.layout.spinner_item, mFundNames);
        mCatSelection.setAdapter(catAdapter);

        return view;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec
                .makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup
                        .LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}
