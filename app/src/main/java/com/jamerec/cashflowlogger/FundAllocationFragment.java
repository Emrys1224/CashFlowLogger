package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment
        implements FundListAdapterRV.FundItemClickListener {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private NestedScrollView mWindow;
    private TextView mRemainingAmountTV;
    private TextView mFundNameTV;
    private TextView mErrorMsgTV;
    private RecyclerView mFundAllocationRV;
    private PhCurrencyInput mAllocateAmountPCI;
    private Button mBtnAddAllocation;

    private OnSubmitFundAllocationListener submitListener;
    private PhCurrency mIncomeAmount;
    private PhCurrency mRemainingAmount;
    private ArrayList<FundItem> mFundsList;
    private RecyclerView.Adapter mFundListAdapter;
    private RecyclerView.LayoutManager mLayoutMgr;
    private int mFundSelectedIndex;

    public FundAllocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSubmitFundAllocationListener) {
            submitListener = (OnSubmitFundAllocationListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnSubmitFundAllocationListener.submitFundAllocation");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fund_allocation, container, false);

        mContext = getContext();
        mIncomeAmount = getArguments() != null ?
                (PhCurrency) getArguments().getParcelable("incomeAmount") : new PhCurrency();
        mRemainingAmount = new PhCurrency(mIncomeAmount);
        mFundsList = new ArrayList<>();
        mFundSelectedIndex = 0;

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
        for (String fundName : fundNames) {
            mFundsList.add(new FundItem(fundName, new PhCurrency()));
        }

        // Initialize widgets
        mWindow = view.findViewById(R.id.scroll_view);
        mRemainingAmountTV = view.findViewById(R.id.txt_remaining);
        mFundNameTV = view.findViewById(R.id.item_fund_name);
        mFundAllocationRV = view.findViewById(R.id.list_funds);
        mAllocateAmountPCI = view.findViewById(R.id.input_amount);
        mErrorMsgTV = view.findViewById(R.id.err_msg_allocation);
        mBtnAddAllocation = view.findViewById(R.id.btn_allocate);

        // Set up fund allocation list.
        mFundAllocationRV.setHasFixedSize(true);
        mLayoutMgr = new LinearLayoutManager(mContext);
        mFundAllocationRV.setLayoutManager(mLayoutMgr);
        mFundListAdapter = new FundListAdapterRV(mContext, mFundsList, this);
        mFundAllocationRV.setAdapter(mFundListAdapter);

        // Setup display text
        mRemainingAmountTV.setText(mIncomeAmount.toString());
        mFundNameTV.setText(
                mFundsList.get(mFundSelectedIndex).getName());
        mErrorMsgTV.setText("");

        mAllocateAmountPCI.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE &&
                        mAllocateAmountPCI.getText().length() > 0)
                    mBtnAddAllocation.setEnabled(true);
                else
                    mBtnAddAllocation.setEnabled(false);
                return false;
            }
        });

        mBtnAddAllocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fund allocation is done.
                // Pass the funds allocation list to IncomeLogActivity.
                if (mFundSelectedIndex < 0) {
                    submitListener.submitFundAllocation(mFundsList);
                    return;
                }

                if (mAllocateAmountPCI.getText().length() <= 0 ||
                        mAllocateAmountPCI.getAmount().isZero()) {
                    mBtnAddAllocation.setEnabled(false);
                    mErrorMsgTV.setText(getString(R.string.err_msg_amountZero));
                    return;
                }

                PhCurrency fundAmount = new PhCurrency(mAllocateAmountPCI.getAmount());
                mAllocateAmountPCI.setText("");

                // Notify if the remaining amount is less than the amount to be allocated.
                if (mRemainingAmount.compareTo(fundAmount) < 0) {
                    mErrorMsgTV.setText(getString(R.string.err_msg_amountTooBig));
                    return;
                }

                int fundsWithAllocation = 0;
                PhCurrency totalAllocatedAmount = new PhCurrency();

                for (FundItem fundItem : mFundsList) {
                    PhCurrency fundValue = fundItem.getAmount();

                    if (fundValue.isZero()) continue;

                    // Current total from funds
                    totalAllocatedAmount.add(fundValue);

                    // Count how many funds have allocation.
                    fundsWithAllocation++;
                }

                totalAllocatedAmount.add(fundAmount);

                // Notify that the income amount has not all been  allocated  while
                // all funds have amount allocation already.
                if (fundsWithAllocation == mFundsList.size() - 1 &&
                        totalAllocatedAmount.compareTo(mIncomeAmount) < 0) {
                    mErrorMsgTV.setText(getString(R.string.err_msg_allocationIncomplete));
                    return;
                }

                // Update funds allocation list
                mFundsList.get(mFundSelectedIndex).updateAmount(fundAmount);
                mFundListAdapter.notifyDataSetChanged();

                // Update remaining amount display
                mRemainingAmount.setValue(mIncomeAmount);
                mRemainingAmount.subtract(totalAllocatedAmount);
                mRemainingAmountTV.setText(mRemainingAmount.toString());

                // Clear error message.
                mErrorMsgTV.setText("");

                // Check if all the income has been allocated
                if (mRemainingAmount.isZero()) {
                    mBtnAddAllocation.setText(R.string.btn_done);
                    mFundSelectedIndex = -1;
                    mFundNameTV.setText("-----");
                    mAllocateAmountPCI.setEnabled(false);
                    return;
                }

                // Change input display value to the next fund with ₱0.00 value.
                for (FundItem fundItem : mFundsList) {
                    if (fundItem.getAmount().isZero()) {
                        mFundSelectedIndex = mFundsList.indexOf(fundItem);
                        mFundNameTV.setText(fundItem.getName());
                        break;
                    }
                }
                mBtnAddAllocation.setText(R.string.btn_allocate);

            }
        });

        return view;
    }

    @Override
    public void onFundItemClicked(View v, int position) {
        // Select a fund to set the allocated amount.
        mFundSelectedIndex = position;
        FundItem fundItem = mFundsList.get(mFundSelectedIndex);

        // Add the amount of this fund back to the remaining amount to be allocated.
        mRemainingAmount.add(fundItem.getAmount());
        fundItem.getAmount().setValue(0);

        // Update the UI display
        mFundListAdapter.notifyDataSetChanged();
        mRemainingAmountTV.setText(mRemainingAmount.toString());
        mFundNameTV.setText(fundItem.getName());
        mAllocateAmountPCI.setEnabled(true);
        mWindow.fullScroll(View.FOCUS_DOWN);
    }

    /**
     * This interface submits the details for fund allocation.
     */
    public interface OnSubmitFundAllocationListener {
        /**
         * Submit fund allocation details.
         *
         * @param fundList list of fund with its amount allocation.
         */
        void submitFundAllocation(ArrayList<FundItem> fundList);
    }

}
