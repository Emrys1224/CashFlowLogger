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

    // Thw maximum integer that a double can represent without
    // losing precision is 2^53. Used as the maximum value for
    // storing the currency value. Since negative value
    // for double is same with the positive value with
    // only the sign bit changed, -PESO_MAX is
    // the minimum that can be stored.
    private final static long PESO_MAX = (long) Math.pow(2, 53);
    private final static long PESO_MIN = PESO_MAX * -1;

    private long mPesoAmount;
    private byte mCentavoAmount;

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Constructors ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

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

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Setters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/


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

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Getters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

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
        return mPesoAmount + ((double) mCentavoAmount / 100);
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
                Locale.ENGLISH, "%s.%02d",
                pesoCurrency.format(mPesoAmount), mCentavoAmount);
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Comparators ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

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

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Arithmetic Operations ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Adds the value from another PhCurrency.
     *
     * @param amount to be added.
     * @throws ArithmeticException Sum peso value overflow.
     */
    public void add(PhCurrency amount) throws ArithmeticException {
        long amountPeso = amount.getPesoAmount();

//        System.out.println("\nthis.peso: " + this.mPesoAmount +
//                "\nthis.centavo: " + this.mCentavoAmount);
//        System.out.println("\naddend.peso: " + amount.getPesoAmount() +
//                "\naddend.centavo: " + amount.getCentavoAmount());

        // Check for overflow
        if (amountPeso > 0 ?
                this.mPesoAmount > PESO_MAX - amountPeso :
                this.mPesoAmount < PESO_MIN - amountPeso) {
            throw new ArithmeticException("Sum peso value overflow. You're too rich for this app! XD");
        }

        // Add the centavo values
        int centSum = this.mCentavoAmount + amount.getCentavoAmount();
//        System.out.println("\nthis.centavo + addend.centavo = " + centSum);
        this.mCentavoAmount = (byte) (centSum % 100);           // Get the tens and ones place
//        System.out.println("centavo : " + this.mCentavoAmount);

        // Carry over to peso value
        if (centSum > 100L) {
            long carry = (long) (centSum / 100);
//            System.out.println("\ncarry : " + carry);
            this.mPesoAmount += carry;
//            System.out.println("this.peso + carry = " + this.mPesoAmount);
        }

        this.mPesoAmount += amountPeso;
//        System.out.println("\nthis.peso + addend.peso = " + this.mPesoAmount);
//        System.out.println("this.peso: " + this.mPesoAmount +
//                "\nthis.centavo: " + this.mCentavoAmount);
    }

    /**
     * Returns the sum of a set of PhCurrency.
     *
     * @param amountList to get the sum from.
     * @return a new PhCurrency whose value is the sum of the list.
     * @throws ArithmeticException Sum peso value overflow.
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
     * @throws ArithmeticException Difference peso value overflow.
     */
    public void subtract(PhCurrency amount) throws ArithmeticException {
        long amountPeso = amount.getPesoAmount();
        byte amountCent = amount.getCentavoAmount();

        // Check for overflow
        if (amountPeso > 0 ?
                this.mPesoAmount < Long.MAX_VALUE + amountPeso :
                this.mPesoAmount > Long.MIN_VALUE + amountPeso) {
            throw new ArithmeticException("Difference peso value overflow. You're crazy!");
        }

        int thisCent = this.mCentavoAmount;
        // Barrow from mPesoAmount
        if (thisCent < amountCent) {
            this.mPesoAmount -= 1;
            thisCent += 100;
        }

        this.mCentavoAmount = (byte) (thisCent - amountCent);
        this.mPesoAmount -= amountPeso;
    }

    /**
     * Get the absolute difference between two PhCurrency values.
     *
     * @param amount1 the currency to compare with.
     * @param amount2 the currency to compare with.
     * @return new PhCurrency with value of the absolute difference.
     * @throws ArithmeticException Difference peso value overflow.
     */
    public PhCurrency differenceAbsolute(PhCurrency amount1, PhCurrency amount2) throws ArithmeticException {
        PhCurrency diffAbs = new PhCurrency();

        if (amount1.equals(amount2))
            return diffAbs;         // return ₱0.00

        // Subtract from the larger value
        diffAbs.setValue(amount1.compareTo(amount2) > 0 ? amount1 : amount2);
        diffAbs.subtract(amount1.compareTo(amount2) < 0 ? amount1 : amount2);

        return diffAbs;
    }

    /**
     * Multiply the PhCurrency value by the given factor.
     *
     * @param factor to multiply the value with.
     * @throws ArithmeticException Product peso value overflow.
     */
    public void multiplyBy(double factor) throws ArithmeticException {
        // Check for overflow
        if (factor > 1 ?
                this.mPesoAmount > PESO_MAX / factor :
                this.mPesoAmount < PESO_MIN / factor) {
            throw new ArithmeticException("Product peso value overflow.");
        }

        this.setValue(this.toDouble() * factor);
    }

    /**
     * Divides the PhCurrency by the given divisor.
     *
     * @param divisor to dived the value by.
     * @throws ArithmeticException Quotient peso value overflow.
     */
    public void divideBy(double divisor) throws ArithmeticException {
        // Check for overflow
        if (divisor > 0 ?
                divisor < 1 && this.mPesoAmount > PESO_MAX * divisor :
                divisor > -1 && this.mPesoAmount < PESO_MIN * divisor) {
            throw new ArithmeticException("Quotient peso value overflow.");
        }

        this.setValue(this.toDouble() / divisor);
    }
}











