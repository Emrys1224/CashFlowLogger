package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class IncomeLogActivity extends AppCompatActivity
        implements
        IncomeDetailFragment.OnSubmitIncomeDetailListener,
        FundAllocationFragment.OnSubmitFundAllocationListener,
        IncomeDetailsConfirmationFragment.OnConfirmIncomeLogListener {

    private final String TAG = getClass().getSimpleName();

    private FragmentManager fragmentManager;

    // Income details
    private String mIncomeSource;
    private PhCurrency mIncomeAmount;
    private ArrayList<FundItem> mFundsList;

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

    @Override
    public void submitIncomeDetails(String incomeSource, PhCurrency incomeAmount, int btnID) {
        this.mIncomeSource = incomeSource;
        this.mIncomeAmount = incomeAmount;

        Log.d(TAG, "Income source: " + incomeSource
                + "\t\tIncome amount: " + incomeAmount.toString());

        switch (btnID) {
            case R.id.btn_allocate_auto:
                allocateFundsAutomatically();
                confirmIncomeLogging();
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
    public void submitFundAllocation(ArrayList<FundItem> fundList) {
        this.mFundsList = fundList;

        confirmIncomeLogging();
    }

    /**
     * Create fund allocation based on the settings in SharedPreference.
     */
    private void allocateFundsAutomatically() {
        mFundsList = new ArrayList<>();

        // Fund names and percent allocation
        // To be retrieved from SharedPreference and defined in the settings menu
        String[] fundNames = {
                "Basic Necessity",
                "Education",
                "Investment",
                "Health",
                "Retirement",
                "Leisure"
        };
        double[] percentAllocation = {
                0.55,
                0.1,
                0.15,
                0.05,
                0.05,
                0.1,
        };

        int index = 0;
        for (String fundName : fundNames) {
            PhCurrency fundAmount = new PhCurrency(mIncomeAmount);
            fundAmount.multiplyBy(percentAllocation[index]);
            mFundsList.add(new FundItem(fundName, fundAmount));
            index++;
        }
    }

    /**
     * Launch the IncomeDetailsConfirmationFragment where the details of
     * the new data entry is displayed. You'll be given the option to
     * edit the details or proceed on to logging to the records.
     */
    private void confirmIncomeLogging() {
        Bundle incomeLogDetails = new Bundle();
        incomeLogDetails.putString("incomeSource", mIncomeSource);
        incomeLogDetails.putParcelable("incomeAmount", mIncomeAmount);
        incomeLogDetails.putParcelableArrayList("fundAllocation", mFundsList);

        IncomeDetailsConfirmationFragment confirmationFragment
                = new IncomeDetailsConfirmationFragment();
        confirmationFragment.setArguments(incomeLogDetails);

        loadFragment(confirmationFragment);
    }

    @Override
    public void confirmIncomeLog(int buttonID) {
        switch (buttonID) {
            case R.id.btn_log:
                // Add the income data entry to the DB here....

                Toast.makeText(this,
                        "Successfully allocated and recorded the income.",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_edit:
                loadFragment(new IncomeDetailFragment());
        }
    }
}
