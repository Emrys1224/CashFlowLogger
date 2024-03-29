package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private CFLoggerOpenHelper mDB;

    private TextView mBalanceDisp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity.onCreate() started....");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDB = new CFLoggerOpenHelper(this);

        // Update balance display
        PhCurrency balance = mDB.getCurrentBalance();
        mBalanceDisp = findViewById(R.id.txt_balance);
        mBalanceDisp.setText(balance.toString());

        Log.d(TAG, "MainActivity.onCreate() done....");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void logIncome(View view) {
        Intent intent = new Intent(this, IncomeLogActivity.class);
        startActivity(intent);
    }

    public void logExpenses(View view) {
        Intent intent = new Intent(this, ExpensesLogActivity.class);
        startActivity(intent);
    }

    public void viewRecord(View view) {
        Intent intent = new Intent(this, RecordViewActivity.class);
        startActivity(intent);
    }
}