package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CFLoggerOpenHelper mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDB = new CFLoggerOpenHelper(this);

        // Update balance display here
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