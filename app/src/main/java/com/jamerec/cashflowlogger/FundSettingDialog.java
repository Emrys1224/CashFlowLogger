package com.jamerec.cashflowlogger;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class FundSettingDialog
        extends AppCompatDialogFragment
        implements View.OnClickListener {

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder fundDialogBuilder =
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater fundDialogInflater = getActivity().getLayoutInflater();
        View view = fundDialogInflater.inflate(R.layout.dialog_fund_setting, null);

        fundDialogBuilder.setView(view);

        Button cancelBtn = view.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);

        return fundDialogBuilder.create();
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
    }

    public interface FundSetListener {
        void setFundAllocation();
    }
}
