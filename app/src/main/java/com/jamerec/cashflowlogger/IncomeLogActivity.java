package com.jamerec.cashflowlogger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.AbstractMap;
import java.util.ArrayList;

public class IncomeLogActivity extends AppCompatActivity
        implements
        IncomeDetailFragment.OnSubmitIncomeDetailListener,
        FundAllocationFragment.OnSubmitFundAllocationListener {

    private final String TAG = getClass().getSimpleName();

    private FragmentManager fragmentManager;

    // Income details
    private String mIncomeSource;
    private PhCurrency mIncomeAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        // Get the account balance, list of income sources and the list of funds from DB here....
        // Account balance dummy data
        loadFragment(new IncomeDetailFragment());
    }

    public void loadFragment(Fragment detailFragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(android.R.id.content, detailFragment);
        fragmentTransaction.commit();
    }

    public void allocateFund(View view) {
        loadFragment(new FundAllocationFragment());
    }

    public void showSummary(View view) {
        loadFragment(new IncomeDetailsConfirmationFragment());
    }

    @Override
    public void submitIncomeDetails(String incomeSource, PhCurrency incomeAmount, int btnID) {
        this.mIncomeSource = incomeSource;
        this.mIncomeAmount = incomeAmount;

        Log.d(TAG, "Income source: " + incomeSource
                + "\t\tIncome amount: " + incomeAmount.toString());

        switch (btnID) {
            case R.id.btn_allocate_auto:
                loadFragment(new IncomeDetailsConfirmationFragment());
                break;

            case R.id.btn_allocate_man:
                Bundle incomeDetail = new Bundle();
                incomeDetail.putParcelable("incomeAmount", mIncomeAmount);
                FundAllocationFragment fundAllocationFragment = new FundAllocationFragment();
                fundAllocationFragment.setArguments(incomeDetail);
                loadFragment(fundAllocationFragment);
                break;

            default:
                loadFragment(new IncomeDetailFragment());
        }
    }

    @Override
    public void submitFundAllocation(ArrayList<AbstractMap.SimpleEntry<String, PhCurrency>> fundList) {

    }
}
