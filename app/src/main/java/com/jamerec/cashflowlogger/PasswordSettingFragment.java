package com.jamerec.cashflowlogger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordSettingFragment extends Fragment {

    // Password Regex
    // Password must be at least 8 characters containing a letter,
    // a number, and a special character(!@#$%^&*)
    private final static String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";

    // Validation
    private final static boolean FAILED = false;
    private final static boolean PASSED = true;

    private String mNewPassword = "";
    private boolean mMatchedNewPassword = false;
    private boolean nMatchedOldPassword = false;

    // Input fields
    private EditText mNewPasswordET;
    private EditText mConfirmPasswordET;
    private EditText mOldPasswordET;

    // Error display
    private TextView mErrNewPasswordTV;
    private TextView mErrConfirmPasswordTV;
    private TextView mErrOldPasswordTV;

    public PasswordSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_setting, container, false);

        // Initialize input widgets
        mNewPasswordET = view.findViewById(R.id.input_new_password);
        mConfirmPasswordET = view.findViewById(R.id.input_confirm_password);
        mOldPasswordET = view.findViewById(R.id.input_old_password);

        // Initialize error display widgets
        mErrNewPasswordTV = view.findViewById(R.id.err_msg_new_password);
        mErrConfirmPasswordTV = view.findViewById(R.id.err_msg_confirm_password);
        mErrOldPasswordTV = view.findViewById(R.id.err_msg_old_password);

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
                    if (!newPassword.equals(mNewPassword)) validatePassword();
                }
            }
        });

        return view;
    }

    public boolean validatePassword() {
        boolean isValid = FAILED;

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(mNewPassword);

        isValid = matcher.matches();

        if (isValid) mErrNewPasswordTV.setVisibility(View.GONE);
        else mErrNewPasswordTV.setVisibility(View.VISIBLE);

        return isValid;
    }

}
