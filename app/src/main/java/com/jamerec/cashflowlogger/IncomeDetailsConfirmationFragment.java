package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeDetailsConfirmationFragment extends Fragment {

    private final static String TAG = "DetailsConfirmFragment";

    private ListView mCategoriesLV;

    private Context mContext;
    private ArrayList<FundItem> mFundList;

    public IncomeDetailsConfirmationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_income_details_confirmation, container, false);

        mContext = getContext();
        mFundList = new ArrayList<>();

        // Dummy values
        // To be replaced with values from FundAllocationFragment
        String[] fundNames = {
                "Basic Necessity",
                "Education",
                "Investment",
                "Health",
                "Retirement",
                "Leisure"
        };

        for (String fundName : fundNames) {
            mFundList.add(new FundItem(fundName, new PhCurrency()));
        }

        // Populate allocation list
        FundListAdapter adapter = new FundListAdapter(mContext, mFundList);
        mCategoriesLV = view.findViewById(R.id.list_funds);
        mCategoriesLV.setAdapter(adapter);

        // Disable scrolling of list
        mCategoriesLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        setListViewHeightBasedOnChildren(mCategoriesLV);

        return view;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}
