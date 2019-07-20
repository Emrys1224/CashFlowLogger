package com.jamerec.cashflowlogger;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MeasuresTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_measures_test() {
        String[][] seedValues2 = {
                {"Single digit whole number", "1"},
                {"Multiple digit whole number", "1244"},
                {"Single digit and a decimal place", "3.5"},
                {"Multiple digit and decimal place", "4534.456"},
                {"A fraction with single digit in both numerator and denominator", "3/4"},
                {"A fraction with multiple digits in both numerator and denominator", "122/345"},
                {"A fraction with numerator greater than denominator", "35/6"},
                {"A fraction which can be reduced", "24/48"},
                {"A fraction where numerator and denominator are the same", "67/67"},
                {"A fraction with a whole number", "4 6/7"},
                {"A fraction with a whole number where the fractional part can be reduced", "7 75/125"},
                {"A fraction with a whole number where numerator and denominator are the same", "4 65/65"},
                {"A fraction with a whole number where the numerator is greater than the denominator", "12 156/36"},
                {"A fraction with a numerator that is a multiple of denominator", "34 39/13"}
        };

        System.out.println("\n~~~~~~~~~~~~~ Create Measures Object Test ~~~~~~~~~~~~~");

        for (String[] seedValue : seedValues2) {
            System.out.println(seedValue[0]);
            System.out.println("Seed value: " + seedValue[1]);

            int[] seedVal = createPseudoFractionObject(seedValue[1]);
            double seedToDouble;
            String seedString;
            Measures testVal = new Measures(seedValue[1]);

            // "seedValue" is in fraction format.
            if (seedVal.length == 3) {
                if (seedVal[2] != 1) {
                    seedToDouble = convertPseudoFractionToDouble(seedVal);
                    seedString = convertPseudoFractionToString(seedVal);

                    try {
                        assertTrue(testVal.isFraction());
                        assertEquals(seedVal[0], testVal.getWholeNumber());
                        assertEquals(seedVal[1], testVal.getNumerator());
                        assertEquals(seedVal[2], testVal.getDenominator());

                    } catch (IllegalAccessException iae) {
                        // Do nothing....
                    }
                }

                // "seedValue" has its numerator as a multiple of the denominator.
                else {
                    assertFalse(testVal.isFraction());

                    seedToDouble = (double) seedVal[0];
                    seedString = String.valueOf(seedToDouble);
                }

            }

            // "seedValue" is in decimal format.
            else {
                assertFalse(testVal.isFraction());

                seedToDouble = Double.parseDouble(seedValue[1]);
                seedString = String.valueOf(seedToDouble);
            }

            System.out.println("Final seed value: " + seedString);

            assertEquals(seedToDouble, testVal.getDouble(), 0.0001);
            assertEquals(seedString, testVal.toString());

            System.out.println();
        }
    }

    @Test
    public void exception_test_invalid_string() {
        System.out.println("\nException Test: Invalid Numeric String");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("This is not a valid numeric string.");

        String seedVal = "123.3a";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures value: " + testVal);
    }

    @Test
    public void exception_test_zero_denominator() {
        System.out.println("\nException Test: Zero Denominator");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The denominator cannot be zero!");

        String seedVal = "123/0";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures value: " + testVal);
    }

    @Test
    public void exception_test_illegal_accessA1() throws IllegalAccessException {
        System.out.println("\nException Test: Get the whole number of a fraction with numerator that is same as the denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "123/123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Whole Number: " + testVal.getWholeNumber());
    }

    @Test
    public void exception_test_illegal_accessA2() throws IllegalAccessException {
        System.out.println("\nException Test: Get the numerator of a fraction with numerator that is same as the denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "123/123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Numerator: " + testVal.getNumerator());
    }

    @Test
    public void exception_test_illegal_accessA3() throws IllegalAccessException {
        System.out.println("\nException Test: Get the denominator of a fraction with numerator that is same as the denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "123/123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Denominator: " + testVal.getDenominator());
    }

    @Test
    public void exception_test_illegal_accessB1() throws IllegalAccessException {
        System.out.println("\nException Test: Get the whole number of a fraction with numerator that is multiple of denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "99/11";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Whole Number: " + testVal.getWholeNumber());
    }

    @Test
    public void exception_test_illegal_accessB2() throws IllegalAccessException {
        System.out.println("\nException Test: Get the numerator of a fraction with numerator that is multiple of denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "99/11";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Numerator: " + testVal.getNumerator());
    }

    @Test
    public void exception_test_illegal_accessB3() throws IllegalAccessException {
        System.out.println("\nException Test: Get the denominator of a fraction with numerator that is multiple of denominator");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "99/11";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Denominator: " + testVal.getDenominator());
    }

    @Test
    public void exception_test_illegal_accessC1() throws IllegalAccessException {
        System.out.println("\nException Test: Get the whole number of a Measures from a decimal value");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "543.123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Whole Number: " + testVal.getWholeNumber());
    }

    @Test
    public void exception_test_illegal_accessC2() throws IllegalAccessException {
        System.out.println("\nException Test: Get the numerator of a Measures from a decimal value");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "543.123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Numerator: " + testVal.getNumerator());
    }

    @Test
    public void exception_test_illegal_accessC3() throws IllegalAccessException {
        System.out.println("\nException Test: Get the denominator of a Measures from a decimal value");

        thrown.expect(IllegalAccessException.class);
        thrown.expectMessage("Field not set for non-fraction Measures....");

        String seedVal = "543.123";
        System.out.printf("Seed value: %s\n", seedVal);

        Measures testVal = new Measures(seedVal);
        System.out.println("Measures String: " + testVal.toString());
        System.out.println("Measures Denominator: " + testVal.getDenominator());
    }

    @NonNull
    private int[] createPseudoFractionObject(@NonNull String value) {
        // Checks if the String is in a valid fraction pattern.
        // Returns an empty array if the fraction is not valid.
        if (!value.matches("\\d+((\\s\\d+)?[/]\\d+)")) {
            System.out.println("NOT A VALID FRACTION FORMAT!!!");
            return new int[]{};
        }

        int wholeNum = 0;
        int numerator;
        int denominator;

        // Isolate the whole number by splitting the string at the space(\s) char and
        // assign to mWholeNum.
        String[] partialVal = value.split("\\s");
        if (partialVal.length > 1) {
            wholeNum = Integer.parseInt(partialVal[0]);
            value = partialVal[1];
//			System.out.println("Whole Number: " + partialVal[0]);
//			System.out.println("Fractional Value: " + fraction);
        }

        // Isolate the remaining String by splitting at the slash(/) char to get the
        // numerator and the denominator and assign to mNumerator and mDenominator accordingly.
        String[] fractionParts = value.split("/");
//		System.out.println("Numerator: " + fractionParts[0]);
//		System.out.println("Denominator: " + fractionParts[1]);

        // Returns an empty array if the denominator is zero or the numerator and denominators are the same.
        numerator = Integer.parseInt(fractionParts[0]);
        denominator = Integer.parseInt(fractionParts[1]);
        if (denominator == 0) {
            System.out.println("NOT A VALID FRACTION FORMAT!!!");
            return new int[]{};
        }

        if (numerator >= denominator && (numerator % denominator == 0)) {
            System.out.println("NOT A VALID FRACTION FORMAT!!!");
            wholeNum += numerator / denominator;
            numerator = 0;
            denominator = 1;
            return new int[]{wholeNum, numerator, denominator};
        }

        return reduceFraction(new int[]{wholeNum, numerator, denominator});
    }

    @NonNull
    @Contract("_ -> new")
    private int[] reduceFraction(@NonNull int[] fraction) {
        int wholeNum = fraction[0];
        int numerator = fraction[1];
        int denominator = fraction[2];

        // If numerator is greater than the denominator divide numerator by
        // denominator then add the answer to mWholeNum.
        if (numerator > denominator) {
            wholeNum += numerator / denominator;

            // Assign the remainder to the numerator.
            numerator = numerator % denominator;
        }

        // Get the Greatest Common Factor (GCF) of numerator and denominator.
        int gcf = findGCF(numerator, denominator);

        if (gcf > 1) {
            // Divide numerator by the GCF then change its value by the answer.
            numerator /= gcf;
            // Divide denominator by the GCF then change its value by the answer.
            denominator /= gcf;
        }

        return new int[]{wholeNum, numerator, denominator};
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

    @Contract(pure = true)
    private double convertPseudoFractionToDouble(@NonNull int[] fraction) {
        return fraction[0] + (fraction[1] / (double) fraction[2]);
    }

    @NonNull
    @Contract(pure = true)
    private String convertPseudoFractionToString(@NonNull int[] seedVal) {
        return (seedVal[0] != 0 ? seedVal[0] + " " : "") + seedVal[1] + "/"
                + seedVal[2];
    }
}