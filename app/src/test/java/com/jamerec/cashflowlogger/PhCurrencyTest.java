package com.jamerec.cashflowlogger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhCurrencyTest {
    private double DELTA = 0.009;
    @Test
    public void creation_is_correct() {
        double testNum = 12345.90d;
        PhCurrency test = new PhCurrency(testNum);
        PhCurrency test2 = new PhCurrency(test);

        assertEquals(12345, test.getPesoAmount());
        assertEquals(90, test.getCentavoAmount());
        assertEquals(testNum, test.toDouble(), 0.001);
        assertEquals("₱ 12,345.90", test.toString());
        assertEquals(test.toDouble(), test2.toDouble(), 0.001);
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
        val1 += val2;
        System.out.println("val1 += val2: " + val1);
        amt1.add(amt2);
        System.out.println("amt1 += amt2: " + amt1.toDouble());
        assertEquals(val1, amt1.toDouble(), DELTA);

        // Add a negative value to a positive value
        val1 += val3;
        System.out.println("val1 += val3: " + val1);
        amt1.add(amt3);
        System.out.println("amt1 += amt3: " + amt1.toDouble());
        assertEquals(val1, amt1.toDouble(), DELTA);

        // Add of two negative values
        val3 += val4;
        System.out.println("val3 += val4: " + val3);
        amt3.add(amt4);
        System.out.println("amt3 += amt4: " + amt3.toDouble());
        assertEquals(val3, amt3.toDouble(), DELTA);

        // Add positive value to negative value
        val3 += val2;
        System.out.println("val3 += val2: " + val3);
        amt3.add(amt2);
        System.out.println("amt3 += amt2: " + amt3.toDouble());
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
    public void multiplication_is_correct() {
        double ansTemp = 0D;
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
}















