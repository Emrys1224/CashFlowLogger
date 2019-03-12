package com.jamerec.cashflowlogger;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

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
}