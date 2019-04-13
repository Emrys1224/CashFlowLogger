package com.jamerec.cashflowlogger;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class ExpensesLogActivity extends AppCompatActivity
implements ExpenseLogDetailsFragment.OnSubmitExpenseDetailsListener {

    private final String TAG = getClass().getSimpleName();

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();

        loadFragment(new ExpenseLogDetailsFragment());
    }

    public void loadFragment(Fragment detailFragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(android.R.id.content, detailFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void submitExpenseDetails(Uri uri) {
        // submit details....
    }
}