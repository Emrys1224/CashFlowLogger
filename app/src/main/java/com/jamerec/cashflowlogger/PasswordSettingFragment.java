package com.jamerec.cashflowlogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordSettingFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    // Password Regex
    // Password must be at least 8 characters containing a letter,
    // a number, and a special character(!@#$%^&*)
    private final static String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";

    // Validation
    private final static boolean FAILED = false;
    private final static boolean PASSED = true;

    // Password and flags
    private String mNewPassword = "";
    private String mCurrentPassword = "";
    private boolean mHasNewValidPassword = false;
    private boolean mMatchedNewPassword = false;
    private boolean mMatchedOldPassword = false;

    // Input fields
    private EditText mNewPasswordET;
    private EditText mConfirmPasswordET;
    private EditText mOldPasswordET;

    // Error display
    private TextView mErrNewPasswordTV;
    private TextView mErrConfirmPasswordTV;
    private TextView mErrOldPasswordTV;

    // Button widgets
    private Button mSaveBtn;
    private Button mCancelBtn;

    private SharedPreferences.Editor mPasswordEditor;

    public PasswordSettingFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_setting, container, false);

        SharedPreferences settingsPref =
                Objects.requireNonNull(getContext())
                        .getSharedPreferences(SettingsActivity.SETTING_PREF, Context.MODE_PRIVATE);
        mCurrentPassword = settingsPref.getString(SettingsActivity.PASSWORD, "");
        Log.d(TAG, "Current Password: " + mCurrentPassword);

        mPasswordEditor = settingsPref.edit();

        // Initialize input widgets
        mNewPasswordET = view.findViewById(R.id.input_new_password);
        mConfirmPasswordET = view.findViewById(R.id.input_confirm_password);
        mOldPasswordET = view.findViewById(R.id.input_old_password);

        // Initialize error display widgets
        mErrNewPasswordTV = view.findViewById(R.id.err_msg_new_password);
        mErrConfirmPasswordTV = view.findViewById(R.id.err_msg_confirm_password);
        mErrOldPasswordTV = view.findViewById(R.id.err_msg_old_password);

        // Initialize buttons
        mSaveBtn = view.findViewById(R.id.btn_save);
        mCancelBtn = view.findViewById(R.id.btn_cancel);

        // Set new password
        mNewPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mNewPassword = mNewPasswordET.getText().toString();
                return validatePassword() == FAILED;    // Keep focus if not valid
            }
        });
        mNewPasswordET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String newPassword = mNewPasswordET.getText().toString();
                    if (!newPassword.equals(mNewPassword)) {
                        mNewPassword = newPassword;
                        validatePassword();
                    }
                }
            }
        });

        // Confirm the new password
        mConfirmPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return confirmNewPassword() == FAILED;
            }
        });
        mConfirmPasswordET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) confirmNewPassword();
            }
        });

        // Confirm the current password
        mOldPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                confirmPassword();
                return  false;
            }
        });
        mOldPasswordET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) confirmPassword();
            }
        });

        // Set buttons ClickListener
        mSaveBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

        return view;
    }

    /**
     * Check if the new password have at least 6 characters which consist of
     * letter, number, and special character(!@#$%^&*)
     *
     * @return FAILED if it does not matched the pattern; PASSED if it matched the pattern
     */
    private boolean validatePassword() {
        boolean isValid;

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(mNewPassword);

        isValid = matcher.matches();

        if (isValid) {
            mErrNewPasswordTV.setVisibility(View.GONE);
            mHasNewValidPassword = true;

        } else {
            mErrNewPasswordTV.setVisibility(View.VISIBLE);
            mHasNewValidPassword = false;
        }

        readyToSave();

        return isValid;
    }

    /**
     * Confirm that the user has inputted the new password that the user intend to put. Also
     * updates the flag for confirmation of new password.
     *
     * @return FAILED if it does not matched the new password; PASSED if it matched the new password
     */
    private boolean confirmNewPassword() {
        if (mNewPassword.equals(mConfirmPasswordET.getText().toString())) {
            mErrConfirmPasswordTV.setVisibility(View.GONE);
            mMatchedNewPassword = true;
        } else {
            mErrConfirmPasswordTV.setVisibility(View.VISIBLE);
            mMatchedNewPassword = false;
        }

        readyToSave();

        return mMatchedNewPassword;
    }

    /**
     * Confirm that the user has entered the right/current password before changing it
     *
     * @return FAILED if it does not matched the current password;
     * PASSED if it matched the current password
     */
    private boolean confirmPassword() {
        if (mCurrentPassword.equals(mOldPasswordET.getText().toString())) {
            mErrOldPasswordTV.setVisibility(View.GONE);
            mMatchedOldPassword = true;
        } else {
            mErrOldPasswordTV.setVisibility(View.VISIBLE);
            mMatchedOldPassword = false;
        }

        readyToSave();

        return mMatchedOldPassword;
    }

    /**
     * Check if a new valid password has been entered, if the confirmation matched, and
     * if the current password has been entered. The 'SAVE' button is
     * enabled once the conditions are met.
     */
    private void readyToSave() {
        if (mHasNewValidPassword && mMatchedNewPassword && mMatchedOldPassword) {
            mSaveBtn.setEnabled(true);

        } else {
            mSaveBtn.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            mPasswordEditor.putString(SettingsActivity.PASSWORD, mNewPassword);

            if (mPasswordEditor.commit())
                Toast.makeText(
                        getContext(),
                        R.string.notify_password_changed,
                        Toast.LENGTH_SHORT)
                        .show();
        }

        ((SettingsActivity) Objects.requireNonNull(getActivity()))
                .loadFragment(new SettingsFragment());
    }
}
