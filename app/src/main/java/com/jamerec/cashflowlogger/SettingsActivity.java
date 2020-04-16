package com.jamerec.cashflowlogger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    final static String SETTING_PREF = "com.jamerec.cashflowlogger.settingpref";
    final static String USER_NAME = "user_name";
    final static String PASSWORD = "password";
    final static String LANGUAGE = "language";
    final static String CURRENCY = "currency";
    final static String DAILY = "daily";
    final static String WEEKLY = "weekly";
    final static String MONTHLY = "monthly";

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
