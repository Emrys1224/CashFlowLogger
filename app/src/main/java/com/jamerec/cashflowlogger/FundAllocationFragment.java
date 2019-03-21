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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FundAllocationFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private TextView mRemainingAmountTV;
    private ListView mFundAllocationLV;
    private Spinner mCatSelection;
    private PhCurrencyInput mAllocateAmountPCI;
    private Button mBtnAddAllocation;

    private OnSubmitFundAllocationListener submitListener;
    private FundListAdapter mFundListAdapter;
    private ArrayAdapter<String> mSelectionAdapter;
    private PhCurrency mIncomeAmount;
    private PhCurrency mRemainingAmount;
    private ArrayList<SimpleEntry<String, PhCurrency>> mFundList;

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
        mFundList = new ArrayList<>();

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
            mFundList.add(new SimpleEntry<>(fundName, new PhCurrency()));
        }

        // Initialize widgets
        mRemainingAmountTV = view.findViewById(R.id.txt_remaining);
        mFundAllocationLV = view.findViewById(R.id.list_funds);
        mCatSelection = view.findViewById(R.id.selection_fund_name);
        mAllocateAmountPCI = view.findViewById(R.id.input_amount);
        mBtnAddAllocation = view.findViewById(R.id.btn_allocate);

        // Setup display text
        mRemainingAmountTV.setText(mIncomeAmount.toString());

        // Setup fund allocation ListView
        mFundListAdapter = new FundListAdapter(mContext, mFundList);
        mFundAllocationLV.setAdapter(mFundListAdapter);
        setListViewHeightBasedOnChildren(mFundAllocationLV);

        // Funds selection spinner
        mSelectionAdapter = new ArrayAdapter<>(
                mContext, R.layout.spinner_item, fundNames);
        mCatSelection.setAdapter(mSelectionAdapter);

        // Setup listeners
        mFundAllocationLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        mCatSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mAllocateAmountPCI.getText().length() > 0)
                    mBtnAddAllocation.setEnabled(true);
                else
                    mBtnAddAllocation.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                if (mAllocateAmountPCI.getText().length() <= 0) {
                    mBtnAddAllocation.setEnabled(false);
                    Toast.makeText(mContext,
                            "Amount input is empty.\nPlease completely fill up the form.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Get the fund details
                String fundName = mCatSelection.getSelectedItem().toString();
                PhCurrency fundAmount = new PhCurrency(
                        mAllocateAmountPCI.getAmount());

                // Clear fund amount input
                mAllocateAmountPCI.setText("");

                // Notify if the remaining amount is less than the amount to allocate.
                if (mRemainingAmount.compareTo(fundAmount) < 0) {
                    Toast.makeText(mContext,
                            "Allocated amount is more than the remaining amount.",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext,
                            "Please enter a value less than or equal to the" +
                                    "remaining amount.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                int fundsWithAllocation = 0;
                PhCurrency fundAmountUpdate = new PhCurrency();
                PhCurrency totalAllocatedAmount = new PhCurrency();

                for (SimpleEntry<String, PhCurrency> fundItem : mFundList) {
                    PhCurrency fundValue = fundItem.getValue();

                    // Current total from funds
                    totalAllocatedAmount.add(fundValue);

                    // Reference to the fund to be updated
                    if (fundItem.getKey().equals(fundName)) {
                        fundAmountUpdate = fundValue;
                    }

                    if (!fundValue.isZero())
                        fundsWithAllocation++;
                }

                totalAllocatedAmount.add(fundAmount);

                // Notify that the income amount has not all been  allocated
                if (fundsWithAllocation == mFundList.size() - 1 &&
                        totalAllocatedAmount.compareTo(mIncomeAmount) < 0) {
                    Toast.makeText(mContext,
                            "This is the last item." +
                                    "\nConsider allocating all the amount or change the other",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update funds allocation list display
                fundAmountUpdate.setValue(fundAmount);
                mFundListAdapter.notifyDataSetChanged();

                // Update remaining amount display
                mRemainingAmount.setValue(mIncomeAmount);
                mRemainingAmount.subtract(totalAllocatedAmount);
                mRemainingAmountTV.setText(mRemainingAmount.toString());

                // Check if all the income has been allocated
                if (mRemainingAmount.isZero()) {
                    mBtnAddAllocation.setText(R.string.btn_done);
                    return;
                }

                // Change Spinner value to the next fund with ₱0.00 value
                for (SimpleEntry fundItem : mFundList) {
                    if (((PhCurrency) fundItem.getValue()).isZero()) {
                        mCatSelection.setSelection(
                                mSelectionAdapter.getPosition((String) fundItem.getKey())
                        );
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
        void submitFundAllocation(ArrayList<SimpleEntry<String, PhCurrency>> fundList);
    }

}
