package com.jamerec.cashflowlogger;

import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhCurrencyTest {
    private double DELTA = 0.009;

    @Test
    public void creation_is_correct() {
        DecimalFormat pesoCurrency =
                new DecimalFormat("₱ ###,###,###.##");

        double[] testNumSet = {
                12345.90D,
                2354.4546D,
                4534D,
                5745.06D,
                12423765.34D
        };

        for (double testNum : testNumSet) {
            long wholeNum = (long) testNum;

            String amtStr = new DecimalFormat("#.0#").format(testNum);
            String decimalStr = amtStr.substring(
                    amtStr.lastIndexOf(".") + 1) + 0;

            byte decimal = Byte.parseByte(
                    decimalStr.length() < 3 ?
                            decimalStr : decimalStr.substring(0, 2));

            String amtPhPFormat = String.format(
                    Locale.ENGLISH, "%s.%02d",
                    pesoCurrency.format(wholeNum), decimal);

            PhCurrency test = new PhCurrency(testNum);
            PhCurrency test2 = new PhCurrency(test);

            System.out.println(String.valueOf(testNum));
            System.out.println(test.toString() + "\n");

            assertEquals(wholeNum, test.getPesoAmount());
            assertEquals(decimal, test.getCentavoAmount());
            assertEquals(testNum, test.toDouble(), 0.009);
            assertEquals(amtPhPFormat, test.toString());
            assertEquals(test.toDouble(), test2.toDouble(), 0.009);
        }
    }

    @Test
    public void comparison_is_correct() {
        PhCurrency val1 = new PhCurrency(1234d);
        PhCurrency val2 = new PhCurrency(12345d);
        PhCurrency val3 = new PhCurrency(12345d);

        assertEquals(-1, val1.compareTo(val2));
        assertEquals(0, val2.compareTo(val3));
        assertEquals(1, val2.compareTo(val1));
        assertFalse(val1.equals(val2));
        assertTrue(val2.equals(val3));
    }

    @Test
    public void addition_is_correct() {
        double val1 = 12345.98D;
        double val2 = 52345.45D;
        double val3 = -3534.43D;
        double val4 = -345345.45;

        PhCurrency amt1 = new PhCurrency(val1);
        PhCurrency amt2 = new PhCurrency(val2);
        PhCurrency amt3 = new PhCurrency(val3);
        PhCurrency amt4 = new PhCurrency(val4);

        // Addition of two positive values
        amt1.add(amt2);
        System.out.println("amt1 += amt2: " + amt1.toDouble());
        val1 += val2;
        System.out.println("val1 += val2: " + val1 + "\n");
        assertEquals(val1, amt1.toDouble(), DELTA);

        // Add a negative value to a positive value
        amt1.add(amt3);
        System.out.println("amt1 += amt3: " + amt1.toDouble());
        val1 += val3;
        System.out.println("val1 += val3: " + val1 + "\n");
        assertEquals(val1, amt1.toDouble(), DELTA);

        // Add of two negative values
        amt3.add(amt4);
        System.out.println("amt3 += amt4: " + amt3.toDouble());
        val3 += val4;
        System.out.println("val3 += val4: " + val3 + "\n");
        assertEquals(val3, amt3.toDouble(), DELTA);

        // Add positive value to negative value
        amt3.add(amt2);
        System.out.println("amt3 += amt2: " + amt3.toDouble());
        val3 += val2;
        System.out.println("val3 += val2: " + val3 + "\n");
        assertEquals(val3, amt3.toDouble(), DELTA);
    }

    @Test
    public void subtraction_is_correct() {
        double val1 = 12345.98D;
        double val2 = 52345.45D;
        double val3 = -3534.43D;
        double val4 = -345345.45;

        PhCurrency amt1 = new PhCurrency(val1);
        PhCurrency amt2 = new PhCurrency(val2);
        PhCurrency amt3 = new PhCurrency(val3);
        PhCurrency amt4 = new PhCurrency(val4);

        // Addition of two positive values
        val1 -= val2;
        System.out.println("val1 -= val2: " + val1);
        amt1.subtract(amt2);
        System.out.println("amt1 -= amt2: " + amt1.toDouble());
        assertEquals(val1, amt1.toDouble(), 0.001);

        // Add a negative value to a positive value
        val1 -= val3;
        System.out.println("val1 -= val3: " + val1);
        amt1.subtract(amt3);
        System.out.println("amt1 -= amt3: " + amt1.toDouble());
        assertEquals(val1, amt1.toDouble(), 0.001);

        // Add of two negative values
        val3 -= val4;
        System.out.println("val3 -= val4: " + val3);
        amt3.subtract(amt4);
        System.out.println("amt3 -= amt4: " + amt3.toDouble());
        assertEquals(val3, amt3.toDouble(), 0.001);

        // Add positive value to negative value
        val3 -= val2;
        System.out.println("val3 -= val2: " + val3);
        amt3.subtract(amt2);
        System.out.println("amt3 -= amt2: " + amt3.toDouble());
        assertEquals(val3, amt3.toDouble(), 0.001);
    }

    @Test
    public void differenceAbsolute_is_correct() {
        double absDiffNum;
        PhCurrency absDiffVal = new PhCurrency();

        double[] testNumSet = {
                3145.54,
                567.56,
                -1334.5,
                -435.74
        };

        ArrayList<PhCurrency> testVal = new ArrayList<>();

        for (double testNum : testNumSet) {
            testVal.add(new PhCurrency(testNum));
        }

        // Absolute difference between positive numbers
        System.out.println("\nAbsolute difference between positive numbers, " +
                "\nwhere the first argument is larger than the second....");
        absDiffNum = differenceAbs(testNumSet[0], testNumSet[1]);
        System.out.println("AbsDiffNum1(" + testNumSet[0] + ", " + testNumSet[1] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(0), testVal.get(1)));
        System.out.println("AbsDiffVal1(" + testVal.get(0).toDouble() + ", " +
                testVal.get(1).toDouble() + ") = " + absDiffNum);

        // Absolute difference between positive numbers
        System.out.println("\nAbsolute difference between positive numbers, " +
                "\nwhere the second argument is larger than the first....");
        absDiffNum = differenceAbs(testNumSet[1], testNumSet[0]);
        System.out.println("AbsDiffNum1(" + testNumSet[1] + ", " + testNumSet[0] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(1), testVal.get(0)));
        System.out.println("AbsDiffVal1(" + testVal.get(1).toDouble() + ", " +
                testVal.get(0).toDouble() + ") = " + absDiffNum);

        // Absolute difference between negative numbers
        System.out.println("\nAbsolute difference between negative numbers, " +
                "\nwhere the first argument is larger than the second....");
        absDiffNum = differenceAbs(testNumSet[3], testNumSet[2]);
        System.out.println("AbsDiffNum1(" + testNumSet[3] + ", " + testNumSet[2] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(3), testVal.get(2)));
        System.out.println("AbsDiffVal1(" + testVal.get(3).toDouble() + ", " +
                testVal.get(2).toDouble() + ") = " + absDiffNum);

        // Absolute difference between negative numbers
        System.out.println("\nAbsolute difference between negative numbers, " +
                "\nwhere the second argument is larger than the first....");
        absDiffNum = differenceAbs(testNumSet[2], testNumSet[3]);
        System.out.println("AbsDiffNum1(" + testNumSet[2] + ", " + testNumSet[3] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(2), testVal.get(3)));
        System.out.println("AbsDiffVal1(" + testVal.get(2).toDouble() + ", " +
                testVal.get(3).toDouble() + ") = " + absDiffNum);

        // Absolute difference between positive and negative number
        System.out.println("\nAbsolute difference between positive and negative number....");
        absDiffNum = differenceAbs(testNumSet[0], testNumSet[3]);
        System.out.println("AbsDiffNum1(" + testNumSet[0] + ", " + testNumSet[3] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(0), testVal.get(3)));
        System.out.println("AbsDiffVal1(" + testVal.get(0).toDouble() + ", " +
                testVal.get(3).toDouble() + ") = " + absDiffNum);

        // Absolute difference between negative and positive number
        System.out.println("\nAbsolute difference between negative and positive number....");
        absDiffNum = differenceAbs(testNumSet[3], testNumSet[0]);
        System.out.println("AbsDiffNum1(" + testNumSet[3] + ", " + testNumSet[0] + ") = " + absDiffNum);
        absDiffVal.setValue(
                PhCurrency.differenceAbsolute(testVal.get(3), testVal.get(0)));
        System.out.println("AbsDiffVal1(" + testVal.get(3).toDouble() + ", " +
                testVal.get(0).toDouble() + ") = " + absDiffNum);
    }

    @Test
    public void multiplication_is_correct() {
        double ansTemp;
        double factor1 = 12.45D;
        double factor2 = -54.5;
        double baseVal1 = 1234.34;
        double baseVal2 = -6454.23;
        PhCurrency val1 = new PhCurrency(baseVal1);
        PhCurrency val2 = new PhCurrency(baseVal2);

        // Positive value multiplied by positive factor
        ansTemp = baseVal1 * factor1;
        System.out.println("baseVal1 *= factor1: " + ansTemp);
        val1.multiplyBy(factor1);
        System.out.println("val1 *= factor1: " + val1.toDouble());
        assertEquals(ansTemp, val1.toDouble(), DELTA);
        val1.setValue(baseVal1);        // reset value

        // Positive value multiplied by negative factor
        ansTemp = baseVal1 * factor2;
        System.out.println("baseVal1 *= factor2: " + ansTemp);
        val1.multiplyBy(factor2);
        System.out.println("val1 *= factor2: " + val1.toDouble());
        assertEquals(ansTemp, val1.toDouble(), DELTA);
        val1.setValue(baseVal1);        // reset value

        // Negative value multiplied by positive factor
        ansTemp = baseVal2 * factor1;
        System.out.println("baseVal2 *= factor1: " + ansTemp);
        val2.multiplyBy(factor1);
        System.out.println("val2 *= factor1: " + val2.toDouble());
        assertEquals(ansTemp, val2.toDouble(), DELTA);
        val2.setValue(baseVal2);        // reset value

        // Negative value multiplied by negative factor
        ansTemp = baseVal2 * factor2;
        System.out.println("baseVal2 *= factor2: " + ansTemp);
        val2.multiplyBy(factor2);
        System.out.println("val2 *= factor2: " + val2.toDouble());
        assertEquals(ansTemp, val2.toDouble(), DELTA);
        val2.setValue(baseVal2);        // reset value
    }

    @Test
    public void division_is_correct() {
        double ansTemp;
        double factor1 = 12.45D;
        double factor2 = -54.5;
        double baseVal1 = 1234.34;
        double baseVal2 = -6454.23;
        PhCurrency val1 = new PhCurrency(baseVal1);
        PhCurrency val2 = new PhCurrency(baseVal2);

        // Positive value divided by positive factor
        ansTemp = baseVal1 / factor1;
        System.out.println("baseVal1 /= factor1: " + ansTemp);
        val1.divideBy(factor1);
        System.out.println("val1 /= factor1: " + val1.toDouble());
        assertEquals(ansTemp, val1.toDouble(), DELTA);
        val1.setValue(baseVal1);        // reset value

        // Positive value multiplied by negative factor
        ansTemp = baseVal1 / factor2;
        System.out.println("baseVal1 /= factor2: " + ansTemp);
        val1.divideBy(factor2);
        System.out.println("val1 /= factor2: " + val1.toDouble());
        assertEquals(ansTemp, val1.toDouble(), DELTA);
        val1.setValue(baseVal1);        // reset value

        // Negative value multiplied by positive factor
        ansTemp = baseVal2 / factor1;
        System.out.println("baseVal2 /= factor1: " + ansTemp);
        val2.divideBy(factor1);
        System.out.println("val2 /= factor1: " + val2.toDouble());
        assertEquals(ansTemp, val2.toDouble(), DELTA);
        val2.setValue(baseVal2);        // reset value

        // Negative value multiplied by negative factor
        ansTemp = baseVal2 / factor2;
        System.out.println("baseVal2 /= factor2: " + ansTemp);
        val2.divideBy(factor2);
        System.out.println("val2 /= factor2: " + val2.toDouble());
        assertEquals(ansTemp, val2.toDouble(), DELTA);
        val2.setValue(baseVal2);        // reset value
    }

    @Test
    public void average_is_correct() {
        double average = 0;
        PhCurrency averageAmt = new PhCurrency();

        double[] valSet = {
                1234.4,
                1423.55,
                5675.23,
                6576.87
        };

        ArrayList<PhCurrency> amountSet = new ArrayList<>();

        for (double val : valSet) {
            amountSet.add(new PhCurrency(val));
            average += val;
            System.out.println(
                    "Amount to add D: " + val +
                            "\t\tRunning Sum D: " + average);
        }
        System.out.println("Sum Total D: " + average);

        average /= valSet.length;
        System.out.println("Average D: " + average);

        averageAmt.setValue(PhCurrency.averageOf(amountSet));

        assertEquals(average, averageAmt.toDouble(), 0.009);
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private double differenceAbs(double val1, double val2) {
        return val1 > val2 ?
                val1 - val2 :
                val2 -val1;
    }
}















