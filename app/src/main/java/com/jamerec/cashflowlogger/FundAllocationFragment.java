package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment
        implements FundListAdapter.FundItemClickListener {

    private final String TAG = getClass().getSimpleName();

    // Argument keys
    final static String TITLE = "title";
    final static String SOURCE = "source";
    final static String AMOUNT = "amount";
    final static String FUND_LIST = "fund_list";

    private Context mContext;

    private NestedScrollView mWindow;
    private TextView mTitleTV;
    private TextView mSourceTV;
    private TextView mRemainingAmountTV;
    private TextView mFundNameTV;
    private TextView mErrorMsgTV;
    private RecyclerView mFundAllocationRV;
    private PhCurrencyInput mAllocateAmountPCI;
    private Button mBtnAddAllocation;

    private OnSubmitFundAllocationListener submitListener;
    private PhCurrency mAmount;
    private PhCurrency mRemainingAmount;
    private ArrayList<FundAllocationAmount> mFundsList;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_allocation, container, false);

        mContext = getContext();

        // Get allocation details
        Bundle details = getArguments();
        if (details == null)
            throw new IllegalStateException(
                    Objects.requireNonNull(getActivity())
                            .getClass().getSimpleName() +
                            " must set the arguments for Fund Allocation.");
        String title = details.getString(TITLE);
        String source = details.getString(SOURCE);
        mAmount = details.getParcelable(AMOUNT);
        mFundsList = details.getParcelableArrayList(FUND_LIST);

        // Missing argument
        if (title == null)
            throw new IllegalStateException(
                    Objects.requireNonNull(getActivity())
                            .getClass().getSimpleName() +
                            " must set TITLE argument for Fund Allocation.");
        if (source == null)
            throw new IllegalStateException(
                    Objects.requireNonNull(getActivity())
                            .getClass().getSimpleName() +
                            " must set SOURCE argument for Fund Allocation.");
        if (mAmount == null)
            throw new IllegalStateException(
                    Objects.requireNonNull(getActivity())
                            .getClass().getSimpleName() +
                            " must set AMOUNT argument for Fund Allocation.");
        if (mFundsList == null)
            throw new IllegalStateException(
                    Objects.requireNonNull(getActivity())
                            .getClass().getSimpleName() +
                            " must set FUND_LIST argument for Fund Allocation.");

        mRemainingAmount = new PhCurrency(mAmount);
        mFundSelectedIndex = 0;

        // Initialize widgets
        mWindow = view.findViewById(R.id.scroll_view);
        mTitleTV = view.findViewById(R.id.heading_title);
        mSourceTV = view.findViewById(R.id.disp_source);
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
        mFundListAdapter = new FundListAdapter(mContext, mFundsList, this, R.layout.list_item_fund);
        mFundAllocationRV.setAdapter(mFundListAdapter);

        // Setup display text
        mTitleTV.setText(title);
        mSourceTV.setText(source);
        mRemainingAmountTV.setText(mAmount.toString());
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
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                // Fund allocation is done.
                // Pass the funds allocation list to IncomeLogActivity.
                if (mFundSelectedIndex < 0) {
                    submitListener.submitFundAllocation(mFundsList);
                    return;
                }

                if (Objects.requireNonNull(mAllocateAmountPCI.getText()).length() <= 0 ||
                        mAllocateAmountPCI.getAmount().isZero()) {
                    mBtnAddAllocation.setEnabled(false);
                    mErrorMsgTV.setText(getString(R.string.err_msg_amountZero));
                    mErrorMsgTV.setVisibility(View.VISIBLE);
                    return;
                }

                PhCurrency fundAmount = new PhCurrency(mAllocateAmountPCI.getAmount());
                mAllocateAmountPCI.setText("");

                // Notify if the remaining amount is less than the amount to be allocated.
                if (mRemainingAmount.compareTo(fundAmount) < 0) {
                    mErrorMsgTV.setText(getString(R.string.err_msg_amountTooBig));
                    mErrorMsgTV.setVisibility(View.VISIBLE);
                    return;
                }

                int fundsWithAllocation = 0;
                PhCurrency totalAllocatedAmount = new PhCurrency();

                for (FundAllocationAmount fundItem : mFundsList) {
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
                        totalAllocatedAmount.compareTo(mAmount) < 0) {
                    mErrorMsgTV.setText(getString(R.string.err_msg_allocationIncomplete));
                    mErrorMsgTV.setVisibility(View.VISIBLE);
                    return;
                }

                // Update funds allocation list
                mFundsList.get(mFundSelectedIndex).updateAmount(fundAmount);
                mFundListAdapter.notifyDataSetChanged();

                // Update remaining amount display
                mRemainingAmount.setValue(mAmount);
                mRemainingAmount.subtract(totalAllocatedAmount);
                mRemainingAmountTV.setText(mRemainingAmount.toString());

                // Clear error message.
                mErrorMsgTV.setText("");
                mErrorMsgTV.setVisibility(View.GONE);

                // Check if all the income has been allocated
                if (mRemainingAmount.isZero()) {
                    mBtnAddAllocation.setText(R.string.btn_done);
                    mFundSelectedIndex = -1;
                    mFundNameTV.setText("-----");
                    mAllocateAmountPCI.setEnabled(false);
                    return;
                }

                // Change input display value to the next fund with ₱0.00 value.
                for (FundAllocationAmount fundItem : mFundsList) {
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
        FundAllocationAmount fundItem = mFundsList.get(mFundSelectedIndex);

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
     * Sets the details for allocation in the funds. This should be set before loading this Fragment.
     * Failure to do so will cause this Fragment to throw an IllegalStateException (see onCreateView())
     *
     * @param title     the header to be displayed which depends on which Activity loaded this Fragment
     * @param source    of the amount to be allocated
     * @param amount    of the money to be allocated
     * @param fundList  the funds where the amount can be distributed
     */
    void setDetails(String title, String source, PhCurrency amount, ArrayList<FundAllocationAmount> fundList) {
        // Details for allocation
        Bundle detail = new Bundle();
        detail.putString(TITLE, title);
        detail.putString(SOURCE, source);
        detail.putParcelable(AMOUNT, amount);
        detail.putParcelableArrayList(FUND_LIST, fundList);

        this.setArguments(detail);
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
        void submitFundAllocation(ArrayList<FundAllocationAmount> fundList);
    }

}
