package com.jamerec.cashflowlogger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PhCurrencyExceptionTest {
    private final static double POSITIVE_LIMIT = PhCurrency.PESO_MAX_VALUE;
    private final static double NEGATIVE_LIMIT = PhCurrency.PESO_MIN_VALUE;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    // Test exception for when the set value is greater than or equal to PESO_MAX_VALUE.
    public void catch_exception_set_value_pos_limit() {
        System.out.println("Exception Test: Set Value Positive Limit");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Set peso value overflow." +
                " Consider donating the excess to 'Charity'." +
                " Contact jamerec1224@gmail.com for further instructions....");

        System.out.printf("Seed value: %f\n\n", POSITIVE_LIMIT);

        PhCurrency test = new PhCurrency(POSITIVE_LIMIT);
        System.out.println("PhCurrency value: " + test);
    }

    @Test
    // Test exception for when the set value is greater than or equal to PESO_MIN_VALUE.
    public void catch_exception_set_value_neg_limit() {
        System.out.println("Exception Test: Set Value Negative Limit");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Set peso value overflow." +
                " Consider donating the excess to 'Charity'." +
                " Contact jamerec1224@gmail.com for further instructions....");

        System.out.printf("Seed value: %f\n\n", NEGATIVE_LIMIT);

        PhCurrency test = new PhCurrency(NEGATIVE_LIMIT);
    }

    @Test
    // Test exception for when the sum of the value added exceeds
    // or equal to PESO_MAX_VALUE ( A + B >= PESO_MAX_VALUE ).
    // Test trigger condition where A > 0 and B > 0.
    public void catch_exception_addition_pos_limit() {
        System.out.println("Exception Test: Addition of Two Positive Values\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Sum peso value overflow. You're too rich for this app! XD");

        double val1 = 12423765.34D;
        double val2 = POSITIVE_LIMIT - val1;

        System.out.printf("Seed value1: %f\n", val1);
        System.out.printf("Seed value2: %f\n", val2);
        System.out.printf("val1 + val2 = %f\n\n", val1 + val2);

        PhCurrency testVal1 = new PhCurrency(val1);
        PhCurrency testVal2 = new PhCurrency(val2);

        System.out.printf("Test value1: %s\n", testVal1);
        System.out.printf("Test value2: %s\n\n", testVal2);

        testVal1.add(testVal2);
    }

    @Test
    // Test exception for when the sum of the value added exceeds
    // or equal to PESO_MIN_VALUE ( A + B <= PESO_MIN_VALUE ).
    // Test trigger condition where A < 0 and B < 0.
    public void catch_exception_addition_neg_limit() {
        System.out.println("Exception Test: Addition of Two Negative Values\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Sum peso value overflow. You're too rich for this app! XD");

        double val1 = -12423765.34D;
        double val2 = NEGATIVE_LIMIT - val1;

        System.out.printf("Seed value1: %f\n", val1);
        System.out.printf("Seed value2: %f\n", val2);
        System.out.printf("val1 + val2 = %f\n\n", val1 + val2);

        PhCurrency testVal1 = new PhCurrency(val1);
        PhCurrency testVal2 = new PhCurrency(val2);

        System.out.printf("Test value1: %s\n", testVal1);
        System.out.printf("Test value2: %s\n\n", testVal2);

        testVal1.add(testVal2);
    }

    @Test
    // Test exception for when the difference of the value subtracted exceeds
    // or equal to PESO_MAX_VALUE ( A - B >= PESO_MAX_VALUE ).
    // Test trigger condition where A > 0 and B < 0.
    public void catch_exception_subtraction_pos_limit() {
        System.out.println("Exception Test: Subtract a Negative Value From a Positive Value\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Difference peso value overflow. You're crazy!");

        double val1 = 12423765.34D;
        double val2 = (POSITIVE_LIMIT - val1) * -1;

        System.out.printf("Seed value1: %f\n", val1);
        System.out.printf("Seed value2: %f\n", val2);
        System.out.printf("val1 - val2 = %f\n\n", val1 - val2);

        PhCurrency testVal1 = new PhCurrency(val1);
        PhCurrency testVal2 = new PhCurrency(val2);

        System.out.printf("Test value1: %s\n", testVal1);
        System.out.printf("Test value2: %s\n\n", testVal2);

        testVal1.subtract(testVal2);
    }

    @Test
    // Test exception for when the difference of the value subtracted exceeds
    // or equal to PESO_MIN_VALUE ( A - B <= PESO_MIN_VALUE ).
    // Test trigger condition where A < 0 and B > 0.
    public void catch_exception_subtraction_neg_limit() {
        System.out.println("Exception Test: Subtract a Positive Value From a Negative Value\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Difference peso value overflow. You're crazy!");

        double val1 = -12423765.34D;
        double val2 = (NEGATIVE_LIMIT - val1) * -1;

        System.out.printf("Seed value1: %f\n", val1);
        System.out.printf("Seed value2: %f\n", val2);
        System.out.printf("val1 - val2 = %f\n\n", val1 - val2);

        PhCurrency testVal1 = new PhCurrency(val1);
        PhCurrency testVal2 = new PhCurrency(val2);

        System.out.printf("Test value1: %s\n", testVal1);
        System.out.printf("Test value2: %s\n\n", testVal2);

        testVal1.subtract(testVal2);
    }

    @Test
    // Test exception for when the product of the value multiplied exceeds
    // or equal to PESO_MAX_VALUE ( A * B >= PESO_MAX_VALUE ).
    // Test trigger condition for A > 0 and B > 0,
    // where A is the peso currency value and B is the factor.
    public void catch_exception_multiplication_pos_limit1() {
        System.out.println("Exception Test: Positive Value Multiplied By a Positive Factor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Product peso value overflow.");

        double val = 12423765.34D;
        double factor = POSITIVE_LIMIT / val;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Factor: %f\n", factor);
        System.out.printf("val * factor = %f\n\n", val * factor);

        testVal.multiplyBy(factor);
    }

    @Test
    // Test exception for when the product of the value multiplied exceeds
    // or equal to PESO_MAX_VALUE ( A * B >= PESO_MAX_VALUE ).
    // Test trigger condition for A < 0 and B < 0,
    // where A is the peso currency value and B is the factor.
    public void catch_exception_multiplication_pos_limit2() {
        System.out.println("Exception Test: Negative Value Multiplied By a Negative Factor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Product peso value overflow.");

        double val = -12423765.34D;
        double factor = POSITIVE_LIMIT / val;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Factor: %f\n", factor);
        System.out.printf("val * factor = %f\n\n", val * factor);

        testVal.multiplyBy(factor);
    }

    @Test
    // Test exception for when the product of the value multiplied is less
    // or equal to PESO_MIN_VALUE ( A * B <= PESO_MIN_VALUE ).
    // Test trigger condition for A > 0 and B < 0,
    // where A is the peso currency value and B is the factor.
    public void catch_exception_multiplication_neg_limit1() {
        System.out.println("Exception Test: Positive Value Multiplied By a Negative Factor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Product peso value overflow.");

        double val = 12423765.34D;
        double factor = NEGATIVE_LIMIT / val;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Factor: %f\n", factor);
        System.out.printf("val * factor = %f\n\n", val * factor);

        testVal.multiplyBy(factor);
    }

    @Test
    // Test exception for when the product of the value multiplied is less
    // or equal to PESO_MAX_VALUE ( A * B >= PESO_MAX_VALUE ).
    // Test trigger condition for A < 0 and B > 0,
    // where A is the peso currency value and B is the factor.
    public void catch_exception_multiplication_neg_limit2() {
        System.out.println("Exception Test: Negative Value Multiplied By a Positive Factor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Product peso value overflow.");

        double val = -12423765.34D;
        double factor =  NEGATIVE_LIMIT / val;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Factor: %f\n", factor);
        System.out.printf("val * factor = %f\n\n", val * factor);

        testVal.multiplyBy(factor);
    }

    @Test
    // Test exception for when the quotient of the value divided exceeds
    // or equal to PESO_MAX_VALUE ( A / B >= PESO_MAX_VALUE ).
    // Test trigger condition for A > 0 and B > 0,
    // where A is the peso currency value and B is the divisor.
    public void catch_exception_division_pos_limit1() {
        System.out.println("Exception Test: Positive Value Divided By a Positive Divisor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Quotient peso value overflow.");

        double val = 12423765.34D;
        double divisor = val / POSITIVE_LIMIT;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Divisor: %.20f\n", divisor);
        System.out.printf("val * factor = %f\n\n", val / divisor);

        testVal.divideBy(divisor);
    }

    @Test
    // Test exception for when the quotient of the value divided exceeds
    // or equal to PESO_MAX_VALUE ( A / B >= PESO_MAX_VALUE ).
    // Test trigger condition for A < 0 and B < 0,
    // where A is the peso currency value and B is the divisor.
    public void catch_exception_division_pos_limit2() {
        System.out.println("Exception Test: Negative Value Divided By a Negative Divisor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Quotient peso value overflow.");

        double val = -12423765.34D;
        double divisor = val / POSITIVE_LIMIT;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Divisor: %.20f\n", divisor);
        System.out.printf("val * factor = %f\n\n", val / divisor);

        testVal.divideBy(divisor);
    }

    @Test
    // Test exception for when the quotient of the value divided is less than
    // or equal to PESO_MIN_VALUE ( A / B <= PESO_MIN_VALUE ).
    // Test trigger condition for A > 0 and B < 0,
    // where A is the peso currency value and B is the divisor.
    public void catch_exception_division_neg_limit1() {
        System.out.println("Exception Test: Positive Value Divided By a Negative Divisor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Quotient peso value overflow.");

        double val = 12423765.34D;
        double divisor = val / NEGATIVE_LIMIT;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Divisor: %.20f\n", divisor);
        System.out.printf("val * factor = %f\n\n", val / divisor);

        testVal.divideBy(divisor);
    }

    @Test
    // Test exception for when the quotient of the value divided is less than
    // or equal to PESO_MIN_VALUE ( A / B <= PESO_MIN_VALUE ).
    // Test trigger condition for A < 0 and B > 0,
    // where A is the peso currency value and B is the divisor.
    public void catch_exception_division_neg_limit2() {
        System.out.println("Exception Test: Negative Value Divided By a Positive Divisor\n");

        thrown.expect(ArithmeticException.class);
        thrown.expectMessage("Quotient peso value overflow.");

        double val = -12423765.34D;
        double divisor = val / NEGATIVE_LIMIT;

        PhCurrency testVal = new PhCurrency(val);

        System.out.printf("Seed value: %f\n", val);
        System.out.printf("Test value: %s\n", testVal);
        System.out.printf("Divisor: %.20f\n", divisor);
        System.out.printf("val * factor = %f\n\n", val / divisor);

        testVal.divideBy(divisor);
    }

}
