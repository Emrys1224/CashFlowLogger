package com.jamerec.cashflowlogger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.jamerec.cashflowlogger.CFLoggerContract.*;

public class CFLoggerOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CFLoggerOpenHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;    // has to be 1 first time or app will crash

    // Build the SQL query that creates the schema.
    private static final String[][] CFL_TABLES = {
            {FractionTextEntry.TABLE_NAME, FractionTextEntry.CREATE_TABLE},
            {BalanceEntry.TABLE_NAME, BalanceEntry.CREATE_TABLE},
            {FundsEntry.TABLE_NAME, FundsEntry.CREATE_TABLE},
            {FundsAllocationEntry.TABLE_NAME, FundsAllocationEntry.CREATE_TABLE},
            {FundsBalanceEntry.TABLE_NAME, FundsBalanceEntry.CREATE_TABLE},
            {SourceEntry.TABLE_NAME, SourceEntry.CREATE_TABLE},
            {IncomeEntry.TABLE_NAME, IncomeEntry.CREATE_TABLE},
            {ProductEntry.TABLE_NAME, ProductEntry.CREATE_TABLE},
            {TagEntry.TABLE_NAME, TagEntry.CREATE_TABLE},
            {ProductTagsEntry.TABLE_NAME, ProductTagsEntry.CREATE_TABLE},
            {BrandEntry.TABLE_NAME, BrandEntry.CREATE_TABLE},
            {UnitEntry.TABLE_NAME, UnitEntry.CREATE_TABLE},
            {ProductSizeEntry.TABLE_NAME, ProductSizeEntry.CREATE_TABLE},
            {ProductVariantEntry.TABLE_NAME, ProductVariantEntry.CREATE_TABLE},
            {ItemEntry.TABLE_NAME, ItemEntry.CREATE_TABLE},
            {ExpenseEntry.TABLE_NAME, ExpenseEntry.CREATE_TABLE}
    };

    // Databases
    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    CFLoggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Construct CFLoggerOpenHelper");

//        String query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";
//        Cursor cursor = null;
//
//        try {
//            if (mReadableDB == null) {
//                mReadableDB = getReadableDatabase();
//            }
//            cursor = mReadableDB.rawQuery(query, null);
//
//            if (cursor.moveToFirst()){
//                StringBuilder data = new StringBuilder("List of CFL Tables:\n");
//
//                do{
//                    data.append(cursor.getString(cursor.getColumnIndex("name")));
//                    data.append("\n");
//                }while(cursor.moveToNext());
//
//                Log.d(TAG, data.toString());
//            }
//
//        } catch (Exception e) {
//            Log.d(TAG, "EXCEPTION! " + e);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating CFLogger DB Schema");
        for (String[] createTable : CFL_TABLES) {
            db.execSQL(createTable[1]);
        }
        Log.d(TAG, "Created CFLogger DB Schema");

        Log.d(TAG, "Created CFLogger DB Schema");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Deleting CFL tables....");
        for (String[] createTable : CFL_TABLES) {
            db.execSQL("DROP TABLE IF EXISTS " + createTable[0]);
        }
        Log.d(TAG, "CFL tables deleted!!!");
        onCreate(db);
    }
}
