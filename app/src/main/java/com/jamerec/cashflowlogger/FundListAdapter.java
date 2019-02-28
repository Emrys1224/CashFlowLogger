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
    private ArrayList<FundItem> categories;

    public FundListAdapter(Context context, ArrayList<FundItem> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
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

        FundItem fundItem = categories.get(position);

        TextView fundNameTV = (TextView) convertView.findViewById(R.id.label_category);
        TextView fundAmtTV = (TextView) convertView.findViewById(R.id.amount);

        fundNameTV.setText(fundItem.getName());
        fundAmtTV.setText(fundItem.getAmountString());

        return convertView;
    }
}
