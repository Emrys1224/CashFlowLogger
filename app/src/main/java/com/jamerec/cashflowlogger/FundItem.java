package com.jamerec.cashflowlogger;

import java.text.DecimalFormat;

/**
 * This is a model for fund category used in
 * income allocation.
 */
public class FundItem {
    private String name;
    private Double amount;

    /**
     * Constructor...
     * @param name   of the fund
     * @param amount in the fund
     */
    public FundItem(String name, Double amount) {
        this.name = name;
        this.amount = amount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getName() {
        return this.name;
    }

    public Double getAmount() {
        return this.amount;
    }

    public String getAmountString() {
        String pesoPattern = "₱ ###,###,###.##";
        DecimalFormat pesoFormat = new DecimalFormat(pesoPattern);

        return pesoFormat.format(amount);
    }

    @Override
    public String toString() {
        return "Fund name: " + name + "\nFund amount: " + getAmountString();
    }
}
