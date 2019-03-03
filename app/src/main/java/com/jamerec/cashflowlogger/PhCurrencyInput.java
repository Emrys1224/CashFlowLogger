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
    private final static String PESO_SIGN = "₱ ";
    private final static DecimalFormat PESO_CURRENCY =
            new DecimalFormat("₱ ###,###,###.##");

    private Editable mValue;

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

        Editable.Factory temp = new Editable.Factory();
        mValue = temp.newEditable("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);

        char keyPressed = (char) event.getUnicodeChar();
        Log.d(TAG, "Key pressed : " + Character.toString(keyPressed));
        Log.d(TAG, "Value : " + getText().toString());
        Log.d(TAG, "mValue : " + mValue.toString());

//        String value = getText().toString();
//        value = value.replaceAll("\\D+", "");
//        if (!value.equals("")) {
//            Log.d(TAG, "Value from string: " + value);
//
//            double amount = Double.parseDouble(value);
//            Log.d(TAG, "Value from double: " + amount);
//
//            String amountStr = PESO_CURRENCY.format(amount);
//            Log.d(TAG, "Value from PhPStr: " + amountStr);
//
//            setText(amountStr);
//        }

        return handled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        canvas.drawText("Test Text", getCompoundPaddingLeft(),
                getLineBounds(0, null), getPaint());
    }
}
