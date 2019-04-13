package com.jamerec.cashflowlogger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        long num1 = 1234L;
        long num2 = 5678L;
        long num3 = -1234L;
        long num4 = -5678L;

        System.out.println("Long min: " + Long.MIN_VALUE);
        System.out.println("Long max: " + Long.MAX_VALUE);
        System.out.println();

        System.out.println("Long min+num1: " + (Long.MIN_VALUE + num1));
        System.out.println("Long max+num1: " + (Long.MAX_VALUE + num1));
        System.out.println();

        System.out.println("Long min-num1: " + (Long.MIN_VALUE - num1));
        System.out.println("Long max-num1: " + (Long.MAX_VALUE - num1));
        System.out.println();

        assertEquals(4, 2 + 2);
    }

    @Test
    public void num_format_test() {
        int num1 = 123;
        int num2 = 90;
        int num3 = 8;
        int num4 = 0;

        String printNum = String.format(
                Locale.ENGLISH, "%d.%02d, %d.%02d, %d.%02d",
                num1, num2, num1, num3, num1, num4
        );

        System.out.println(printNum);

        assertTrue(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void creation_exception_test() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Set peso value overflow." +
                " Consider donating the excess to 'Charity'." +
                " Contact jamerec1224@gmail.com for further instructions....");

        double illegalValue = Math.pow(2, 54);      // Value greater than range
        System.out.println("This did run...");

        PhCurrency badValue = new PhCurrency(illegalValue);
        System.out.println("Finished run...");      // Not executed
    }

    @Test
    public void replace_string_test() {
        String test1 = "12312.23kilo";

        String test2 = test1.replaceAll("[^\\d.]", "");
        String test3 = test1.replaceAll("[\\d.]", "");

        System.out.println("Test 1: " + test1);
        System.out.println("Test 2: " + test2);
        System.out.println("Test 3: " + test3);

        assertTrue(true);
    }

}