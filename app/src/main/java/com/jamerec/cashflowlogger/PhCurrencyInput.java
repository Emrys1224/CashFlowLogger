package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Extends (@link AppCompatEditText) for input of Philippine peso currency.
 * -- Set the input type to "numberDecimal"
 * -- Formats the display to "₱ ###,###,###.##"
 */
public class PhCurrencyInput extends AppCompatEditText {
    private final String TAG = getClass().getSimpleName();
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAmount = new PhCurrency();

        // Set input type to "numericDecimal"
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Set hint text to "₱ XXX,XXX.XX"
        setHint(HINT);

        addTextChangedListener(onTextChangeListener());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setText("-----");
            mAmount.setValue(0);
            return;
        }
        setText("");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeTextChangedListener(onTextChangeListener());
    }

    private TextWatcher onTextChangeListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.d(TAG, "Editable: " + s);

                if (!isEnabled()) return;

                removeTextChangedListener(this);

                // Get the numerical value in decimal format
                String displayValue = s.toString();
                displayValue = displayValue.replaceAll("[ ₱,]+", "");
//                Log.d(TAG, "Value from string: " + displayValue);

                if (displayValue.length() == 0) {
                    setText(displayValue);
                    mAmount.setValue(0);                    // amount is ₱0.00
                    addTextChangedListener(this);
                    return;
                }

                // Count the decimal places
                int decimals = 0;
                if (displayValue.contains(".")) {
                    String decimalPart = displayValue.substring(displayValue.lastIndexOf(".") + 1);
                    decimals = decimalPart.length();

                    // Do not alter the display
                    if (decimals == 0) {
                        addTextChangedListener(this);
                        return;
                    }
                }

                // Don't update the value
                if (decimals > 2) {
                    displayValue = mAmount.toString();
                    setText(displayValue);
                    setSelection(displayValue.length());
                    addTextChangedListener(this);
                    return;
                }

                // Update peso currency value
                mAmount.setValue(Double.parseDouble(displayValue));
//                Log.d(TAG, "Value from double: " + mAmount.toDouble());

                // Update displayed value
                displayValue = mAmount.toString();
//                Log.d(TAG, "Value from PhPStr 1: " + displayValue);
                if (decimals == 0)
                    // Remove the decimal places ( n.00 to n )
                    displayValue = displayValue.substring(0, displayValue.length() - 3);
                else if (decimals < 2)
                    // Remove the tenths place zero ( n.m0 to n.m )
                    displayValue = displayValue.substring(0, displayValue.length() - 1);
//                Log.d(TAG, "Value from PhPStr 2: " + displayValue);
                setText(displayValue);
                setSelection(displayValue.length());   // Move cursor to the end

                addTextChangedListener(this);
            }
        };
    }

    /**
     * Get the peso amount from this input
     *
     * @return peso amount
     */
    public PhCurrency getAmount() {
        return mAmount;
    }

    /**
     * Provide a pre-set value.
     * @param amount value currency
     */
    public void setAmount(PhCurrency amount) {
        setText(amount.toString());
        mAmount.setValue(amount);
    }
}
