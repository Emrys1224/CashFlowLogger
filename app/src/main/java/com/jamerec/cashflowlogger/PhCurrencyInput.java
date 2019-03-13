package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Extends EditText for input of Philippine peso currency.
 * -- Set the input type to "numberDecimal"
 * -- Formats the display to "₱ ###,###,###.##"
 */
public class PhCurrencyInput extends AppCompatEditText {
    private final static String TAG = "PhPInput";
    private final static String HINT = "₱ XXX,XXX.XX";

    private PhCurrency mAmount;

    public PhCurrencyInput(Context context) {
        super(context);
    }

    public PhCurrencyInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhCurrencyInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAmount = new PhCurrency();

        // Set input type to "numericDecimal"
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Set hint
        setHint(HINT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        String displayValue = getText().toString();
        if (displayValue.equals("")) return true;

        // Count the decimal places
        int decimals = 0;
        if (displayValue.contains(".")) {
            String decimalPart = displayValue.substring(displayValue.lastIndexOf(".") + 1);
            decimals = decimalPart.length();
        }

        // Remove the third decimal place and don't update the value
        if (decimals > 2) {
            displayValue = displayValue.substring(0, displayValue.length() - 1);
            setText(displayValue);
            setSelection(getText().length());
            return true;
        }

        // Update peso currency value
        String plainNum = displayValue.replaceAll("[₱,]+", "");
//        Log.d(TAG, "Value from string: " + plainNum);
        mAmount.setValue(Double.parseDouble(plainNum));
//        Log.d(TAG, "Value from double: " + mAmount.toDouble());

        // Update displayed value
        displayValue = mAmount.toString();
//        Log.d(TAG, "Value from PhPStr 1: " + displayValue);
        if (decimals == 0)
            // Remove the decimal places ( n.00 to n )
            displayValue = displayValue.substring(0, displayValue.length() - 3);
        else if (decimals < 2)
            // Remove the tenths place zero ( n.m0 to n.m )
            displayValue = displayValue.substring(0, displayValue.length() - 1);
//        Log.d(TAG, "Value from PhPStr 2: " + displayValue);
        setText(displayValue);
        setSelection(getText().length());   // Move cursor to the end

        return true;
    }

    /**
     * Get the peso amount from this input
     *
     * @return peso amount
     */
    public PhCurrency getAmount() {
        return mAmount;
    }
}
