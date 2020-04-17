package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class FundListAdapter extends RecyclerView.Adapter<FundListAdapter.FundListItem> {

    private final String TAG = getClass().getSimpleName();

    private List mFundsList;
    private LayoutInflater mInflater;
    private FundItemClickListener mListener;
    private int mItemLayout;

    FundListAdapter(Context context,
                    List fundsList,
                    FundItemClickListener listener,
                    int itemLayout) {
        this.mFundsList = fundsList;
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        this.mItemLayout = itemLayout;
    }

    @NonNull
    @Override
    public FundListItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View fundItemView = mInflater.inflate(mItemLayout, viewGroup, false);
        return new FundListItem(fundItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FundListItem fundListItem, int fundIndex) {
        String name = "";
        String amount = "";
        Object fundItem = mFundsList.get(fundIndex);

        if (fundItem instanceof FundAllocationAmount) {
            FundAllocationAmount thisFundItem = (FundAllocationAmount) fundItem;
            name = thisFundItem.getName();
            amount = thisFundItem.getAmount().toString();
        }

        if (fundItem instanceof FundAllocationPercentage) {
            FundAllocationPercentage thisFundItem =
                    (FundAllocationPercentage) fundItem;
            name = thisFundItem.getFundName();
            amount = thisFundItem.getPercentAllocation() + "%";
        }

        fundListItem.mFundName.setText(name);
        fundListItem.mFundAmount.setText(amount);

        // Implement a ClickListener if the caller has FundItemClickListener.
        if (mListener == null) return;
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

    static class FundListItem extends RecyclerView.ViewHolder {
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
         *
         * @param v        the fund item that has been clicked.
         * @param position od the clicked item.
         */
        void onFundItemClicked(View v, int position);
    }
}
