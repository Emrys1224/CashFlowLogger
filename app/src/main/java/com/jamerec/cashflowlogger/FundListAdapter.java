package com.jamerec.cashflowlogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class FundListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SimpleEntry<String, PhCurrency>> mFundList;

    FundListAdapter(
            Context context,
            ArrayList<SimpleEntry<String, PhCurrency>> mFundList) {
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

        SimpleEntry fundItem = (SimpleEntry) getItem(position);

        TextView fundNameTV = (TextView) convertView.findViewById(R.id.label_fund);
        TextView fundAmtTV = (TextView) convertView.findViewById(R.id.amount);

        fundNameTV.setText((String)fundItem.getKey());
        fundAmtTV.setText(fundItem.getValue().toString());

        return convertView;
    }
}
