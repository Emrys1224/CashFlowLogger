package com.jamerec.cashflowlogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CFLoggerOpenHelper mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDB = new CFLoggerOpenHelper(this);

        // Update balance display here
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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