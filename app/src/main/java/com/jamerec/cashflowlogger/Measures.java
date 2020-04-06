package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Measures class is for handling units of measures which are to be sent to DB.
 * DB stores a TEXT value for fractions in look-up table and in REAL at the same
 * time thus the need for a way to easily check and convert from String form
 * of fraction to its "Double" value and vice versa.
 * On the other hand, if the value is in decimal format it is flagged as not a fraction
 * and stored as is. Same thing is done to fractions which can be reduced to a whole number.
 */
class Measures implements Parcelable {
    // Measures has no value stored.
    private static final String CLEARED = "";

    // For decimal numeric String format such as 1, 123, 123.4, etc....
    private static final Pattern DEC_NUM_PATTERN = Pattern.compile("^\\d+([.]\\d+)?");
    // For fraction numeric format such as 1/2, 1 2/3, etc....
    private static final Pattern FRACTION_PATTERN = Pattern.compile("^\\d+( \\d+)?[/]\\d+");
    // For unit of measures with/without a single space after the numeric value, and those which uses
    // Greek characters, a caret, and slash ... such as 1m, 1meter, 1μm, 1m^2, 1m/s, etc....
    private static final Pattern UNIT_PATTERN = Pattern.compile("^[ ]?[a-zA-Zα-ωΑ-Ω]+[a-zA-Zα-ωΑ-Ω0-9./^]*$");

    private int mWholeNum;      // The whole number part of the fraction. Zero for decimal and value less than 1.
    private int mNumerator;     // The numerator part of the fraction. Zero for decimal format source String.
    private int mDenominator;   // The numerator part of the fraction. One for decimal format source String.
    private double mDecValue;   // The decimal value of the measured unit.
    private String mUnit;       // The unit of measurement. Empty when the value is cleared.

    /**
     * Creates a Measures object in "CLEARED" state.
     */
    Measures() {
        clear();
    }

    /**
     * Creates a Measures object from a source String.
     *
     * @param value is the source String to derive the values from.
     * @throws InvalidValueException    if there is valid numeric value found.
     * @throws NoValidUnitException     if there is no valid unit of measure found.
     * @throws ZeroDenominatorException if the denominator of the fractional part of the source String is zero.
     */
    Measures(@NonNull String value) throws
            InvalidValueException, NoValidUnitException, ZeroDenominatorException {

        setValue(value);
    }

    /**
     * Creates a deep copy of the given Measures object.
     *
     * @param value the Measures object to copied.
     */
    Measures(@NonNull Measures value) {
        setValue(value);
    }

    protected Measures(Parcel in) {
        mWholeNum = in.readInt();
        mNumerator = in.readInt();
        mDenominator = in.readInt();
        mDecValue = in.readDouble();
        mUnit = in.readString();
    }

    public static final Creator<Measures> CREATOR = new Creator<Measures>() {
        @Override
        public Measures createFromParcel(Parcel in) {
            return new Measures(in);
        }

        @Override
        public Measures[] newArray(int size) {
            return new Measures[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mWholeNum);
        dest.writeInt(mNumerator);
        dest.writeInt(mDenominator);
        dest.writeDouble(mDecValue);
        dest.writeString(mUnit);
    }

    /**
     * Set a new value from a source String.
     *
     * @param value is the source String to derive the values from.
     * @throws InvalidValueException    if there is no valid numeric value found.
     * @throws NoValidUnitException     if there is no valid unit of measure found.
     * @throws ZeroDenominatorException if the denominator of the fractional part of the source String is zero.
     */
    void setValue(@NonNull String value) throws
            InvalidValueException, NoValidUnitException, ZeroDenominatorException {

//        System.out.println("Source String: " + value);

        // Clear Measures value.
        if (value.isEmpty()) {
            clear();
            return;
        }

        // Also serves as flag to be set later to determine if the numeric value is
        // a fraction or decimal format.
        this.mDenominator = 0;

        // Extract numeric value.
        String sizeVal;

        // Fractional value
        Matcher fractionMatcher = FRACTION_PATTERN.matcher(value);
        if (fractionMatcher.find()) {
            sizeVal = fractionMatcher.group();
//            System.out.println("(f)Numeric value obtained: " + sizeVal);

        }

        // Decimal value
        else {
            Matcher decimalMatcher = DEC_NUM_PATTERN.matcher(value);
            if (decimalMatcher.find()) {
                sizeVal = decimalMatcher.group();
//                System.out.println("(d)Numeric value obtained: " + sizeVal);

                this.mWholeNum = 0;
                this.mNumerator = 0;
                this.mDenominator = 1;
                this.mDecValue = Double.parseDouble(sizeVal);
            }

            // Invalid String, no numeric value.
            else
                throw new InvalidValueException(value);
        }

        // Extract the unit of measurement.
        Matcher unitMatcher = UNIT_PATTERN.matcher(value.replaceAll(sizeVal, ""));
        if (unitMatcher.find()) {
            this.mUnit = unitMatcher.group();
//            System.out.println("Unit obtained: " + this.mUnit);
        }

        // Invalid String, no unit of measure found.
        else
            throw new NoValidUnitException(value);

        // Measure is of a decimal value.
        if (this.mDenominator == 1)
            return;

        // Isolate the whole number by splitting the string at the space(\s) char and
        // assign to mWholeNum.
        String[] partialVal = sizeVal.split("\\s");
        if (partialVal.length > 1) {
            this.mWholeNum = Integer.parseInt(partialVal[0]);
            sizeVal = partialVal[1];
//            System.out.println("Whole Number: " + partialVal[0]);
//            System.out.println("Fractional Value: " + partialVal[1]);
        }

        // Isolate the remaining String by splitting at the slash(/) char to get the
        // numerator and the denominator and assign to mNumerator and mDenominator accordingly.
        String[] fractionParts = sizeVal.split("/");
//        System.out.println("Numerator: " + fractionParts[0]);
//        System.out.println("Denominator: " + fractionParts[1]);

        int numerator = Integer.parseInt(fractionParts[0]);
        int denominator = Integer.parseInt(fractionParts[1]);

        // The denominator is zero.
        if (denominator == 0)
            throw new ZeroDenominatorException(value);

        // The fractional part is equal to a whole number.
        // I know... crazy right.
        if (numerator % denominator == 0) {
            this.mDecValue = this.mWholeNum + (double) (numerator / denominator);
            this.mWholeNum = 0;
            this.mNumerator = 0;
            this.mDenominator = 1;
            return;
        }

        this.mNumerator = numerator;
        this.mDenominator = denominator;
        reduce();

        this.mDecValue = this.mWholeNum + (this.mNumerator / (double) this.mDenominator);
    }

    /**
     * Copy the value from a Measures object.
     *
     * @param value the Measures object to copied.
     */
    void setValue(@NonNull Measures value) {
        this.mWholeNum = value.mWholeNum;
        this.mNumerator = value.mNumerator;
        this.mDenominator = value.mDenominator;
        this.mDecValue = value.mDecValue;
        this.mUnit = value.mUnit;
    }

    /**
     * Clears the value of this Measures object including the unit of measure.
     */
    void clear() {
        this.mWholeNum = 0;
        this.mNumerator = 0;
        this.mDenominator = 1;
        this.mDecValue = 0;
        this.mUnit = "";
    }

    /**
     * A method to check if there is a value stored in this object.
     *
     * @return true if no value is stored, false otherwise.
     */
    boolean isCleared() {
        return this.mUnit.isEmpty();
    }

    /**
     * A method to check if the stored value was from a fraction formatted numeric String.
     *
     * @return true is the source String was a fraction, false otherwise.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     */
    boolean isFraction() throws ClearedStateException {
        if (isCleared())
            throw new ClearedStateException();

        return this.mDenominator != 1;
    }

    /**
     * A method to get the whole number value for fraction formatted source String.
     *
     * @return the whole number from the fraction formatted numerical string.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     * @throws NotFractionException  if the source String was from a decimal format String.
     */
    int getWholeNumber() throws ClearedStateException, NotFractionException {
        if (!isFraction())
            throw new NotFractionException();

        return this.mWholeNum;
    }

    /**
     * A method to get the numerator value for fraction formatted source String.
     *
     * @return the numerator from the fraction formatted numerical string.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     * @throws NotFractionException  if the source String was from a decimal format String.
     */
    int getNumerator() throws ClearedStateException, NotFractionException {
        if (!isFraction())
            throw new NotFractionException();

        return this.mNumerator;
    }

    /**
     * A method to get the denominator value for fraction formatted source String.
     *
     * @return the denominator from the fraction formatted numerical string.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     * @throws NotFractionException  if the source String was from a decimal format String.
     */
    int getDenominator() throws ClearedStateException, NotFractionException {
        if (!isFraction())
            throw new NotFractionException();

        return this.mDenominator;
    }

    /**
     * A method to get the decimal equivalent value.
     *
     * @return the decimal equivalent value.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     */
    double getDouble() throws ClearedStateException {
        if (isCleared())
            throw new ClearedStateException();

        return this.mDecValue;
    }

    /**
     * A method to get the fraction formatted value.
     *
     * @return the fraction formatted value.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     * @throws NotFractionException  if the source String was from a decimal format String.
     */
    String getFractionString() throws ClearedStateException, NotFractionException {
        if (isCleared())
            throw new ClearedStateException();

        if (!isFraction())
            throw new NotFractionException();

        return (this.mWholeNum == 0 ? "" : this.mWholeNum + " ")
                + this.mNumerator
                + "/"
                + this.mDenominator;
    }

    /**
     * A method to get the unit of measure given from the source String.
     *
     * @return the unit of measure.
     * @throws ClearedStateException if the Measures object is in "CLEARED" state.
     */
    String getUnitOfMeasure() throws ClearedStateException {
        if (isCleared())
            throw new ClearedStateException();

        return this.mUnit.replaceAll(" ", "");
    }

    @Override
    @NonNull
    public String toString() {
        try {
            if (isFraction())
                return (this.mWholeNum == 0 ? "" : this.mWholeNum + " ")
                        + this.mNumerator
                        + "/"
                        + this.mDenominator
                        + this.mUnit;

        } catch (ClearedStateException iae) {
            return CLEARED;
        }

        return (this.mDecValue == Math.floor(this.mDecValue)
                ? String.format(Locale.ENGLISH, "%1.0f", this.mDecValue) : this.mDecValue)
                + this.mUnit;
    }

    /**
     * This changes to value of mNumerator and mDenominator to its lowest form
     * as a fraction.
     */
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

    /**
     * This calculates the Greatest Common Factor of two numbers. This is
     * used in converting the mNumerator and mDenominator to its lowest form.
     *
     * @param number1 to calculate the greatest common factor with.
     * @param number2 to calculate the greatest common factor with.
     * @return the Greatest Common Factor.
     */
    @Contract(pure = true)
    private int findGCF(int number1, int number2) {
//		System.out.println("number 1: " + number1 + "\t|| number 2: " + number2);
        // base case
        if (number2 == 0) {
//        	System.out.println("GCF: " + number1);
            return number1;
        }
        return findGCF(number2, number1 % number2);
    }

    /**
     * Thrown when the source String does not have a properly formatted numeric
     * value (decimal format such as 1, 1.1, etc., or fraction format such as
     * 1/2, 1 2/3, etc.)
     */
    class InvalidValueException extends IllegalArgumentException {
        private final String MESSAGE = "Invalid String! Measures should have a numeric value.";
        private String mSource;

        InvalidValueException(String source) {
            this.mSource = source;
        }

        @Override
        public String getMessage() {
            return "Source String: " + mSource + "\n" + this.MESSAGE;
        }
    }

    /**
     * Thrown when the source String does not have a properly formatted unit
     * of measure such as m, meter, m^2, m/s, yd., etc...
     */
    class NoValidUnitException extends IllegalArgumentException {
        private final String MESSAGE = "Invalid String! Measures should have a unit of measure.";
        private String mSource;

        NoValidUnitException(String source) {
            this.mSource = source;
        }

        @Override
        public String getMessage() {
            return "Source String: " + mSource + "\n" + this.MESSAGE;
        }
    }

    /**
     * Thrown when the source String which have a fraction formatted numeric value
     * have zero as its denominator.
     */
    class ZeroDenominatorException extends IllegalArgumentException {
        private final String MESSAGE = "The denominator cannot be zero!";
        private String mFraction;

        ZeroDenominatorException(String fraction) {
            this.mFraction = fraction;
        }

        @Override
        public String getMessage() {
            return "Source Value: " + mFraction + "\n" + this.MESSAGE;
        }
    }

    /**
     * Thrown when calling methods for instance of Measures where the source String's
     * numeric value was not a fraction formatted String.
     */
    class NotFractionException extends IllegalAccessException {
        private final String MESSAGE = "This is not from a fraction formatted String!";

        NotFractionException() {
        }

        @Override
        public String getMessage() {
            return "Source String: " + Measures.this.toString() + "\n" + this.MESSAGE;
        }
    }

    /**
     * Thrown when calling methods for instance of Measures that are in CLEARED state.
     */
    class ClearedStateException extends IllegalAccessException {
        private final String MESSAGE = "No value set. This is in CLEARED state!";

        ClearedStateException() {
        }

        @Override
        public String getMessage() {
            return "Source String: " + Measures.this.toString() + "\n" + this.MESSAGE;
        }
    }
}
