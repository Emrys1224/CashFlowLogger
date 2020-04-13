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
import android.text.InputType;
import android.view.Display;
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
import java.util.Locale;

public class ExpenseLogDetailsFragment extends Fragment
        implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    // Input validation status
    private final boolean FAILED = false;
    private final boolean PASSED = true;

    // First item in 'Deduct From' input field. Also serves as a hint.
    private final String FUND_SEL_HINT = "-- Select Fund --";

    private NestedScrollView mWindowNSV;

    // Input Field Widgets
    private AutoCompleteTextView mItemATV;
    private AutoCompleteTextView mBrandATV;
    private PhCurrencyInput mPricePCI;
    private AutoCompleteTextView mSizeATV;
    private EditText mQuantityET;
    private TextView mTotalPriceTV;
    private Spinner mFundSelectionS;
    private RecyclerView mTagsRV;
    private AutoCompleteTextView mTagsATV;
    private EditText mRemarksET;

    // Error Messages Widgets
    private TextView mItemErrMsgTV;
    private TextView mBrandErrMsgTV;
    private TextView mPriceErrMsgTV;
    private TextView mSizeErrMsgTV;
    private TextView mQuantityErMsgTV;
    private TextView mFundErrMsgTV;

    // Button Widgets
    private Button mBtnLog;
    private Button mBtnCancel;

    private Context mContext;
    private CFLoggerOpenHelper mDB;
    private OnSubmitExpenseDetailsListener mListener;

    // Purchase item data
    private ExpenseItem mExpenseItem;

    // Adapters
    private ArrayAdapter<String> mFundSelectionAdapter;
    private TagItemAdapter mTagItemAdapter;

    // For tracking screen scroll position for proper positioning of input fields
    // along with the soft keyboard
    private int mScrollYReference;

    // Flag for checking if mFundSelectionS.setOnItemSelectedListener()
    // making sure that it has initialized.
    private boolean mInitialized = false;

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
        mTagsRV = view.findViewById(R.id.disp_allocation);
        mTagsATV = view.findViewById(R.id.input_tags);
        mRemarksET = view.findViewById(R.id.input_remarks);
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
        List<String> funds = new ArrayList<>();
        funds.add(FUND_SEL_HINT);
        funds.addAll(mDB.getFundsList());

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

        // Add autocomplete suggestion item for mItemATV.
        ArrayAdapter<String> itemAdapter =
                new ArrayAdapter<>(
                        mContext, android.R.layout.simple_list_item_1,
                        mDB.getProductsList()
                );
        mItemATV.setThreshold(1);
        mItemATV.setAdapter(itemAdapter);

        // Add autocomplete suggestion item for mBrandATV.
        ArrayAdapter<String> brandAdapter =
                new ArrayAdapter<>(
                        mContext, android.R.layout.simple_list_item_1,
                        mDB.getBrandList()
                );
        mBrandATV.setThreshold(1);
        mBrandATV.setAdapter(brandAdapter);

        // Add autocomplete suggestion item for mSizeATV.
        ArrayAdapter<String> sizeAdapter =
                new ArrayAdapter<>(
                        mContext, android.R.layout.simple_list_item_1,
                        mDB.getSizesList()
                );
        mSizeATV.setThreshold(1);
        mSizeATV.setAdapter(sizeAdapter);

        // Add autocomplete suggestion item for mSizeATV.
        ArrayAdapter<String> tagsAdapter =
                new ArrayAdapter<>(
                        mContext, android.R.layout.simple_list_item_1,
                        mDB.getTagsList()
                );
        mTagsATV.setThreshold(1);
        mTagsATV.setAdapter(tagsAdapter);

        // Set/update product name.
        mItemATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String itemName = mItemATV.getText().toString();

                // Nothing is changed so skip the validation.
                if (!itemName.isEmpty()
                        && itemName.equals(mExpenseItem.getItemName()))
                    return false;

                // Set/update the product name
                mExpenseItem.setItemName(itemName, mDB);
//                Log.d(TAG, mExpenseItem.toString());

                // Retain the view focus and show the error message
                if (validateItemName() == FAILED) return true;

                return false;
            }
        });
        mItemATV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String itemName = mItemATV.getText().toString();

                    // Nothing is changed so skip the validation.
                    if (!itemName.isEmpty()
                            && itemName.equals(mExpenseItem.getItemName()))
                        return;

                    // Set/update the product name
                    mExpenseItem.setItemName(itemName, mDB);
//                Log.d(TAG, mExpenseItem.toString());

                    // Retain the view focus and show the error message
                    validateItemName();
                }
            }
        });

        // Set/update brand name.
        mBrandATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String brand = mBrandATV.getText().toString();

                // Nothing is changed so skip the validation.
                if (!brand.isEmpty()
                        && brand.equals(mExpenseItem.getBrand()))
                    return false;

                mExpenseItem.setBrand(brand);
//                Log.d(TAG, mExpenseItem.toString());

                // Retain the view focus and show the error message if
                // there is an error
                return validateBrandName() == FAILED;
            }
        });
        mBrandATV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String brand = mBrandATV.getText().toString();

                    // Nothing is changed so skip the validation.
                    if (!brand.isEmpty()
                            && brand.equals(mExpenseItem.getBrand()))
                        return;

                    mExpenseItem.setBrand(mBrandATV.getText().toString());
//                    Log.d(TAG, mExpenseItem.toString());

                    // Check and display if an error occurs
                    validateBrandName();
                }
            }
        });

        // Set/update price per unit item.
        mPricePCI.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                PhCurrency itemPrice = mPricePCI.getAmount();

                // Nothing is changed so skip the validation.
                if (!itemPrice.isZero()
                        && itemPrice.equals(mExpenseItem.getItemPrice()))
                    return false;

                mExpenseItem.setPrice(itemPrice);
//                Log.d(TAG, mExpenseItem.toString());

                // Retain the view focus and show the error message if
                // there is an error
                return validatePrice() == FAILED;
            }
        });
        mPricePCI.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    PhCurrency itemPrice = mPricePCI.getAmount();

                    // Nothing is changed so skip the validation.
                    if (!itemPrice.isZero()
                            && itemPrice.equals(mExpenseItem.getItemPrice()))
                        return;

                    mExpenseItem.setPrice(itemPrice);
//                    Log.d(TAG, mExpenseItem.toString());

                    // Check and display if an error occurs
                    validatePrice();
                }
            }
        });

        // Set/update packaging/serving size of the item.
        mSizeATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String size = mSizeATV.getText().toString();

                // Nothing is changed so skip the validation.
                if (!size.isEmpty()
                        && size.equals(mExpenseItem.getSize().toString()))
                    return false;

                int updateResult = mExpenseItem.setSize(size);

                // Retain the view focus and show the error message if
                // there is an error
                return validateSize(updateResult) == FAILED;
            }
        });
        mSizeATV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String size = mSizeATV.getText().toString();

                    // Nothing is changed so skip the validation.
                    if (!size.isEmpty()
                            && size.equals(mExpenseItem.getSize().toString()))
                        return;

                    int updateResult = mExpenseItem.setSize(size);

                    // Check and display if an error occurs
                    validateSize(updateResult);
                }
            }
        });

        // Set/update quantity of purchased item.
        mQuantityET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                double quantity = mQuantityET.getText().toString().isEmpty() ?
                        0 : Double.parseDouble(mQuantityET.getText().toString());

                if (quantity == 0)
                    mExpenseItem.getQuantity().clear();
                else
                    mExpenseItem.setQuantity(quantity);
//                Log.d(TAG, mExpenseItem.toString());

                // Retain the view focus and show the error message if
                // there is an error
                return validateQuantity() == FAILED;
            }
        });
        mQuantityET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    double quantity = mQuantityET.getText().toString().isEmpty() ?
                            0 : Double.parseDouble(mQuantityET.getText().toString());

                    if (quantity == 0)
                        mExpenseItem.getQuantity().clear();
                    else
                        mExpenseItem.setQuantity(quantity);

//                    Log.d(TAG, mExpenseItem.toString());

                    // Check and display if an error occurs
                    validateQuantity();
                }
            }
        });

        // Set/update the fund from where to deduct this purchased item.
        mFundSelectionS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check that initial execution of this method is done during
                // creation of this fragment.
                if (mInitialized) {
                    String selectedFund = parent.getItemAtPosition(position).toString();

                    // Fund not selected
                    if (selectedFund.equals(FUND_SEL_HINT))
                        mExpenseItem.setFund("");
                    else
                        mExpenseItem.setFund(selectedFund);
//                    Log.d(TAG, mExpenseItem.toString());

                    mTagsATV.requestFocus();

                } else {
                    // Initial execution of this function after creation of this fragment
                    mInitialized = true;
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
                if (hasFocus) {
                    hideSoftKeyboard();

                } else {
                    if (mExpenseItem.getFund().isEmpty()) {
                        // Show error message.
                        mFundErrMsgTV.setText(R.string.err_msg_no_fund_selected);
                        mFundErrMsgTV.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Clear error message.
                    mFundErrMsgTV.setText("");
                    mFundErrMsgTV.setVisibility(View.GONE);
                }
            }
        });

        // Update product tags.
        mTagsATV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String tag = mTagsATV.getText().toString();

                if (tag.isEmpty()) {
                    mRemarksET.requestFocus();
                    return false;    // Adding tags done.
                }

                // Add a tag, if does not exist, and update UI display.
                if (mExpenseItem.addTag(tag)) {
                    mTagItemAdapter.notifyDataSetChanged();
//                    Log.d(TAG, mExpenseItem.toString());
                }

                mTagsATV.setText("");

                return true;    // Keep focus for adding additional tags.
            }
        });
        mTagsATV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String tag = mTagsATV.getText().toString();

                if (tag.isEmpty()) return;    // Do nothing

                // Add a tag, if does not exist, and update UI display.
                if (mExpenseItem.addTag(tag)) {
                    mTagItemAdapter.notifyDataSetChanged();
//                    Log.d(TAG, mExpenseItem.toString());
                }

                mTagsATV.setText("");
            }
        });

        // Change soft keyboard "Enter" button into "Done" button
        mRemarksET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mRemarksET.setRawInputType(InputType.TYPE_CLASS_TEXT);
        // Add/update the remarks for this purchase
        mRemarksET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mExpenseItem.setRemarks(mRemarksET.getText().toString());
                    hideSoftKeyboard();
                    mBtnLog.requestFocus();
                }
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
        mTotalPriceTV.setText(mExpenseItem.getTotalPrice().toString());
        mFundSelectionS.setSelection(mFundSelectionAdapter.getPosition(mExpenseItem.getFund()));
        mTagItemAdapter.notifyDataSetChanged();
        mRemarksET.setText(mExpenseItem.getRemarks());
        try {
            mQuantityET.setText(
                    String.format(Locale.ENGLISH, "%.2f", mExpenseItem.getQuantity().getDouble()));
        } catch (Measures.ClearedStateException e) {
            e.printStackTrace();
        }

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
        validateItemName();
        validateBrandName();
        if (mExpenseItem.getItemPrice().isZero()) {
            mPriceErrMsgTV.setText(R.string.err_msg_item_price_zero);
            mPriceErrMsgTV.setVisibility(View.VISIBLE);
        }
        if (mExpenseItem.getSize().isCleared()) {
            mSizeErrMsgTV.setText(R.string.err_msg_set_size_empty);
            mSizeErrMsgTV.setVisibility(View.VISIBLE);
        }
        if (mExpenseItem.getQuantity().isCleared()) {
            mQuantityErMsgTV.setText(R.string.err_msg_quantity_zero);
            mQuantityErMsgTV.setVisibility(View.VISIBLE);
        }
        if (mExpenseItem.getFund().isEmpty()) {
            mFundErrMsgTV.setText(R.string.err_msg_no_fund_selected);
            mFundErrMsgTV.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Hides the soft keyboard.
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFundSelectionS.getWindowToken(), 0);
    }

    /**
     * Checks if there is an item name given and update the error display
     * accordingly.
     *
     * @return FAILED if the item name is empty;
     * PASSED if there is an item name given
     */
    private boolean validateItemName() {
        if (mExpenseItem.getItemName().isEmpty()) {
            // Show error message and retain focus.
            mItemErrMsgTV.setText(R.string.err_msg_item_name_empty);
            mItemErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message.
        mItemErrMsgTV.setText("");
        mItemErrMsgTV.setVisibility(View.GONE);

        // Update tags display as per product stored in database.
        mTagItemAdapter.notifyDataSetChanged();

        return PASSED;
    }

    /**
     * Checks if there is an brand name given and update the error display
     * accordingly.
     *
     * @return FAILED if the brand name is empty;
     * PASSED if there is an brand name given
     */
    private boolean validateBrandName() {
        if (mExpenseItem.getBrand().isEmpty()) {
            // Show error message and retain focus.
            mBrandErrMsgTV.setText(R.string.err_msg_brand_name_empty);
            mBrandErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message.
        mBrandErrMsgTV.setText("");
        mBrandErrMsgTV.setVisibility(View.GONE);

        return PASSED;
    }

    /**
     * Checks that the item price is not zero
     *
     * @return FAILED if the item price is zero;
     * PASSED if the item price is not zero
     */
    private boolean validatePrice() {
        // Update total price display.
        mTotalPriceTV.setText(
                mExpenseItem.getTotalPrice().toString());

        if (mExpenseItem.getItemPrice().isZero()) {
            // Show error message and retain focus.
            mPriceErrMsgTV.setText(R.string.err_msg_item_price_zero);
            mPriceErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message.
        mPriceErrMsgTV.setText("");
        mPriceErrMsgTV.setVisibility(View.GONE);

        return PASSED;
    }

    /**
     * Check if size for the purchased for had ben set properly.
     *
     * @param updateResult for updating the size value
     * @return FAILED if an error occured;
     * PASSED if no error found
     */
    private boolean validateSize(int updateResult) {
        int errMsg = 0;

        // Update packaging size value and check for errors
        switch (updateResult) {
            case ExpenseItem.EMPTY_STRING:
                errMsg = R.string.err_msg_set_size_empty;
                break;
            case ExpenseItem.INVALID_VALUE:
                errMsg = R.string.err_msg_no_set_value;
                break;
            case ExpenseItem.NO_UNIT:
                errMsg = R.string.err_msg_no_set_unit;
                break;
            case ExpenseItem.ZERO_DENOMINATOR:
                errMsg = R.string.err_msg_zero_denominator;
                break;
        }

        try {
            if (mExpenseItem.getSize().getDouble() == 0)
                errMsg = R.string.err_msg_size_zero;

        } catch (Measures.ClearedStateException e) {
            errMsg = R.string.err_msg_set_size_empty;
        }

//         Log.d(TAG, mExpenseItem.toString());

        if (errMsg != 0) {
            // Show error message and retain focus.
            mSizeErrMsgTV.setText(errMsg);
            mSizeErrMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message.
        mSizeErrMsgTV.setText("");
        mSizeErrMsgTV.setVisibility(View.GONE);

        return PASSED;
    }

    /**
     * Check if the quantity is set and display error accordingly.
     * Also update the displayed total price of the purchase item.
     *
     * @return FAILED if quantity was not set;
     * PASSED if quantity was set
     */
    private boolean validateQuantity() {
        mTotalPriceTV.setText(mExpenseItem.getTotalPrice().toString());

        if (mExpenseItem.getQuantity().isCleared()) {
            // Show error message and retain focus.
            mQuantityErMsgTV.setText(R.string.err_msg_quantity_zero);
            mQuantityErMsgTV.setVisibility(View.VISIBLE);
            return FAILED;
        }

        // Clear error message.
        mQuantityErMsgTV.setText("");
        mQuantityErMsgTV.setVisibility(View.GONE);

        // Show fund selection dropdown.
        mFundSelectionS.performClick();

        return PASSED;
    }

    /**
     * This passes the expense details for logging into database
     */
    public interface OnSubmitExpenseDetailsListener {
        void submitExpenseDetails(ExpenseItem expenseItem);
    }
}
