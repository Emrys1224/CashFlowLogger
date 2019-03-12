package com.jamerec.cashflowlogger;

import org.junit.Test;

import static org.junit.Assert.*;

public class PhCurrencyTest {
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
    public void comparison_test() {
        PhCurrency val1 = new PhCurrency(1234d);
        PhCurrency val2 = new PhCurrency(12345d);
        PhCurrency val3 = new PhCurrency(12345d);

        assertEquals(-1, val1.compareTo(val2));
        assertEquals(0, val2.compareTo(val3));
        assertEquals(1, val2.compareTo(val1));
        assertFalse(val1.equals(val2));
        assertTrue(val2.equals(val3));
    }
}
