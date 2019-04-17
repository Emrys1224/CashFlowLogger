package com.jamerec.cashflowlogger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

public class ExpenseLogDetailsFragment extends Fragment
        implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private NestedScrollView mWindowNSV;
    private AutoCompleteTextView mItemATV;
    private TextView mItemErrMsgTV;
    private AutoCompleteTextView mBrandATV;
    private TextView mBrandErrMsgTV;
    private PhCurrencyInput mPricePCI;
    private TextView mPriceErrMsgTV;
    private AutoCompleteTextView mSizeATV;
    private TextView mSizeErrMsgTV;
    private EditText mQuantityET;
    private TextView mQuantityErMsgTV;
    private TextView mTotalPriceTV;
    private Spinner mFundSelectionS;
    private RecyclerView mTagsRV;
    private EditText mTagsET;
    private Button mBtnLog;
    private Button mBtnCancel;

    private Context mContext;
    private OnSubmitExpenseDetailsListener mListener;
    private ExpenseItem mExpenseItem;
    private TagItemAdapter mTagItemAdapter;
    private List<String> mTags;

    public ExpenseLogDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmitExpenseDetailsListener) {
            mListener = (OnSubmitExpenseDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSubmitExpenseDetailsListener");
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_expense_log_details, container, false);

        mContext = getContext();
        mExpenseItem = new ExpenseItem();
        mTags = new ArrayList<>();

        // Initialize widgets.
        mWindowNSV = view.findViewById(R.id.nestedScrollView);
        mItemATV = view.findViewById(R.id.input_item);
        mItemErrMsgTV = view.findViewById(R.id.err_msg_item);
        mBrandATV = view.findViewById(R.id.input_brand);
        mBrandErrMsgTV = view.findViewById(R.id.err_msg_brand);
        mPricePCI = view.findViewById(R.id.input_price);
        mPriceErrMsgTV = view.findViewById(R.id.err_msg_price);
        mSizeATV = view.findViewById(R.id.input_size);
        mSizeErrMsgTV = view.findViewById(R.id.err_msg_size);
        mQuantityET = view.findViewById(R.id.input_quantity);
        mQuantityErMsgTV = view.findViewById(R.id.err_msg_quantity);
        mTotalPriceTV = view.findViewById(R.id.disp_total_price);
        mFundSelectionS = view.findViewById(R.id.selection_fund);
        mTagsRV = view.findViewById(R.id.disp_tags);
        mTagsET = view.findViewById(R.id.input_tags);
        mBtnLog = view.findViewById(R.id.btn_log);
        mBtnCancel = view.findViewById(R.id.btn_cancel);

        //Clear error messages.
        mItemErrMsgTV.setText("");
        mBrandErrMsgTV.setText("");
        mPriceErrMsgTV.setText("");
        mSizeErrMsgTV.setText("");
        mQuantityErMsgTV.setText("");

        // Initialize total item price display.
        mTotalPriceTV.setText(
                mExpenseItem.getTotalPrice().toString());

        // List of funds retrieved from SharedPreference.
        List<String> funds = new ArrayList<>();
        funds.add("");
        funds.add("Basic Needs");
        funds.add("Education");
        funds.add("Investment");
        funds.add("Retirement");
        funds.add("Leisure");

        // Set up fund selection dropdown.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, funds);
        mFundSelectionS.setAdapter(adapter);

        // Set up tags display.
        FlexboxLayoutManager flexBoxLayoutMgr = new FlexboxLayoutManager(mContext);
        flexBoxLayoutMgr.setFlexDirection(FlexDirection.ROW);
        flexBoxLayoutMgr.setJustifyContent(JustifyContent.FLEX_START);
        flexBoxLayoutMgr.setFlexWrap(FlexWrap.WRAP);
        mTagsRV.setLayoutManager(flexBoxLayoutMgr);
        mTagItemAdapter = new TagItemAdapter(mContext, mTags);
        mTagsRV.setAdapter(mTagItemAdapter);

        // Scroll the view when the input field is focused to show both the input field
        // and the space for the error message.
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Get the dimension of the screen after the soft keyboard was shown.
                        Rect rect = new Rect();
                        view.getWindowVisibleDisplayFrame(rect);
                        Log.d(TAG,
                                "Rect dimension/position" +
                                        "\nRect left: " + rect.left + "\nRect top: " + rect.top +
                                        "\nRect right: " + rect.right + "\nRect bottom: " + rect.bottom);

                        // Get the position of the focused view within the scroll view.
                        final View focusedView = getActivity().getCurrentFocus();
                        if (focusedView != null) {
                            int focusedViewYPos = focusedView.getTop();
                            Log.d(TAG,
                                    "Focused View Position" +
                                            "\nY Pos: " + focusedViewYPos);

                            // Scroll the screen to show the input field and the
                            // space for the error message.
                            final int scrollYTo = focusedViewYPos - ((rect.bottom * 5) / 9);
                            mWindowNSV.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWindowNSV.smoothScrollTo(0, scrollYTo);
                                }
                            });
                        }
                    }
                }
        );

        // Set/update product name.
        mItemATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String itemName = mItemATV.getText().toString();
                mExpenseItem.setItemName(itemName);
                Log.d(TAG, mExpenseItem.toString());

                // Add autocomplete suggestion item for mBrandATV.
                ArrayAdapter<String> brandAdapter =
                        new ArrayAdapter<>(
                                mContext, android.R.layout.simple_list_item_1,
                                mExpenseItem.suggestBrands()
                        );
                mBrandATV.setThreshold(1);
                mBrandATV.setAdapter(brandAdapter);

                // Add autocomplete suggestion item for mSizeATV.
                ArrayAdapter<String> sizeAdapter =
                        new ArrayAdapter<>(
                                mContext, android.R.layout.simple_list_item_1,
                                mExpenseItem.suggestSizes()
                        );
                mSizeATV.setThreshold(1);
                mSizeATV.setAdapter(sizeAdapter);

                // Update tags display as per product stored in database.
                mTags.clear();
                mTags.addAll(mExpenseItem.getTags());
                mTagItemAdapter.notifyDataSetChanged();

                if (itemName.isEmpty()) {
                    // Show error message and retain focus.
                    mItemErrMsgTV.setText(R.string.err_msg_item_name_empty);
                    return true;
                }

                // Clear error message.
                mItemErrMsgTV.setText("");

                return false;
            }
        });

        // Set/update brand name.
        mBrandATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String brandName = mBrandATV.getText().toString();
                mExpenseItem.setBrand(brandName);
                Log.d(TAG, mExpenseItem.toString());

                if (brandName.isEmpty()) {
                    // Show error message and retain focus.
                    mBrandErrMsgTV.setText(R.string.err_msg_brand_name_empty);
                    return true;
                }

                // Clear error message.
                mBrandErrMsgTV.setText("");

                return false;
            }
        });

        // Set/update price per unit item.
        mPricePCI.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                PhCurrency itemPrice = mPricePCI.getAmount();
                mExpenseItem.setPrice(itemPrice);
                Log.d(TAG, mExpenseItem.toString());

                // Update total price display.
                mTotalPriceTV.setText(
                        mExpenseItem.getTotalPrice().toString());

                if (itemPrice.isZero()) {
                    // Show error message and retain focus.
                    mPriceErrMsgTV.setText(R.string.err_msg_item_price_zero);
                    return true;
                }

                // Clear error message.
                mPriceErrMsgTV.setText("");

                return false;
            }
        });

        // Set/update packaging/serving size of the item.
        mSizeATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Update packaging size value.
                int result = mExpenseItem.setSize(
                        mSizeATV.getText().toString());

                // Set error message.
                int errMsg = 0;
                switch (result) {
                    case ExpenseItem.SET_SIZE_OK:
                        break;
                    case ExpenseItem.NO_SET_UNIT:
                        errMsg = R.string.err_msg_no_set_unit;
                        break;
                    case ExpenseItem.NO_SET_VALUE:
                        errMsg = R.string.err_msg_no_set_value;
                        break;
                    case ExpenseItem.SET_SIZE_EMPTY:
                        errMsg = R.string.err_msg_set_size_empty;
                }

                Log.d(TAG, mExpenseItem.toString());

                if (errMsg != 0) {
                    // Show error message and retain focus.
                    mSizeErrMsgTV.setText(errMsg);
                    return true;
                }

                // Clear error message.
                mSizeErrMsgTV.setText("");

                return false;
            }
        });

        // Set/update quantity of purchased item.
        mQuantityET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String quantity = mQuantityET.getText().toString();
                mExpenseItem.setQuantity(
                        quantity.isEmpty() ?
                                0D : Double.parseDouble(quantity)
                );

                Log.d(TAG, mExpenseItem.toString());

                mTotalPriceTV.setText(
                        mExpenseItem.getTotalPrice().toString());

                if (quantity.isEmpty()) {
                    // Show error message and retain focus.
                    mQuantityErMsgTV.setText(R.string.err_msg_quantity_zero);
                    return true;
                }

                // Clear error message.
                mQuantityErMsgTV.setText("");

                // Hide the soft keyboard.
                InputMethodManager imm = (InputMethodManager)
                        mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mQuantityET.getWindowToken(), 0);
                // Show fund selection dropdown.
                mFundSelectionS.performClick();
                return false;
            }
        });

        // Set/update the fund from where to deduct this purchased item.
        mFundSelectionS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExpenseItem.setFund(
                        parent.getItemAtPosition(position).toString());

                Log.d(TAG, mExpenseItem.toString());

                if (!mExpenseItem.getItemName().isEmpty()) {
                    mTagsET.requestFocus();
                }

                // Show soft keyboard.
                InputMethodManager imm = (InputMethodManager)
                        mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTagsET, InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /*Do nothing....*/ }
        });

        // Update product tags.
        mTagsET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String tag = mTagsET.getText().toString();

                if (tag.isEmpty()) {
                    mBtnLog.requestFocus();
                    return false;    // Adding tags done.
                }

                // Add a tag and update UI display.
                mExpenseItem.addTag(tag);
                mTags.clear();
                mTags.addAll(mExpenseItem.getTags());
                mTagItemAdapter.notifyDataSetChanged();

                mTagsET.setText("");

                Log.d(TAG, mExpenseItem.toString());

                return true;    // Keep focus for adding additional tags.
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int btnId = v.getId();

        if (btnId == R.id.btn_cancel) {
            Intent intent = new Intent(mContext, ExpensesLogActivity.class);
            startActivity(intent);
            return;
        }

        // Valida expense item details.

    }

    /**
     * This passes the expense details
     */
    public interface OnSubmitExpenseDetailsListener {
        void submitExpenseDetails(ExpenseItem expenseItem);
    }
}
