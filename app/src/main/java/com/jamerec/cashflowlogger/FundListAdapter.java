package com.jamerec.cashflowlogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FundListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<FundItem> mFundList;

    FundListAdapter(
            Context context,
            ArrayList<FundItem> mFundList) {
        this.context = context;
        this.mFundList = mFundList;
    }

    @Override
    public int getCount() {
        return mFundList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFundList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_fund, parent, false);
        }

        FundItem fundItem = (FundItem) getItem(position);

        TextView fundNameTV = convertView.findViewById(R.id.label_fund);
        TextView fundAmtTV = convertView.findViewById(R.id.amount);

        fundNameTV.setText(fundItem.getName());
        fundAmtTV.setText(fundItem.getAmount().toString());

        return convertView;
    }
}
