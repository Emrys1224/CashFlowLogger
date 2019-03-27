package com.jamerec.cashflowlogger;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private ScrollView mWindow;
    private TextView mRemainingAmountTV;
    private TextView mFundNameTV;
    private ListView mFundAllocationLV;
    private PhCurrencyInput mAllocateAmountPCI;
    private Button mBtnAddAllocation;

    private OnSubmitFundAllocationListener submitListener;
    private FundListAdapter mFundListAdapter;
    private PhCurrency mIncomeAmount;
    private PhCurrency mRemainingAmount;
    private ArrayList<FundItem> mFundsList;
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
        mFundAllocationLV = view.findViewById(R.id.list_funds);
        mAllocateAmountPCI = view.findViewById(R.id.input_amount);
        mBtnAddAllocation = view.findViewById(R.id.btn_log);

        // Setup display text
        mRemainingAmountTV.setText(mIncomeAmount.toString());
        mFundNameTV.setText(
                mFundsList.get(mFundSelectedIndex).getName());

        // Setup fund allocation ListView
        mFundListAdapter = new FundListAdapter(mContext, mFundsList);
        mFundAllocationLV.setAdapter(mFundListAdapter);
        setListViewHeightBasedOnChildren(mFundAllocationLV);

        // Setup listeners
        mFundAllocationLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        mFundAllocationLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // Select a fund from the list view to set/change the amount allocation.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "Clicked fund: " + mFundsList.get(position).getName());

                mFundSelectedIndex = position;
                FundItem fundItem = mFundsList.get(position);

                // Set the amount of this fund to ₱0.00 and add it back to the remaining amount.
                mRemainingAmount.add(fundItem.getAmount());
                fundItem.resetAmount();
                mFundListAdapter.notifyDataSetChanged();

                // Update text display
                mRemainingAmountTV.setText(mRemainingAmount.toString());
                mFundNameTV.setText(fundItem.getName());
                if (!mAllocateAmountPCI.isEnabled()) mAllocateAmountPCI.setEnabled(true);
            }
        });

        mAllocateAmountPCI.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Scroll to bottom
                mWindow.fullScroll(View.FOCUS_DOWN);

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

                if (mAllocateAmountPCI.getText().length() <= 0) {
                    mBtnAddAllocation.setEnabled(false);
                    Toast.makeText(mContext,
                            "Amount input is empty.\nPlease completely fill up the form.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                PhCurrency fundAmount = new PhCurrency(mAllocateAmountPCI.getAmount());
                mAllocateAmountPCI.setText("");

                // Notify if the remaining amount is less than the amount to be allocated.
                if (mRemainingAmount.compareTo(fundAmount) < 0) {
                    Toast.makeText(mContext,
                            "Allocated amount is more than the remaining amount.",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext,
                            "Please enter a value less than or equal to the " +
                                    "remaining amount.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                int fundsWithAllocation = 0;
                PhCurrency totalAllocatedAmount = new PhCurrency();

                for (FundItem fundItem : mFundsList) {
                    PhCurrency fundValue = fundItem.getAmount();

                    // Current total from funds
                    totalAllocatedAmount.add(fundValue);

                    // Count how many funds have allocation.
                    if (!fundValue.isZero())
                        fundsWithAllocation++;
                }

                totalAllocatedAmount.add(fundAmount);

                // Notify that the income amount has not all been  allocated  while
                // all funds have amount allocation already.
                if (fundsWithAllocation == mFundsList.size() - 1 &&
                        totalAllocatedAmount.compareTo(mIncomeAmount) < 0) {
                    Toast.makeText(mContext,
                            "This is the last item." +
                                    "\nConsider allocating all the amount or change the other",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update funds allocation list
                mFundsList.get(mFundSelectedIndex).updateAmount(fundAmount);
                mFundListAdapter.notifyDataSetChanged();

                // Update remaining amount display
                mRemainingAmount.setValue(mIncomeAmount);
                mRemainingAmount.subtract(totalAllocatedAmount);
                mRemainingAmountTV.setText(mRemainingAmount.toString());

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
