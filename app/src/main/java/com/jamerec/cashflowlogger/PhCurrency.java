package com.jamerec.cashflowlogger;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Class for representing and handling operations for Philippine peso.
 * This is created as a solution for inaccuracy of decimals when
 * using double/float. This includes the methods for comparisons
 * and arithmetic operations for this data model.
 */
public class PhCurrency {
    private final static String TAG = "PhCurrency";

    // Maximum integer that a double can represent without
    // losing precision. Used as the maximum value for
    // storing the currency value. Since negative value
    // for double is same with the positive value with
    // only the sign bit changed, -PESO_MAX is
    // the minimum that can be stored.
    private final static long PESO_MAX = (long) Math.pow(2, 53);
    private final static long PESO_MIN = PESO_MAX * -1;

    private long mPesoAmount;
    private byte mCentavoAmount;

    /**
     * Create PhCurrency with a default value of ₱0.00.
     */
    public PhCurrency() {
        setValue(0D);
    }

    /**
     * Create PhCurrency with the given value.
     *
     * @param amount of the currency.
     */
    public PhCurrency(double amount) {
        setValue(amount);
    }

    /**
     * Create PhCurrency with the given value.
     *
     * @param amount of the currency.
     */
    public PhCurrency(PhCurrency amount) {
        setValue(amount);
    }

    /**
     * Change the current value to the given value.
     *
     * @param amount new value.
     */
    public void setValue(double amount) {
        this.mPesoAmount = (long) amount;
        this.mCentavoAmount = (byte) Math.round((amount - mPesoAmount) * 100D);
    }

    /**
     * Copy the value from another amount.
     *
     * @param amount to be copied.
     */
    public void setValue(PhCurrency amount) {
        this.mPesoAmount = amount.getPesoAmount();
        this.mCentavoAmount = amount.getCentavoAmount();
    }

    /**
     * Get the peso value.
     *
     * @return peso value.
     */
    public long getPesoAmount() {
        return mPesoAmount;
    }

    /**
     * Get the centavo value.
     *
     * @return centavo value.
     */
    public byte getCentavoAmount() {
        return mCentavoAmount;
    }

    /**
     * Get the amount in double.
     *
     * @return amount in double.
     */
    public double toDouble() {
        return mPesoAmount + ( (double) mCentavoAmount / 100 );
    }

    /**
     * Get a formatted string representation.
     *
     * @return Philippine peso formatted string.
     */
    @Override
    public String toString() {
        DecimalFormat pesoCurrency =
                new DecimalFormat("₱ ###,###,###.##");
        return String.format(
                Locale.ENGLISH,"%s.%02d",
                pesoCurrency.format(mPesoAmount), mCentavoAmount);
    }

    /**
     * Checks if the PhCurrency is greater than,  less than,
     * or equal to the compared PhCurrency.
     *
     * @param amount is the value to compare to
     * @return 0 if the values are equal;
     * -1 if less than the value being compared with;
     * 1 if greater than the value being compared with.
     */
    public byte compareTo(PhCurrency amount) {
        long pesoDiff = this.mPesoAmount - amount.getPesoAmount();
        if (pesoDiff > 0) return 1;     // this is greater than compared value
        if (pesoDiff < 0) return -1;    // this is less than compared value

        int centDiff = this.mCentavoAmount - amount.getCentavoAmount();
        if (centDiff > 0) return 1;     // this is greater than compares value
        if (centDiff < 0) return -1;    // this is less than compared value

        return 0;   // values are equal
    }

    /**
     * Checks if the PhCurrency(s) are of equal value.
     *
     * @param amount to compare.
     * @return true if equal;
     * false if not equal.
     */
    public boolean equals(PhCurrency amount) {
        if (this.mPesoAmount != amount.getPesoAmount())
            return false;
        return this.mCentavoAmount == amount.getCentavoAmount();
    }

    /**
     * Adds the value from another PhCurrency.
     *
     * @param amount to be added.
     * @throws ArithmeticException Peso value overflow.
     */
    public void add(PhCurrency amount) throws ArithmeticException {
        long amountPeso = amount.getPesoAmount();

        // Check for overflow
        if (amountPeso > 0 ?
                this.mPesoAmount > PESO_MAX - amountPeso :
                this.mPesoAmount < PESO_MIN - amountPeso) {
            throw new ArithmeticException("Peso value overflow. You're too rich for this app! XD");
        }

        this.mCentavoAmount += amount.getCentavoAmount();
        // Carry over to peso value
        if (this.mCentavoAmount > 100L) {
            long carry = this.mCentavoAmount / 100L;
            this.mCentavoAmount %= 100;    // get the tens and ones place
            this.mPesoAmount += carry;
        }

        this.mPesoAmount += amountPeso;
    }

    /**
     * Returns the sum of a set of PhCurrency.
     *
     * @param amountList to get the sum from.
     * @return a new PhCurrency whose value is the sum of the list.
     * @throws ArithmeticException Peso value overflow.
     */
    public static PhCurrency sum(@NonNull ArrayList<PhCurrency> amountList) throws ArithmeticException {
        PhCurrency sum = new PhCurrency();

        if (amountList.size() == 0) return sum;

        for (int i = 0; i < amountList.size(); i++) {
            sum.add(amountList.get(i));
        }

        return sum;
    }

    /**
     * Subtract a value from this amount.
     *
     * @param amount to be subtracted.
     * @throws ArithmeticException Peso value overflow.
     */
    public void subtract(PhCurrency amount) throws ArithmeticException {
        long amountPeso = amount.getPesoAmount();
        byte amountCent = amount.getCentavoAmount();

        // Check for overflow
        if (amountPeso > 0 ?
                this.mPesoAmount < Long.MAX_VALUE + amountPeso :
                this.mPesoAmount > Long.MIN_VALUE + amountPeso) {
            throw new ArithmeticException("Peso value overflow. You're crazy! XD");
        }

        // Barrow from mPesoAmount
        if (this.mCentavoAmount < amountCent) {
            this.mPesoAmount -= 1;
            this.mCentavoAmount += 100;
        }

        this.mCentavoAmount -= amountCent;
        this.mPesoAmount -= amountPeso;
    }

    public void multiplyBy(double factor) {
        // Check for overflow
        long factorTest = (long) factor;
        if (factorTest > 0 ?
                this.mPesoAmount > Long.MAX_VALUE / factorTest ||
                        this.mPesoAmount < Long.MIN_VALUE / factorTest :
                (factorTest < -1 ?
                        this.mPesoAmount > Long.MIN_VALUE / factorTest ||
                                this.mPesoAmount < Long.MAX_VALUE / factorTest :
                        factorTest == -1 &&
                                this.mPesoAmount == Long.MIN_VALUE)) {
            throw new ArithmeticException("Peso value overflow. Where did money came from?!!");
        }

        this.mCentavoAmount *= factor;

        double partialProduct = (double) this.mPesoAmount * factor;
        this.mPesoAmount = (long) partialProduct;
        // add the decimal part to mCentavo
        this.mCentavoAmount = (byte) Math.round( (partialProduct - this.mPesoAmount) * 100D );

        // Carry over to peso value
        if (this.mCentavoAmount > 100L) {
            long carry = this.mCentavoAmount / 100L;
            this.mCentavoAmount %= 100;    // get the tens and ones place
            this.mPesoAmount += carry;
        }
    }
}











