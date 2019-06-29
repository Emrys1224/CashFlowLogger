package com.jamerec.cashflowlogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CFLoggerOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;    // has to be 1 first time or app will crash
    private static final String DATABASE_NAME = "cash_flow_logger";

    CFLoggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
