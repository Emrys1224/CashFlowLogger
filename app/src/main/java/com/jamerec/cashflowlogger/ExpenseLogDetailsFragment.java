package com.jamerec.cashflowlogger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.flexbox.*;

import java.util.List;
import java.util.Locale;

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
    private TextView mFundErrMsgTV;
    private RecyclerView mTagsRV;
    private EditText mTagsET;
    private Button mBtnLog;
    private Button mBtnCancel;

    private Context mContext;
    private CFLoggerOpenHelper mDB;
    private OnSubmitExpenseDetailsListener mListener;
    private ExpenseItem mExpenseItem;
    private ArrayAdapter<String> mFundSelectionAdapter;
    private TagItemAdapter mTagItemAdapter;
    private int mScrollYReference;

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
        mDB = new CFLoggerOpenHelper(mContext);
        mExpenseItem = getArguments() != null ?
                (ExpenseItem) getArguments().getParcelable("expenseItem") : new ExpenseItem();

        // Get device display height.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScrollYReference = size.y;
//        Log.d(TAG, "Scroll Y Position\nmScrollYReference: " + mScrollYReference);

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
        mFundErrMsgTV = view.findViewById(R.id.err_msg_fund);
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
        mFundErrMsgTV.setText("");

        // Initialize total item price display
        mTotalPriceTV.setText(
                mExpenseItem.getTotalPrice().toString());

        // List of funds retrieved from database
        List<String> funds = mDB.getFundsList();

        // Set up fund selection dropdown
        mFundSelectionAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, funds);
        mFundSelectionS.setAdapter(mFundSelectionAdapter);

        // Set up tags display.
        FlexboxLayoutManager flexBoxLayoutMgr = new FlexboxLayoutManager(mContext);
        flexBoxLayoutMgr.setFlexDirection(FlexDirection.ROW);
        flexBoxLayoutMgr.setJustifyContent(JustifyContent.FLEX_START);
        flexBoxLayoutMgr.setFlexWrap(FlexWrap.WRAP);
        mTagsRV.setLayoutManager(flexBoxLayoutMgr);
        mTagItemAdapter = new TagItemAdapter(mContext, mExpenseItem, TagItemAdapter.BUTTON_TAG);
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
                        mScrollYReference = Math.min(rect.bottom, mScrollYReference);
//                          Log.d(TAG,
//                                "Rect dimension/position" +
//                                        "\nRect left: " + rect.left + "\nRect top: " + rect.top +
//                                        "\nRect right: " + rect.right + "\nRect bottom: " + rect.bottom);

                        // Get the position of the focused view within the scroll view.
                        if (getActivity() == null) return;
                        final View focusedView = getActivity().getCurrentFocus();
                        if (focusedView == null) return;
                        int focusedViewYPos = focusedView.getTop();
//                            Log.d(TAG,
//                                    "Focused View Position" +
//                                            "\nY Pos: " + focusedViewYPos);

                        // Scroll the screen to show the input field and the
                        // space for the error message.
                        final int scrollYTo = focusedViewYPos - ((mScrollYReference * 5) / 9);
                        mWindowNSV.post(new Runnable() {
                            @Override
                            public void run() {
                                mWindowNSV.smoothScrollTo(0, scrollYTo);
                            }
                        });
                    }
                }
        );

        // Set/update product name.
        mItemATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Set/update the product name
                String itemName = mItemATV.getText().toString();
                mExpenseItem.setItemName(itemName, mDB);
//                Log.d(TAG, mExpenseItem.toString());

                // Update tags display as per product stored in database.
                mTagItemAdapter.notifyDataSetChanged();

                // Add autocomplete suggestion item for mBrandATV.
                ArrayAdapter<String> brandAdapter =
                        new ArrayAdapter<>(
                                mContext, android.R.layout.simple_list_item_1,
                                mDB.getBrandList(mExpenseItem)
                        );
                mBrandATV.setThreshold(1);
                mBrandATV.setAdapter(brandAdapter);

                // Add autocomplete suggestion item for mSizeATV.
                ArrayAdapter<String> sizeAdapter =
                        new ArrayAdapter<>(
                                mContext, android.R.layout.simple_list_item_1,
                                mDB.getSizesList(mExpenseItem)
                        );
                mSizeATV.setThreshold(1);
                mSizeATV.setAdapter(sizeAdapter);

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
//                Log.d(TAG, mExpenseItem.toString());

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
//                Log.d(TAG, mExpenseItem.toString());

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
                int errMsg = 0;
                try {
                    mExpenseItem.setSize(mSizeATV.getText().toString());

                } catch (Measures.InvalidValueException ive) {
                    errMsg = R.string.err_msg_no_set_value;

                } catch (Measures.NoValidUnitException iue) {
                    errMsg = R.string.err_msg_no_set_unit;

                } catch (Measures.ZeroDenominatorException zde) {
                    errMsg = R.string.err_msg_zero_denominator;
                }

                try {
                    if (mExpenseItem.getSize().getDouble() == 0)
                        errMsg = R.string.err_msg_size_zero;

                } catch (IllegalAccessException iae) {
                    errMsg = R.string.err_msg_set_size_empty;
                }

//                Log.d(TAG, mExpenseItem.toString());

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
                mExpenseItem.setQuantity(mQuantityET.getText().toString());

//                Log.d(TAG, mExpenseItem.toString());

                mTotalPriceTV.setText(mExpenseItem.getTotalPrice().toString());

                if (mExpenseItem.getQuantity().isCleared()) {
                    // Show error message and retain focus.
                    mQuantityErMsgTV.setText(R.string.err_msg_quantity_zero);
                    return true;
                }

                // Clear error message.
                mQuantityErMsgTV.setText("");

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

//                Log.d(TAG, mExpenseItem.toString());

                if (!mExpenseItem.getItemName().isEmpty()) {
                    mTagsET.requestFocus();

                    // Show soft keyboard.
                    InputMethodManager imm = (InputMethodManager)
                            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mTagsET, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Do Nothing */ }
        });

        mFundSelectionS.setFocusable(true);
        mFundSelectionS.setFocusableInTouchMode(true);
        mFundSelectionS.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // Hide the soft keyboard.
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)
                            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mFundSelectionS.getWindowToken(), 0);
                } else {
                    // Show error message.
                    if (mExpenseItem.getFund().isEmpty()) {
                        mFundErrMsgTV.setText(R.string.err_msg_no_fund_selected);
                        return;
                    }

                    // Clear error message.
                    mFundErrMsgTV.setText("");
                }
            }
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
                mTagItemAdapter.notifyDataSetChanged();

                mTagsET.setText("");

//                Log.d(TAG, mExpenseItem.toString());

                return true;    // Keep focus for adding additional tags.
            }
        });

        // Submit ExpenseItem
        mBtnLog.setOnClickListener(this);

        // Return to home page.
        mBtnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) return;

        // Update input values for editing.
        mItemATV.setText(mExpenseItem.getItemName());
        mBrandATV.setText(mExpenseItem.getBrand());
        mPricePCI.setAmount(mExpenseItem.getItemPrice());
        mSizeATV.setText(mExpenseItem.getSize().toString());
        mQuantityET.setText(
                String.format(Locale.ENGLISH, "%.2f", mExpenseItem.getQuantity()));
        mTotalPriceTV.setText(mExpenseItem.getTotalPrice().toString());
        mFundSelectionS.setSelection(
                mFundSelectionAdapter.getPosition(mExpenseItem.getFund()));
        mTagItemAdapter.notifyDataSetChanged();
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
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            return;
        }

        // Submit ExpenseItem for confirmation.
        if (mExpenseItem.readyToLog()) {
            mListener.submitExpenseDetails(mExpenseItem);
            return;
        }

        // Show error messages.
        if (mExpenseItem.getItemName().isEmpty())
            mItemErrMsgTV.setText(R.string.err_msg_item_name_empty);
        if (mExpenseItem.getBrand().isEmpty())
            mBrandErrMsgTV.setText(R.string.err_msg_brand_name_empty);
        if (mExpenseItem.getItemPrice().isZero())
            mPriceErrMsgTV.setText(R.string.err_msg_item_price_zero);
        if (mExpenseItem.getSize().isCleared())
            mSizeErrMsgTV.setText(R.string.err_msg_set_size_empty);
        if (mExpenseItem.getQuantity().isCleared())
            mQuantityErMsgTV.setText(R.string.err_msg_quantity_zero);
        if (mExpenseItem.getFund().isEmpty())
            mFundErrMsgTV.setText(R.string.err_msg_no_fund_selected);

    }

    /**
     * This passes the expense details
     */
    public interface OnSubmitExpenseDetailsListener {
        void submitExpenseDetails(ExpenseItem expenseItem);
    }
}
