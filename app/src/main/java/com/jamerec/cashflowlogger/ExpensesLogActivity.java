package com.jamerec.cashflowlogger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class ExpensesLogActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_log_expenses);

    Spinner fundSelection = findViewById(R.id.selection_fund);

    // List of funds retrieved from SharedPreference.
    List<String> funds = new ArrayList<>();
    funds.add("Basic Needs");
    funds.add("Education");
    funds.add("Investment");
    funds.add("Retirement");
    funds.add("Leisure");

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, funds);

    fundSelection.setAdapter(adapter);
  }
}