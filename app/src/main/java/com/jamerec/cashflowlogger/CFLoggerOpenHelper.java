package com.jamerec.cashflowlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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

    private void initilizeDB() {
        // * Add an entry into the `funds` table with the value for `name` as 'Basic Needs'.
        // * Add an entry into the `funds_allocation` table with the following values:
        //      > `fund_id`            = the id of 'Basic Needs'
        //      > `percent_allocation` = '100' percent.
        // * Call logIncome() with the following arguments:
        //      > incomeSource = 'Initial Balance'
        //      > amount = PhCurrency(0)
    }

    void logIncome(String incomeSource, PhCurrency amount, ArrayList<FundItem> fundsList) {
        // * Get the `id` of the 'incomeSource' from `source` table. If not found add an entry
        //   for it.
        // * Add an entry in `income` table with the following values:
        //      > `source_id`  = the id of 'incomeSource'
        //      > `amountX100` = the value from amount.getAmountX100()
        //   and get its id.
        // * Update the record for cash at hand (`balance` table) by calling
        //   updateCashBalance() with the following arguments:
        //      > amountDiff = amount
        //      > incomeId   = ID of the recently added entry in `income` table
        //      > expenseId  = NULL_ID (-111)
        // * Update the `funds_balance` table by calling updateFundBalance() as
        //   per the allocation amount from 'fundsList' ArrayList and the id retrieved from
        //   updateCashBalance().
    }

    void logExpense(ExpenseItem expenseItem) {
        // * Get the id of the product from the `product` table. If none was
        //   found add an entry for it in the table.
        // * Get the ids of the tags associated with the product from the `tags` table.
        //   Add to the `tags` table those that are not yet recorded.
        // * Check if the tags given are matched in the `product_tags` intermediary table and
        //   add it if it is not yet matched.
        // * Get the id of the brand from the `brand` table, add an entry for it if none
        //   was found.
        // * Get the id of the unit from the `unit` table, add an entry for it if none
        //   was found.
        // * Get the id of the packaging size with the corresponding id for unit of measurement
        //   from the `product_size` table, add an entry for it if none was found.
        // * Get the id for the product variant with the corresponding product id, brand id, and
        //   product size id in the 'product_variant' table, add an entry for it if none was found.
        // * Get the id and latest price for this product variant in the 'item' table. If the
        //   price is different add a new entry with it's current price for this
        //   product variant and get its ID.
        // * Add new entry into `expense` table with the given item ID, quantity, and
        //   remarks if there is any, and get the id for the new entry.
        // * Update the record for cash at hand (`balance` table) by calling
        //   updateCashBalance() with the following arguments:
        //      > amountDiff = expenseItem.getTotalPrice() * -1
        //      > incomeId   = NULL_ID (-111)
        //      > expenseId  = ID of the recently added entry in `expense` table
        // * Update the `funds_balance` table by calling updateFundBalance()
        //   with the following arguments:
        //      > 'fundID'          = id of the expenseItem.mFund
        //      > 'balanceUpdateId' = the returned id of updateCashBalance()
        //      > 'amountDiff'      = expenseItem.getTotalPrice() * -1

    }

    long updateCashBalance(PhCurrency amountDiff, int incomeId, int expenseId) {
        long newBalanceId = -1;

        // * Check that only one of 'incomeId' and 'expenseId' has a valid id
        //   while the other should have a 'NULL_ID' as value, otherwise throw a Exception.
        // * Get the latest `amountX100` (cash balance) from `balance` table.
        //   NOTE: If none is retrieved, which would occur during the first entry,
        //         `amountX100` is equals 'amountDiff'.
        // * Calculate the new `amountX100` (cash balance), that is add 'amountDiff' if
        //   is from income and subtract if it is from expense.
        // * Add an entry into `balance` table with the following values:
        //      > `amountX100`        = the calculated latest balance
        //      > `income_update_id`  = 'incomeId'
        //      > `expense_update_id` = 'expenseId'
        //   and get its id and return it.

        return newBalanceId;
    }

    void updateFundBalance(int fundID, int balanceUpdateId, PhCurrency amountDiff) {
        // * Get the latest `amountX100` (cash balance) from `funds_balance` table for the
        //   selected fund as per the 'fundID' given.
        //   NOTE: If none is retrieved, which would occur during the first entry,
        //         `amountX100` is equals 'amountDiff'.
        // * Calculate the new `amountX100` (cash balance), that is add 'amountDiff' if it
        //   is from income and subtract if it is from expense.
        // * Add an entry into `funds_balance` table with the following values:
        //      > `fund_id`           = 'fundID'
        //      > `balance_update_id` = 'balanceUpdateId'
        //      > `amountX100`        = the calculated latest balance
    }

    long insertEntry(String table, ContentValues values) {
        long entryId = -1;

        // * Add entry to 'table' with the given 'values' by calling mWritableDB.insert().
        // * Get and return the id of the new entry if it has `id` column, otherwise -1.

        return entryId;
    }

    long queryId(String table, ContentValues arguments) {
        long entryId = -1;

        // * Build the arguments for querying the id from the 'table' and arguments given.
        Set<Map.Entry<String, Object>> colValPairs = arguments.valueSet();

        String idColumn = "id";
        String[] columns = new String[]{idColumn};
        StringBuilder whereClause = new StringBuilder();
        String[] whereArgs = new String[colValPairs.size()];

        int index = 0;
        int argsLastIndex = colValPairs.size() - 1;
        for (Map.Entry<String, Object> colValPair : colValPairs) {
            String column = colValPair.getKey();
            String value = colValPair.getValue().toString();

            whereClause.append(column).append(" = ?");
            if (index < argsLastIndex) whereClause.append(" AND ");
            whereArgs[index] = value;

            index++;
        }

        // * Execute query to retrieve and return the id, else return -1 when none was found.
        Cursor queryResult = null;

        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            queryResult = mReadableDB.query(
                    table, columns, whereClause.toString(), whereArgs, null, null, idColumn);
            queryResult.moveToFirst();
            entryId = queryResult.getLong(queryResult.getColumnIndex(idColumn));

        } catch (Exception e) {
            Log.d(TAG, "EXCEPTION! " + e);

        } finally {
            if (queryResult != null) queryResult.close();
        }

        return entryId;
    }
}
