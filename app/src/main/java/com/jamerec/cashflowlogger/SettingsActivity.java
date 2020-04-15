package com.jamerec.cashflowlogger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadFragment(new SettingsFragment());
    }

    void loadFragment(Fragment settingFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, settingFragment)
                .commit();
    }
}
