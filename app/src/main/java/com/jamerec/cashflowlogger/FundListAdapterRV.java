package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class FundListAdapterRV extends RecyclerView.Adapter<FundListAdapterRV.FundListItem> {

    private final String TAG = getClass().getSimpleName();

    private ArrayList<com.jamerec.cashflowlogger.FundItem> mFundsList;
    private LayoutInflater mInflater;
    private FundItemClickListener mListener;

    FundListAdapterRV(Context context,
                      ArrayList<com.jamerec.cashflowlogger.FundItem> fundsList,
                      FundItemClickListener listener) {
        this.mFundsList = fundsList;
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public FundListItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View fundItemView = mInflater.inflate(R.layout.list_item_fund, viewGroup, false);
        return new FundListItem(fundItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FundListItem fundListItem, int fundIndex) {
        FundItem fundItem = mFundsList.get(fundIndex);
        fundListItem.mFundName.setText(fundItem.getName());
        fundListItem.mFundAmount.setText(fundItem.getAmount().toString());

        fundListItem.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFundItemClicked(
                        fundListItem.mItemView, fundListItem.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFundsList.size();
    }

    class FundListItem extends RecyclerView.ViewHolder {
        final View mItemView;
        final TextView mFundName;
        final TextView mFundAmount;

        FundListItem(@NonNull View itemView) {
            super(itemView);

            this.mItemView = itemView;
            this.mFundName = itemView.findViewById(R.id.label_fund);
            this.mFundAmount = itemView.findViewById(R.id.amount);
        }
    }

    /**
     * Interface for accessing the RecyclerView item when it is clicked.
     */
    interface FundItemClickListener {

        /**
         * This passes the view (fund item) clicked and its position
         * @param v the fund item that has been clicked.
         * @param position od the clicked item.
         */
        void onFundItemClicked(View v, int position);
    }
}
