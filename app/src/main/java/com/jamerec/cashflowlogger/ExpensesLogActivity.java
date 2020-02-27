package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ExpensesLogActivity extends AppCompatActivity
        implements ExpenseLogDetailsFragment.OnSubmitExpenseDetailsListener,
        ExpenseLogConfirmationFragment.OnConfirmExpenseLogListener {

    private final String TAG = getClass().getSimpleName();

    private ExpenseItem mExpenseItem;

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
    public void submitExpenseDetails(ExpenseItem expenseItem) {
        mExpenseItem = expenseItem;

        // submit details....
        Log.d(TAG, expenseItem.toString());

        Bundle expenseDetails = new Bundle();
        expenseDetails.putParcelable("expenseItem", expenseItem);

        ExpenseLogConfirmationFragment expenseLogConfirmationFragment =
                new ExpenseLogConfirmationFragment();
        expenseLogConfirmationFragment.setArguments(expenseDetails);

        loadFragment(expenseLogConfirmationFragment);
    }

    @Override
    public void confirmExpenseLog(int btnID) {
        switch (btnID) {
            case R.id.btn_log:
                // Add the expense data entry to the DB here....
                CFLoggerOpenHelper db = new CFLoggerOpenHelper(this);
                try {
                    db.logExpense(mExpenseItem);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }

                Toast.makeText(this,
                        "Successfully allocated and recorded the expense item.",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_edit:
                Bundle expenseDetails = new Bundle();
                expenseDetails.putParcelable("expenseItem", mExpenseItem);

                ExpenseLogDetailsFragment expenseLogDetailsFragment =
                        new ExpenseLogDetailsFragment();
                expenseLogDetailsFragment.setArguments(expenseDetails);

                loadFragment(expenseLogDetailsFragment);
        }
    }
}