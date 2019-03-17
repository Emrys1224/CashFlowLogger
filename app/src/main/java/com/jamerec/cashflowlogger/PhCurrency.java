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

    // The maximum integer that a double can represent without
    // losing precision is 2^53. Used as the maximum value for
    // storing the currency value. Since negative value for
    // double is same with the positive value with only the
    // sign bit changed, -PESO_MAX_VALUE is the minimum that can be stored.
    // This will be the range of values for this data model.
    // Values outside of these will cause an ArithmeticException.
    public final static long PESO_MAX_VALUE = (long) Math.pow(2, 53);
    public final static long PESO_MIN_VALUE = PESO_MAX_VALUE * -1;

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
        // Check if value is within the permissible range (-2^53 to 2^53)
        if (amount >= PESO_MAX_VALUE || amount <= PESO_MIN_VALUE)
            throw new IllegalArgumentException("Set peso value overflow. " +
                    "Consider donating the excess to 'Charity'. " +
                    "Contact jamerec1224@gmail.com for further instructions....");

        String amtStr = new DecimalFormat("#.0#").format(amount);
        String decimalStr = amtStr.substring(
                amtStr.lastIndexOf(".") + 1) + 0;   // Append 0 for values such as n.m0 (e.g. 3.90, 45.00, etc)

//        System.out.println("Amount: " + amtStr);
//        System.out.println("Decimal: " + decimalStr);

        this.mPesoAmount = (long) amount;
        this.mCentavoAmount = Byte.parseByte(
                decimalStr.length() < 3 ?
                        decimalStr : decimalStr.substring(0, 2));

        if (amount < 1)
            this.mCentavoAmount *= -1;      // For negative values
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
                pesoCurrency.format(mPesoAmount),
                mCentavoAmount > 0 ? mCentavoAmount : -mCentavoAmount);
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
        if (centDiff > 0) return 1;     // this is greater than compared value
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
        // Overflow conditions:
        //  ** When the sum of two positive values exceeds or equal to PESO_MAX_VALUE.
        //          A + B >= PESO_MAX_VALUE where A > 0 and B > 0
        //  ** When the sum of two negative values is less than or equal to PESO_MIN_VALUE.
        //          A + B <= PESO_MIN_VALUE where A < 0 and B < 0
        if (amountPeso > 0 ?
                this.mPesoAmount >= PESO_MAX_VALUE - amountPeso :
                this.mPesoAmount <= PESO_MIN_VALUE - amountPeso
        ) {
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
        // Overflow conditions:
        //  ** When the difference of a positive value subtracted from a negative value
        //      is less than or equal to PESO_MIN_VALUE.
        //          A - B <= PESO_MIN_VALUE where A < 0 and B > 0;
        //  ** When the difference of a negative value subtracted from a positive value
        //      exceeds or equal to PESO_MAX_VALUE.
        //          A - B >= PESO_MAX_VALUE where A > 0 and B < 0
        if (amountPeso > 0 ?
                this.mPesoAmount <= PESO_MIN_VALUE + amountPeso :
                this.mPesoAmount >= PESO_MAX_VALUE + amountPeso
        ) {
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
    public static PhCurrency differenceAbsolute(@NonNull PhCurrency amount1, PhCurrency amount2) throws ArithmeticException {
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
                currentAmount >= PESO_MIN_VALUE / factor
                : factor < 0 ?
                currentAmount <= PESO_MAX_VALUE / factor :
                currentAmount <= PESO_MIN_VALUE / factor
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
        if (currentAmount > 0 ?
                (divisor > 0 && divisor <  1 && currentAmount >= PESO_MAX_VALUE * divisor) ||
                (divisor < 0 && divisor > -1 && currentAmount >= PESO_MIN_VALUE * divisor) :
                (divisor < 0 && divisor > -1 && currentAmount <= PESO_MAX_VALUE * divisor) ||
                (divisor > 0 && divisor <  1 && currentAmount <= PESO_MIN_VALUE * divisor)
        ) {
            throw new ArithmeticException("Quotient peso value overflow.");
        }

        this.setValue(currentAmount / divisor);
    }

    public static PhCurrency averageOf(@NonNull ArrayList<PhCurrency> amountSet) throws ArithmeticException {
        PhCurrency average = new PhCurrency();

        if (amountSet.isEmpty()) return average;

        for (int i = 0; i < amountSet.size(); i++) {
            average.add(amountSet.get(i));
            System.out.println(
                    "Amount to add:" + amountSet.get(i).toDouble() +
                            "\t\tRunning Sum: " + average.toDouble());

        }
        System.out.println("Sum Total: " + average.toDouble());

        average.divideBy(amountSet.size());
        System.out.println("Average: " + average.toDouble());

        return average;
    }
}











