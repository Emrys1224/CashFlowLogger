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
        String[][] seedUnits = {
                {"Simple unit of measure", "m"},
                {"Whole word unit of measure", "meter"},
                {"A measure with space between numerical part and its unit", " gallons"},
                {"Unit of measure with a numerical and caret(^) character", "m^2"},
                {"Unit of measure with a period(.) character", "oz."},
                {"Unit of measure with a slash(/) character", "packs/box"},
                {"Unit of measure that uses Greek character", "μm"}
        };

        String[][] seedValues = {
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

        System.out.println("\n~~~~~~~~~~~~~ Create Measures Object Test ~~~~~~~~~~~~~\n");

        for (String[] seedUnit : seedUnits) {
            System.out.println(">>>" + seedUnit[0]);
            System.out.println("Seed unit: " + seedUnit[1] + "\n");

            for (String[] seedValue : seedValues) {
                System.out.println("===== " + seedValue[0]);

                int[] seedVal = createPseudoFractionObject(seedValue[1]);
                double seedToDouble;
                String seedString = seedValue[1] + seedUnit[1];
                System.out.println("Seed value: " + seedString);

                Measures testVal = null;
                try {
                    testVal = new Measures(seedString);
                } catch (Measures.InvalidValueException e) {
                    e.printStackTrace();
                } catch (Measures.NoValidUnitException e) {
                    e.printStackTrace();
                } catch (Measures.ZeroDenominatorException e) {
                    e.printStackTrace();
                }

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
                            assertEquals(seedString, testVal.getFractionString());

                        } catch (IllegalAccessException iae) {
                            // Do nothing....
                        }

                        seedString += seedUnit[1];
                    }

                    // "seedValue" has its numerator as a multiple of the denominator.
                    else {
                        try {
                            assertFalse(testVal.isFraction());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        seedToDouble = (double) seedVal[0];
                        seedString = seedVal[0] + seedUnit[1];
                    }

                }

                // "seedValue" is in decimal format.
                else {
                    try {
                        assertFalse(testVal.isFraction());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    seedToDouble = Double.parseDouble(seedValue[1]);
                    seedString = seedValue[1] + seedUnit[1];
                }

                seedUnit[1] = seedUnit[1].replaceAll(" ", "");
                System.out.println("Final seed value: " + seedString);

                try {
                    assertEquals(seedToDouble, testVal.getDouble(), 0.0001);
                    assertEquals(seedUnit[1], testVal.getUnitOfMeasure());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                assertEquals(seedString, testVal.toString());

                System.out.println();
            }
        }
    }

    @Test
    public void catch_NoValidUnitException_test_1() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: No given unit of measure ~~~~~";
        String sourceString = "123.23";
        Class<? extends Throwable> exceptionType = Measures.NoValidUnitException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a unit of measure.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_NoValidUnitException_test_2() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: Invalid unit of measure (starts with a space followed by a number) ~~~~~";
        String sourceString = "123.23 23a";
        Class<? extends Throwable> exceptionType = Measures.NoValidUnitException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a unit of measure.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_NoValidUnitException_test_3() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: More than one space before unit of measure ~~~~~";
        String sourceString = "123.23  tons";
        Class<? extends Throwable> exceptionType = Measures.NoValidUnitException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a unit of measure.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_NoValidUnitException_test_4() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: Invalid character before unit of measure ~~~~~";
        String sourceString = "123/23/tons";
        Class<? extends Throwable> exceptionType = Measures.NoValidUnitException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a unit of measure.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_NoValidUnitException_test_5() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: Invalid character inside unit of measure ~~~~~";
        String sourceString = "123.23ton$";
        Class<? extends Throwable> exceptionType = Measures.NoValidUnitException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a unit of measure.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_InvalidValueException_test_1() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: No numeric value ~~~~~";
        String sourceString = "meters";
        Class<? extends Throwable> exceptionType = Measures.InvalidValueException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a numeric value.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_InvalidValueException_test_2() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: Invalid character before the numeric value ~~~~~";
        String sourceString = "a123meters";
        Class<? extends Throwable> exceptionType = Measures.InvalidValueException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "Invalid String! Measures should have a numeric value.";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_ZeroDenominatorException_test() throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        String description = "~~~~~ Exception Test: Invalid character before the numeric value ~~~~~";
        String sourceString = "42/0lbs";
        Class<? extends Throwable> exceptionType = Measures.ZeroDenominatorException.class;
        String exceptionMessage = "Source Value: " + sourceString + "\n" + "The denominator cannot be zero!";

        setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);
    }

    @Test
    public void catch_ClearedStateException_isFraction_test() throws
            Measures.ClearedStateException {

        String description = "~~~~~ Exception Test: Call isFraction() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        boolean testBool = test.isFraction();
        System.out.println("isFraction returns: " + testBool);
    }

    @Test
    public void catch_ClearedStateException_getWholeNum_test() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getWholeNumber() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getWholeNumber();
        System.out.println("getWholeNumber returns: " + testInt);
    }

    @Test
    public void catch_ClearedStateException_getNumerator_test() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getNumerator() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getNumerator();
        System.out.println("getWholeNumber returns: " + testInt);
    }

    @Test
    public void catch_ClearedStateException_getDenominator_test() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getDenominator() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getDenominator();
        System.out.println("getWholeNumber returns: " + testInt);
    }

    @Test
    public void catch_ClearedStateException_getDouble_test() throws
            Measures.ClearedStateException {

        String description = "~~~~~ Exception Test: Call getDouble() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        double testDouble = test.getDouble();
        System.out.println("getWholeNumber returns: " + testDouble);
    }

    @Test
    public void catch_ClearedStateException_getFractionString_test() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getFractionString() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        String testFractionString = test.getFractionString();
        System.out.println("getWholeNumber returns: " + testFractionString);
    }

    @Test
    public void catch_ClearedStateException_getUnitOfMeasure_test() throws
            Measures.ClearedStateException {

        String description = "~~~~~ Exception Test: Call getUnitOfMeasure() from empty source String ~~~~~";
        String sourceString = "";
        Class<? extends Throwable> exceptionType = Measures.ClearedStateException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "No value set. This is in CLEARED state!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        String testUnitOfMeasure = test.getUnitOfMeasure();
        System.out.println("getWholeNumber returns: " + testUnitOfMeasure);
    }

    @Test
    public void catch_NotFractionException_getWholeNum_test1() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getWholeNumber() from source String with whole number numeric value ~~~~~";
        String sourceString = "1l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "This is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getWholeNumber();
        System.out.println("getWholeNumber returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getNumerator_test1() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getNumerator() from source String with whole number numeric value ~~~~~";
        String sourceString = "1l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "This is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getNumerator();
        System.out.println("getNumerator returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getDenominator_test1() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getDenominator() from source String with whole number numeric value ~~~~~";
        String sourceString = "1l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: " + sourceString + "\n" + "This is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getDenominator();
        System.out.println("getDenominator returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getFractionString_test1() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getFractionString() from source String with whole number numeric value ~~~~~";
        String sourceString = "1l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: 1l\nThis is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        String testFractionString = test.getFractionString();
        System.out.println("getFractionString returns: " + testFractionString);
    }

    @Test
    public void catch_NotFractionException_getWholeNum_test2() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getWholeNumber() from source String with ~~~~~\n" +
                "~~~~~~~~~ fraction numeric value that is reduced to whole number ~~~~~~~~";
        String sourceString = "1 234/234l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: 2l\nThis is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getWholeNumber();
        System.out.println("getWholeNumber returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getNumerator_test2() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getNumerator() from source String with ~~~~~\n" +
                             "~~~~~~~~ fraction numeric value that is reduced to whole number ~~~~~~~";
        String sourceString = "1 234/234l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: 2l\nThis is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getNumerator();
        System.out.println("getNumerator returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getDenominator_test2() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getDenominator() from source String with ~~~~~\n" +
                             "~~~~~~~~~ fraction numeric value that is reduced to whole number ~~~~~~~~";
        String sourceString = "1 234/234l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: 2l\nThis is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        int testInt = test.getDenominator();
        System.out.println("getDenominator returns: " + testInt);
    }

    @Test
    public void catch_NotFractionException_getFractionString_test2() throws
            Measures.ClearedStateException,
            Measures.NotFractionException {

        String description = "~~~~~ Exception Test: Call getFractionString() from source String with ~~~~~\n" +
                             "~~~~~~~~~~ fraction numeric value that is reduced to whole number ~~~~~~~~~~";
        String sourceString = "1 234/234l";
        Class<? extends Throwable> exceptionType = Measures.NotFractionException.class;
        String exceptionMessage = "Source String: 2l\nThis is not from a fraction formatted String!";
        Measures test = setupMeasuresExceptionTest(description, sourceString, exceptionType, exceptionMessage);

        String testFractionString = test.getFractionString();
        System.out.println("getFractionString returns: " + testFractionString);
    }

    private Measures setupMeasuresExceptionTest(String description, String sourceString,
                                                Class<? extends Throwable> exceptionType, String exceptionMessage) {
        System.out.println("\n" + description);
        System.out.println("Source String: " + sourceString);

        thrown.expect(exceptionType);
        thrown.expectMessage(exceptionMessage);

        Measures testVal = new Measures(sourceString);
        System.out.println("Measures value: " + testVal);

        return testVal;
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