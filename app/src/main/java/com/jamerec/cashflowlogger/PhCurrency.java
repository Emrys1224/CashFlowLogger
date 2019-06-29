package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Class for representing and handling operations for Philippine peso.
 * This is created as a solution for inaccuracy of decimals when
 * using double/float. This includes the methods for comparisons
 * and arithmetic operations for this data model.
 */
public class PhCurrency implements Parcelable {
    // The maximum integer that a double can represent without
    // losing precision is 2^53. Used as the maximum value for
    // storing the currency value. Since negative value for
    // double is same with the positive value with only the
    // sign bit changed, -PESO_MAX_VALUE is the minimum that can be stored.
    // This will be the range of values for this data model.
    // Values outside of these will cause an ArithmeticException.
    public final static long PESO_MAX_VALUE = (long) Math.pow(2, 53);
    public final static long PESO_MIN_VALUE = PESO_MAX_VALUE * -1;

    // Value is stored as amount times 100 to include the centavo part of the amount.
    private long mAmountX100;

    protected PhCurrency(Parcel in) {
        mAmountX100 = in.readLong();
    }

    public static final Creator<PhCurrency> CREATOR = new Creator<PhCurrency>() {
        @Override
        public PhCurrency createFromParcel(Parcel in) {
            return new PhCurrency(in);
        }

        @Override
        public PhCurrency[] newArray(int size) {
            return new PhCurrency[size];
        }
    };

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Constructors ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Create PhCurrency with a default value of ₱0.00.
     */
    public PhCurrency() {
        setValue(0);
    }

    /**
     * Create PhCurrency with the given value.
     *
     * @param amountX100 of the currency.
     */
    public PhCurrency(long amountX100) {
        setValue(amountX100);
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
     * @param amountX100 new value which X100 of the currency value.
     */
    public void setValue(long amountX100) {
        this.mAmountX100 = amountX100;
    }

    /**
     * Change the current value to the given value.
     *
     * @param amount new value.
     */
    public void setValue(double amount) {
        // Check if value is within the permissible range (-2^53 to 2^53)
        if (amount >= PESO_MAX_VALUE || amount <= PESO_MIN_VALUE)
            throw new IllegalArgumentException("Set peso value overflow. " +
                    "Consider donating the excess to 'Charity'. " +
                    "Contact jamerec1224@gmail.com for further instructions....");

        this.mAmountX100 = (long) Math.round(amount * 100);
    }

    /**
     * Copy the value from another amount.
     *
     * @param amount to be copied.
     */
    public void setValue(PhCurrency amount) {
        this.mAmountX100 = amount.mAmountX100;
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Getters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Get the amount as stored in the object which is X100 of the equivalent amount,
     * which is the same as the value stored in database.
     * @return amount times 100;
     */
    public long getAmountX100() {
        return this.mAmountX100;
    }

    /**
     * Get the amount in double.
     *
     * @return amount in double.
     */
    public double toDouble() {
        return (double)this.mAmountX100 / 100;
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

        String amountPhPesoStr = pesoCurrency.format(this.toDouble());
        String decimalStr = amountPhPesoStr.substring(amountPhPesoStr.lastIndexOf(".") + 1);

        if(decimalStr.length() > 2)
            amountPhPesoStr += ".00";
        if(decimalStr.length() == 1)
            amountPhPesoStr += "0";

        return amountPhPesoStr;
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
        if(this.mAmountX100 > amount.mAmountX100) return 1;
        if(this.mAmountX100 < amount.mAmountX100) return -1;
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
        return this.mAmountX100 == amount.mAmountX100;
    }

    /**
     * Checks if the amount is not zero
     *
     * @return false if not zero;
     * true if zero.
     */
    public boolean isZero() {
        return this.mAmountX100 == 0;
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Arithmetic Operations ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Adds the value from another PhCurrency.
     *
     * @param amount to be added.
     * @throws ArithmeticException Sum peso value overflow.
     */
    public void add(PhCurrency amount) throws ArithmeticException {
        double thisAmount = this.toDouble();
        double addAmount = amount.toDouble();

        // Check for overflow
        // Overflow conditions:
        //  ** When the sum of two positive values exceeds or equal to PESO_MAX_VALUE.
        //          A + B >= PESO_MAX_VALUE where A > 0 and B > 0
        //  ** When the sum of two negative values is less than or equal to PESO_MIN_VALUE.
        //          A + B <= PESO_MIN_VALUE where A < 0 and B < 0
        if (addAmount > 0 ?
                thisAmount >= PESO_MAX_VALUE - addAmount :
                thisAmount <= PESO_MIN_VALUE - addAmount) {
            throw new ArithmeticException("Sum peso value overflow. You're too rich for this app! XD");
        }

        this.mAmountX100 += amount.mAmountX100;
    }

    /**
     * Returns the sum of a set of PhCurrency.
     *
     * @param amountList to get the sum from.
     * @return a new PhCurrency whose value is the sum of the list.
     * @throws ArithmeticException Sum peso value overflow.
     */
    public static PhCurrency sum(ArrayList<PhCurrency> amountList) throws ArithmeticException {
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
        double thisAmount = this.toDouble();
        double subtractAmount = amount.toDouble();

        // Check for overflow
        // Overflow conditions:
        //  ** When the difference of a positive value subtracted from a negative value
        //      is less than or equal to PESO_MIN_VALUE.
        //          A - B <= PESO_MIN_VALUE where A < 0 and B > 0;
        //  ** When the difference of a negative value subtracted from a positive value
        //      exceeds or equal to PESO_MAX_VALUE.
        //          A - B >= PESO_MAX_VALUE where A > 0 and B < 0
        if (subtractAmount > 0 ?
                thisAmount <= PESO_MIN_VALUE + subtractAmount :
                thisAmount >= PESO_MAX_VALUE + subtractAmount) {
            throw new ArithmeticException("Difference peso value overflow. You're crazy!");
        }

        this.mAmountX100 -= amount.mAmountX100;
    }

    /**
     * Get the absolute difference between two PhCurrency values.
     *
     * @param amount1 the currency to compare with.
     * @param amount2 the currency to compare with.
     * @return new PhCurrency with value of the absolute difference.
     * @throws ArithmeticException Difference peso value overflow.
     */
    public static PhCurrency differenceAbsolute(PhCurrency amount1, PhCurrency amount2) throws ArithmeticException {
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
        double currentAmount = this.toDouble();

        // Check for overflow
        // Overflow conditions where A is the currency value and B is the factor:
        //  ** When the positive value multiplied by a positive factor exceeds or
        //      equal to PESO_MAX_VALUE.
        //          A * B >= PESO_MAX_VALUE where A > 0 and B > 0
        //  ** When the positive value multiplied by a negative factor is less
        //      than or equal to PESO_MIN_VALUE.
        //          A * B <= PESO_MIN_VALUE where A > 0 and B < 0
        //  ** When the negative value multiplied by a negative factor exceeds or
        //      equal to PESO_MAX_VALUE.
        //          A * B >= PESO_MAX_VALUE where A < 0 and B < 0
        //  ** When the negative value multiplied by a positive factor is less
        //      than or equal to PESO_MIN_VALUE.
        //          A * B <= PESO_MIN_VALUE where A < 0 and B > 0
        if (currentAmount > 0
                ? factor > 0 ?
                currentAmount >= PESO_MAX_VALUE / factor :
                factor != 0 && currentAmount >= PESO_MIN_VALUE / factor
                : factor < 0 ?
                currentAmount <= PESO_MAX_VALUE / factor :
                factor != 0 && currentAmount <= PESO_MIN_VALUE / factor
        ) {
            throw new ArithmeticException("Product peso value overflow.");
        }

        this.setValue(currentAmount * factor);
    }

    /**
     * Divides the PhCurrency by the given divisor.
     *
     * @param divisor to dived the value by.
     * @throws ArithmeticException Quotient peso value overflow.
     */
    public void divideBy(double divisor) throws ArithmeticException {
        double currentAmount = this.toDouble();

        // Check for overflow
        // Overflow conditions where A is the currency value and B is the divisor:
        //  ** When the positive value divided by a positive divisor exceeds or
        //      equal to PESO_MAX_VALUE.
        //          A / B >= PESO_MAX_VALUE where A > 0 and 1 > B > 0
        //  ** When the positive value divided by a negative divisor is less
        //      than or equal to PESO_MIN_VALUE.
        //          A / B <= PESO_MIN_VALUE where A > 0 and 0 > B > -1
        //  ** When the negative value divided by a negative divisor exceeds or
        //      equal to PESO_MAX_VALUE.
        //          A / B >= PESO_MAX_VALUE where A < 0 and 0 > B > -1
        //  ** When the negative value divided by a positive divisor is less
        //      than or equal to PESO_MIN_VALUE.
        //          A / B <= PESO_MIN_VALUE where A < 0 and 1 > B > 0
        if (divisor == 0 || currentAmount > 0 ?
                (divisor > 0 && divisor < 1 && currentAmount >= PESO_MAX_VALUE * divisor) ||
                        (divisor < 0 && divisor > -1 && currentAmount >= PESO_MIN_VALUE * divisor) :
                (divisor < 0 && divisor > -1 && currentAmount <= PESO_MAX_VALUE * divisor) ||
                        (divisor > 0 && divisor < 1 && currentAmount <= PESO_MIN_VALUE * divisor)
        ) {
            throw new ArithmeticException("Quotient peso value overflow.");
        }

        this.setValue(currentAmount / divisor);
    }

    public static PhCurrency averageOf(ArrayList<PhCurrency> amountSet) throws ArithmeticException {
        PhCurrency average = new PhCurrency();

        if (amountSet.isEmpty()) return average;

        average.setValue(PhCurrency.sum(amountSet));
        average.divideBy(amountSet.size());
//        System.out.println("Average: " + average.toDouble());

        return average;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mAmountX100);
    }
}
