package com.jamerec.cashflowlogger;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExpenseItemTest {
    double delta = 0.009D;

    @Test
    public void expense_item_creation_test() throws Measures.ClearedStateException {
        String product = "Rice";
        String brand = "Maharlika";
        double price = 1200D;
        String size = "25kilo";
        double quantity = 3D;
        String fund = "Basic Needs";

        ExpenseItem test = new ExpenseItem();
        test.setItemName(product, null);
        test.setBrand(brand);
        test.setPrice(new PhCurrency(price));
        test.setSize(size);
        test.setQuantity(quantity);
        test.setFund(fund);

        assertEquals(product, test.getItemName());
        assertEquals(brand, test.getBrand());
        assertEquals(price, test.getItemPrice().toDouble(), delta);
        assertEquals(size, test.getSize().toString());
        assertEquals(quantity, test.getQuantity().getDouble(), delta);
        assertEquals(fund, test.getFund());
        assertEquals(price*quantity, test.getTotalPrice().toDouble(), delta);

        System.out.println(test.toString());
    }

    @Test
    public void modify_tags_test() {
        ExpenseItem test = new ExpenseItem();
        test.setItemName("Rice", null);
        System.out.println("Test tags");
        for (String tag : test.getTags()) {
            System.out.println(tag);
        }
        System.out.println();

        test.addTag("fish");
        test.addTag("frozen food");
        System.out.println("Test tags with tags added");
        for (String tag : test.getTags()) {
            System.out.println(tag);
        }
        System.out.println();

        test.removeTag("frozen food");
        System.out.println("Test tags with tags removed");
        for (String tag : test.getTags()) {
            System.out.println(tag);
        }
        System.out.println();
    }

    @Test
    public void modify_tags_test2() {
        String product = "Rice";
        String brand = "Maharlika";
        double price = 1200D;
        String size = "25kilo";
        double quantity = 3D;
        String fund = "Basic Needs";

        ExpenseItem test = new ExpenseItem();
        test.setItemName(product, null);
        test.setBrand(brand);
        test.setPrice(new PhCurrency(price));
        test.setSize(size);
        test.setQuantity(quantity);
        test.setFund(fund);

        System.out.println("Initial Values");
        System.out.println(test.toString());
        System.out.println();

        List<String> tags = test.getTags();
        tags.add("added tag1");
        tags.add("added tag2");

        System.out.println("Tags Added");
        System.out.println(test.toString());
        System.out.println();

        tags.remove("added tag2");

        System.out.println("A Tag Removed");
        System.out.println(test.toString());
        System.out.println();
    }
}
