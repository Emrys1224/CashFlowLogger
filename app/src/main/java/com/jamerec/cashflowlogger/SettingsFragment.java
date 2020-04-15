package com.jamerec.cashflowlogger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    final static String SETTING_PREF = "com.jamerec.cashflowlogger.settingpref";
    final static String USER_NAME = "user_name";
    final static String PASSWORD = "password";
    final static String LANGUAGE = "language";
    final static String CURRENCY = "currency";
    final static String DAILY = "daily";
    final static String WEEKLY = "weekly";
    final static String MONTHLY = "monthly";

    private Context mContext;
    private CFLoggerOpenHelper mDB;

    // Setting Values
    private String mUserName;
    private String mPassword;
    private String mLanguage;
    private String mCurrency;
    private boolean mDurationDaily;
    private boolean mDurationWeekly;
    private boolean mDurationMonthly;
    private List<FundAllocationPercentage> mAllocation;

    // Flag if allocation was changed
    private boolean mIsAllocationChanged = false;

    // Settings value
    private TextView mUserNameTV;
    private TextView mPasswordTV;
    private RecyclerView mAllocationRV;

    // Edit buttons (clickable text)
    private TextView mUserNameBtn;
    private TextView mPasswordBtn;
    private TextView mAllocationBtn;

    // Input fields
    private EditText mUserNameET;
    private Spinner mLanguageSpr;
    private Spinner mCurrencySpr;
    private CheckBox mDailyCB;
    private CheckBox mWeeklyCB;
    private CheckBox mMonthlyCB;
    private Spinner mDailyHourSpr;
    private Spinner mDailyMinSpr;
    private Spinner mDailyAmPmSpr;
    private Spinner mWeeklyHourSpr;
    private Spinner mWeeklyMinSpr;
    private Spinner mWeeklyAmPmSpr;
    private Spinner mMonthlyHourSpr;
    private Spinner mMonthlyMinSpr;
    private Spinner mMonthlyAmPmSpr;

    // Error displays
    private TextView mErrUserNameTV;

    // Buttons
    private Button mSaveBtn;
    private Button mBackBtn;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mContext = getContext();
        mDB = new CFLoggerOpenHelper(mContext);

        // Get the setting values from SharedPreference and database.
        final SharedPreferences settingPref = mContext.getSharedPreferences(SETTING_PREF, Context.MODE_PRIVATE);
        Resources resources = mContext.getResources();
        mUserName = settingPref.getString(USER_NAME, resources.getString(R.string.dummy_username));
        mPassword = settingPref.getString(PASSWORD, resources.getString(R.string.dummy_password));
        mLanguage = settingPref.getString(LANGUAGE, resources.getString(R.string.dummy_language));
        mCurrency = settingPref.getString(CURRENCY, resources.getString(R.string.dummy_currency));
        mDurationDaily = settingPref.getBoolean(DAILY, false);
        mDurationWeekly = settingPref.getBoolean(WEEKLY, false);
        mDurationMonthly = settingPref.getBoolean(MONTHLY, false);
        mAllocation = mDB.getFundsAllocationPercentage();

        // Initialize setting values widgets
        mUserNameTV = view.findViewById(R.id.setting_username);
        mPasswordTV = view.findViewById(R.id.setting_password);
        mAllocationRV = view.findViewById(R.id.disp_allocation);

        // Initialize 'EDIT' buttons
        mUserNameBtn = view.findViewById(R.id.btn_edit_username);
        mPasswordBtn = view.findViewById(R.id.btn_edit_password);
        mAllocationBtn = view.findViewById(R.id.btn_edit_fund_allocation);

        // Initialize input fields widgets
        mUserNameET = view.findViewById(R.id.input_username);
        mDailyCB = view.findViewById(R.id.checkBox_daily);
        mWeeklyCB = view.findViewById(R.id.checkBox_weekly);
        mMonthlyCB = view.findViewById(R.id.checkBox_monthly);

        // Initialize language setting Spinner
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                mContext,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.language));
        mLanguageSpr = view.findViewById(R.id.selection_language);
        mLanguageSpr.setAdapter(languageAdapter);

        // Initialize currency Spinner
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                mContext,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.currency));
        mCurrencySpr = view.findViewById(R.id.selection_currency);
        mCurrencySpr.setAdapter(currencyAdapter);

        // Initialize error display widgets
        mErrUserNameTV = view.findViewById(R.id.err_username);

        // Initialize Buttons
        mSaveBtn = view.findViewById(R.id.btn_save);
        mBackBtn = view.findViewById(R.id.btn_back);

        // Display the setting values
        mUserNameTV.setText(mUserName);
        mPasswordTV.setText(mPassword);
        mLanguageSpr.setSelection(languageAdapter.getPosition(mLanguage));
        mCurrencySpr.setSelection(currencyAdapter.getPosition(mCurrency));
        mDailyCB.setChecked(mDurationDaily);
        mWeeklyCB.setChecked(mDurationWeekly);
        mMonthlyCB.setChecked(mDurationMonthly);

        // Fund allocation percentage settings
        LinearLayoutManager allocationLayoutMgr = new LinearLayoutManager(mContext);
        mAllocationRV.setLayoutManager(allocationLayoutMgr);
        FundListAdapter adapter = new FundListAdapter(mContext, mAllocation, null);
        mAllocationRV.setAdapter(adapter);

        // Edit user name
        mUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserNameTV.setVisibility(View.GONE);
                mUserNameET.setVisibility(View.VISIBLE);
                mUserNameET.setText(mUserName);
            }
        });

        // Set/update username
        mUserNameET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mUserNameET.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mUserNameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String userName = mUserNameET.getText().toString();

                if (userName.isEmpty()) {
                    mErrUserNameTV.setVisibility(View.VISIBLE);
                    mErrUserNameTV.setText(R.string.err_msg_user_name_empty);
                    return true;
                }

                mUserName = userName;
                mUserNameTV.setText(mUserName);
                mUserNameTV.setVisibility(View.VISIBLE);
                mUserNameET.setVisibility(View.GONE);
                mErrUserNameTV.setVisibility(View.GONE);

                mUserNameET.getWindowToken();

                return false;
            }
        });
        mUserNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mUserNameET.requestFocus();
                    hideSoftKeyboard();
                }
            }
        });

        // Edit password
        mPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .loadFragment(new PasswordSettingFragment());
            }
        });

        // Set/update language setting
        mLanguageSpr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLanguage = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
        });

        // Set/update currency setting
        mCurrencySpr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrency = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
        });

        // Edit fund allocation percentage
        mAllocationBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                ((SettingsActivity) Objects.requireNonNull(getActivity()))
                        .loadFragment(new AllocationSettingFragment());
            }
        });

        // Set/update Cash Flow Report duration setting
        mDailyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDurationDaily = isChecked;
            }
        });
        mWeeklyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDurationWeekly = isChecked;
            }
        });
        mMonthlyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDurationMonthly = isChecked;
            }
        });

        // Save changes in the settings
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor settingsEditor = settingPref.edit();
                settingsEditor.putString(USER_NAME, mUserName);
                settingsEditor.putString(PASSWORD, mPassword);
                settingsEditor.putString(LANGUAGE, mLanguage);
                settingsEditor.putString(CURRENCY, mCurrency);
                settingsEditor.putBoolean(DAILY, mDurationDaily);
                settingsEditor.putBoolean(WEEKLY, mDurationWeekly);
                settingsEditor.putBoolean(MONTHLY,mDurationMonthly);
                // set the CashFlowReportService here

                if (mIsAllocationChanged) mDB.editFundsAllocationPercentage(mAllocation);

                if (settingsEditor.commit()) {
                    Toast.makeText(mContext, R.string.notify_save_done, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, R.string.notify_save_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Cancel change of settings
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Hides the soft keyboard.
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUserNameET.getWindowToken(), 0);
    }
}
