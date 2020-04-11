package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExpenseLogConfirmationFragment extends Fragment
        implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private ExpenseItem mExpenseItem;
    private Context mContext;
    private OnConfirmExpenseLogListener mListener;

    public ExpenseLogConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnConfirmExpenseLogListener) {
            mListener = (OnConfirmExpenseLogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnConfirmExpenseLogListener.confirmExpenseLog");
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense_log_confirmation, container, false);

        mContext = getContext();
        mExpenseItem = getArguments().getParcelable("expenseItem");
//        Log.d(TAG, mExpenseItem.toString());

        // Initialize widgets
        TextView itemTV = view.findViewById(R.id.detail_item);
        TextView brandTV = view.findViewById(R.id.detail_brand);
        TextView priceTV = view.findViewById(R.id.detail_price);
        TextView itemSizeTV = view.findViewById(R.id.detail_item_size);
        TextView quantityTV = view.findViewById(R.id.detail_quantity);
        TextView totalPriceTV = view.findViewById(R.id.detail_total_price);
        TextView fundTV = view.findViewById(R.id.detail_fund);
        TextView remarksTV = view.findViewById(R.id.detail_remarks);
        Button btnLogExpense = view.findViewById(R.id.btn_log);
        Button btnEditExpenseDetails = view.findViewById(R.id.btn_edit);

        // Display ExpenseItem details.
        itemTV.setText(mExpenseItem.getItemName());
        brandTV.setText(mExpenseItem.getBrand());
        priceTV.setText(mExpenseItem.getItemPrice().toString());
        itemSizeTV.setText(mExpenseItem.getSize().toString());
        quantityTV.setText(mExpenseItem.getQuantity().toString());
        totalPriceTV.setText(mExpenseItem.getTotalPrice().toString());
        fundTV.setText(mExpenseItem.getFund());
        if (mExpenseItem.getRemarks().isEmpty()) {
            remarksTV.setText(R.string.no_remarks);
            remarksTV.setTextColor(getResources().getColor(R.color.button_main));
        }
        else {
            remarksTV.setText(mExpenseItem.getRemarks());
        }

        // Set up tags display.
        RecyclerView tagsRV = view.findViewById(R.id.detail_tags);
        FlexboxLayoutManager flexBoxLayoutMgr = new FlexboxLayoutManager(mContext);
        flexBoxLayoutMgr.setFlexDirection(FlexDirection.ROW);
        flexBoxLayoutMgr.setJustifyContent(JustifyContent.FLEX_START);
        flexBoxLayoutMgr.setFlexWrap(FlexWrap.WRAP);
        tagsRV.setLayoutManager(flexBoxLayoutMgr);
        TagItemAdapter tagItemAdapter = new TagItemAdapter(mContext, mExpenseItem, TagItemAdapter.DISPLAY_ONLY);
        tagsRV.setAdapter(tagItemAdapter);

        // Set OnClickListeners.
        btnLogExpense.setOnClickListener(this);
        btnEditExpenseDetails.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int btnID = v.getId();
        mListener.confirmExpenseLog(btnID);
    }

    /**
     * As per the user, proceeds on to logging the ExpenseItem, or
     * redirect back to ExpenseLogDetailsFragment for changing
     * the expense details.
     */
    public interface OnConfirmExpenseLogListener {
        void confirmExpenseLog(int btnID);
    }

}
