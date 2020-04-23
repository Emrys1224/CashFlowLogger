package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class IncomeLogActivity extends AppCompatActivity
        implements
        IncomeDetailFragment.OnSubmitIncomeDetailListener,
        FundAllocationFragment.OnSubmitFundAllocationListener,
        IncomeDetailsConfirmationFragment.OnConfirmIncomeLogListener {

    private final String TAG = getClass().getSimpleName();

    // Income details
    private String mIncomeSource;
    private PhCurrency mIncomeAmount;
    private ArrayList<FundAllocationAmount> mFundsList;

    private  CFLoggerOpenHelper mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDB = new CFLoggerOpenHelper(this);
        loadFragment(new IncomeDetailFragment());
    }

    public void loadFragment(Fragment detailFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, detailFragment)
                .commit();
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
                // Get the list of funds for manual allocation
                List<String> fundsList = mDB.getFundsList();
                ArrayList<FundAllocationAmount> fundsAllocation = new ArrayList<>();
                for (String fundName : fundsList) {
                    fundsAllocation.add(new FundAllocationAmount(fundName, new PhCurrency()));
                }

                FundAllocationFragment fundAllocationFragment = new FundAllocationFragment();
                fundAllocationFragment.setDetails(
                        getResources().getString(R.string.heading_income_detail),
                        mIncomeSource, mIncomeAmount, fundsAllocation);
                loadFragment(fundAllocationFragment);
                break;

            default:
                loadFragment(new IncomeDetailFragment());
        }
    }

    @Override
    public void submitFundAllocation(ArrayList<FundAllocationAmount> fundList) {
        this.mFundsList = fundList;

        confirmIncomeLogging();
    }

    /**
     * Create fund allocation based on the settings in database.
     */
    private void allocateFundsAutomatically() {
        mFundsList = new ArrayList<>();

        List<FundAllocationPercentage> fundsAllocationPercentage = mDB.getFundsAllocationPercentage();
        for (FundAllocationPercentage fundAllocation : fundsAllocationPercentage) {
            String fundName = fundAllocation.getFundName();
            int percentAllocation = fundAllocation.getPercentAllocation();

            if (percentAllocation > 0) {
                double allocationDecimal = percentAllocation / 100D;

                PhCurrency fundAmount = new PhCurrency(mIncomeAmount);
                fundAmount.multiplyBy(allocationDecimal);

                mFundsList.add(new FundAllocationAmount(fundName, fundAmount));
            }
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
            case R.id.btn_allocate:
                // Add the income data entry to the DB
                mDB.logIncome(mIncomeSource, mIncomeAmount, mFundsList);

                Toast.makeText(this,
                        "Successfully allocated and recorded the income.",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_edit:
                Bundle incomeDetails = new Bundle();
                incomeDetails.putString("incomeSource", mIncomeSource);
                incomeDetails.putParcelable("incomeAmount", mIncomeAmount);

                IncomeDetailFragment incomeDetailFragment = new IncomeDetailFragment();
                incomeDetailFragment.setArguments(incomeDetails);

                loadFragment(incomeDetailFragment);
        }
    }
}
