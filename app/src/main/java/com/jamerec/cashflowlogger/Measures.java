package com.jamerec.cashflowlogger;

import android.support.annotation.NonNull;

/**
 * Measures class is for handling units of measures which are to be sent to DB.
 * DB stores a TEXT value for fractions in look-up table and in REAL at the same
 * time thus the need for a way to easily check and convert from String form
 * of fraction to its "Double" value and vice versa.
 * On the other hand, if the value is in decimal format it is flagged as not a fraction
 * and stored as is. Same thing is done to fractions which can be reduced to a whole number.
 */
public class Measures {
    // Numeric format String such as 1, 1.11, 1 1/2, and 1/2 ....
    private static final String VALID_NUMBER = "\\d+(([.]\\d+)|(\\s\\d+)?[/]\\d+)?";

    private boolean mIsFraction;
    private int mWholeNum;
    private int mNumerator;
    private int mDenominator;
    private double mDecValue;

    // Just in case it is needed....
    Measures(double value) {
        this.mIsFraction = false;
        this.mDecValue = value;
        this.mWholeNum = 0;
        this.mNumerator = 0;
        this.mDenominator = 1;
    }

    /**
     * Stores a value from a numeric format String.
     *
     * @param value the numeric formatted String to be stored.
     * @throws IllegalArgumentException when the String is not in a valid numeric format;
     *                                  when the fraction formatted String has a zero as its denominator.
     */
    Measures(@NonNull String value) throws IllegalArgumentException {
        // Checks if the String is in a valid numeric pattern.
        // Throws an IllegalArgumentException if String is not valid.
        if (!value.matches(VALID_NUMBER)) {
            throw new IllegalArgumentException("This is not a valid numeric string.");
        }

        // "value" is in decimal format.
        if (!value.contains("/")) {
            this.mIsFraction = false;
            this.mDecValue = Double.parseDouble(value);
            this.mWholeNum = 0;
            this.mNumerator = 0;
            this.mDenominator = 1;
            return;
        }

        // Isolate the whole number by splitting the string at the space(\s) char and
        // assign to mWholeNum.
        String[] partialVal = value.split("\\s");
        if (partialVal.length > 1) {
            this.mWholeNum = Integer.parseInt(partialVal[0]);
            value = partialVal[1];
//			System.out.println("Whole Number: " + partialVal[0]);
//			System.out.println("Fractional Value: " + fraction);
        }

        // Isolate the remaining String by splitting at the slash(/) char to get the
        // numerator and the denominator and assign to mNumerator and mDenominator accordingly.
        String[] fractionParts = value.split("/");
//		System.out.println("Numerator: " + fractionParts[0]);
//		System.out.println("Denominator: " + fractionParts[1]);

        // Throws an IllegalArgumentException if the denominator is zero.
        int numerator = Integer.parseInt(fractionParts[0]);
        int denominator = Integer.parseInt(fractionParts[1]);
        if (denominator == 0)
            throw new IllegalArgumentException("The denominator cannot be zero!");

        // The fractional part is equal to 1.
        // I know... crazy right.
        if (numerator % denominator == 0) {
            this.mIsFraction = false;
            this.mNumerator = 0;
            this.mDenominator = 1;
            this.mDecValue = this.mWholeNum + (double) (numerator / denominator);
            this.mWholeNum = 0;
            return;
        }

        this.mIsFraction = true;
        this.mNumerator = numerator;
        this.mDenominator = denominator;
        reduce();

        this.mDecValue = this.mWholeNum + (this.mNumerator / (double) this.mDenominator);
    }

    Measures(@NonNull Measures value) {
        this.mIsFraction = value.mIsFraction;
        this.mWholeNum = value.mWholeNum;
        this.mNumerator = value.mNumerator;
        this.mDenominator = value.mDenominator;
        this.mDecValue = value.mDecValue;
    }

    boolean isFraction() {
        return this.mIsFraction;
    }

    int getWholeNumber() throws IllegalAccessException {
        if (!mIsFraction)
            throw new IllegalAccessException("Field not set for non-fraction Measures....");

        return this.mWholeNum;
    }

    int getNumerator() throws IllegalAccessException {
        if (!mIsFraction)
            throw new IllegalAccessException("Field not set for non-fraction Measures....");

        return this.mNumerator;
    }

    int getDenominator() throws IllegalAccessException {
        if (!mIsFraction)
            throw new IllegalAccessException("Field not set for non-fraction Measures....");

        return this.mDenominator;
    }

    double getDouble() {
        return this.mDecValue;
    }

    @Override
    @NonNull
    public String toString() {
        if (mIsFraction)
            return (this.mWholeNum != 0 ? this.mWholeNum + " " : "") + this.mNumerator + "/"
                    + this.mDenominator;

        return String.valueOf(this.mDecValue);
    }

    private void reduce() {
        // If mNumerator is greater than the mDenominator divide mNumerator by
        // mDenominator then add the answer to mWholeNum.
        if (this.mNumerator > this.mDenominator) {
            int wholeNum = this.mNumerator / this.mDenominator;
            this.mWholeNum += wholeNum;

            // Assign the remainder to the mNumerator.
            this.mNumerator = this.mNumerator % this.mDenominator;
        }

        // Get the Greatest Common Factor (GCF) of mNumerator and mDenominator.
        int gcf = findGCF(this.mNumerator, this.mDenominator);

        if (gcf > 1) {
            // Divide mNumerator by the GCF then change its value by the answer.
            this.mNumerator /= gcf;
            // Divide mDenominator by the GCF then change its value by the answer.
            this.mDenominator /= gcf;
        }
    }

    private int findGCF(int number1, int number2) {
//		System.out.println("number 1: " + number1 + "\t|| number 2: " + number2);
        // base case
        if (number2 == 0) {
//        	System.out.println("GCF: " + number1);
            return number1;
        }
        return findGCF(number2, number1 % number2);
    }
}
