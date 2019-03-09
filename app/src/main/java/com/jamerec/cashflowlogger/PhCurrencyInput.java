package com.jamerec.cashflowlogger;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import java.text.DecimalFormat;

/**
 * Extends EditText for input of Philippine peso currency.
 * -- Set the input type to "numberDecimal"
 * -- Formats the display to "₱ ###,###,###.##"
 */
public class PhCurrencyInput extends AppCompatEditText {
    private final static String TAG = "PhPInput";
    private final static String HINT = "₱ XXX,XXX.XX";
    private final static DecimalFormat PESO_CURRENCY =
            new DecimalFormat("₱ ###,###,###.##");

    private double mAmount = 0d;

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

        // Set input type to "numericDecimal"
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Set hint
        setHint(HINT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        String value = getText().toString();

        if (value.equals("")) return true;

        // Get the decimal part of amount
        String decimalPart = "";
        if (value.contains("."))
            decimalPart = value.substring(value.lastIndexOf(".") + 1);

        // Do not update mAmount if the decimal place from input is more than 3
        if (decimalPart.length() < 3) {
            value = value.replaceAll("[₱,]+", "");
            Log.d(TAG, "Value from string: " + value);

            mAmount = Double.parseDouble(value);
            Log.d(TAG, "Value from double: " + mAmount);
        }

        // Do not reformat once centavo place is entered
        if (decimalPart.equals("") || decimalPart.length() >2)
            value = PESO_CURRENCY.format(mAmount);
        Log.d(TAG, "Value from PhPStr: " + value);

        setText(value);
        setSelection(getText().length());

        return true;
    }

    /**
     * Get the peso amount from this input
     * @return peso amount
     */
    public double getAmount() {
        return mAmount;
    }
}
