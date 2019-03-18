package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class IncomeLogActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager  = getSupportFragmentManager();

        // Get the account balance, list of income sources and the list of funds from DB here....
        // Account balance dummy data
        loadFragment(new IncomeDetailFragment());
    }

    public void loadFragment(Fragment detailFragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(android.R.id.content, detailFragment);
        fragmentTransaction.commit();
    }

    public void cancelLogging(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void allocateFund(View view) {
        loadFragment(new FundAllocationFragment());
    }

    public void showSummary(View view) {
        loadFragment(new IncomeDetailsConfirmationFragment());
    }
}
