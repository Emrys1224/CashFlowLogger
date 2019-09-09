package com.jamerec.cashflowlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.jamerec.cashflowlogger.CFLoggerContract.*;

public class CFLoggerOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = CFLoggerOpenHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;    // has to be 1 first time or app will crash

    private static final boolean ENTRY_ADDED = true;
    private static final boolean ENTRY_FAILED = false;
    private static final long NO_ID_COL = -123;
    private static final long ID_NOT_FOUND = -456;
    private static final String ID_COL = "id";
    private static final String NAME_COL = "name";

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

        logTablesTest();

        String table = "source";
        String name = "Restaurant";
        queryIdTest(table, name);
        insertIdTest(table, name);
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

    long updateCashBalance(PhCurrency amountDiff, int incomeId, int expenseId) throws IllegalArgumentException {
        // * Check that only one of 'incomeId' and 'expenseId' has a valid id
        //   while the other should have a 'NULL_ID' as value, otherwise throw a IllegalArgumentException.
        if ((incomeId >= 0 && expenseId >= 0) || (incomeId < 0 && expenseId < 0))
            throw new IllegalArgumentException(
                    "Either 'incomeId' or 'incomeId' should have a valid ID but not both....");

        // * Get the latest `amountX100` (cash balance) from `balance` table.
        String balanceTable = BalanceEntry.TABLE_NAME;
        String idCol = BalanceEntry.COL_ID;
        String amountX100Col = BalanceEntry.COL_AMOUNTx100;
        String incomeUpdateIdCol = BalanceEntry.COL_INCOME_UPDATE_ID;
        String expenseUpdateIdCol = BalanceEntry.COL_EXPENSE_UPDATE_ID;

        PhCurrency latestBalance = new PhCurrency();
        String orderBy = "`" + idCol + "` DESC";
        String limit = "1";

        if (mReadableDB == null) mReadableDB = getReadableDatabase();
        Cursor queryBalance = mReadableDB.query(
                balanceTable, new String[]{amountX100Col}, null,
                null, null, null, orderBy, limit);

        if (queryBalance.getCount() > 0) {
            queryBalance.moveToFirst();
            latestBalance.setValue(
                    queryBalance.getLong(
                            queryBalance.getColumnIndex(amountX100Col)));
        }
        queryBalance.close();

        // * Calculate the new `amountX100` (cash balance), that is add 'amountDiff' if
        //   is from income and subtract if it is from expense.
        //   Note: For update done through income update 'amountDiff' should be a positive value
        //         while an expense update should have a negative value. This logic is handled by
        //         logIncome() and logExpense().
        latestBalance.add(amountDiff);

        // * Add an entry into `balance` table with the following values:
        //      > `amountX100`        = the calculated latest balance
        //      > `income_update_id`  = 'incomeId'
        //      > `expense_update_id` = 'expenseId'
        //   and get its id and return it.
        String incomeIdString = incomeId < 0 ? "NULL" : String.valueOf(incomeId);
        String expenseIdString = expenseId < 0 ? "NULL" : String.valueOf(expenseId);
        ContentValues newBalanceEntry = new ContentValues();
        newBalanceEntry.put(amountX100Col, latestBalance.getAmountX100());
        newBalanceEntry.put(incomeUpdateIdCol, incomeIdString);
        newBalanceEntry.put(expenseUpdateIdCol, expenseIdString);
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            if (mWritableDB.insertOrThrow(balanceTable, null, newBalanceEntry) < 0)
                return -1;

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(balanceTable, newBalanceEntry);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return queryId(balanceTable, newBalanceEntry);
    }

    private boolean updateFundBalance(int fundID, int balanceUpdateId, PhCurrency amountDiff) {
        String fundsBalanceTable = FundsBalanceEntry.TABLE_NAME;
        String fundIdCol = FundsBalanceEntry.COL_FUND_ID;
        String balanceUpdateIdCol = FundsBalanceEntry.COL_BALANCE_UPDATE_ID;
        String amountX100Col = FundsBalanceEntry.COL_AMOUNTx100;

        // * Get the latest `amountX100` (cash balance) from `funds_balance` table for the
        //   selected fund as per the 'fundID' given.
        PhCurrency latestBalance = new PhCurrency();
        String whereClause = "`" + fundIdCol + "` = '" + fundID + "'";
        String orderBy = "`" + balanceUpdateIdCol + "` DESC";
        String limit = "1";

        if (mReadableDB == null) mReadableDB = getReadableDatabase();
        Cursor queryBalance = mReadableDB.query(
                fundsBalanceTable, new String[]{amountX100Col}, whereClause,
                null, null, null, orderBy, limit);

        if (queryBalance.getCount() > 0) {
            queryBalance.moveToFirst();
            latestBalance.setValue(
                    queryBalance.getLong(
                            queryBalance.getColumnIndex(amountX100Col)));
        }
        queryBalance.close();

        // * Calculate the new `amountX100`(cash balance), that is add 'amountDiff' to last balance.
        //   Note: For update done through income update 'amountDiff' should be a positive value
        //         while an expense update should have a negative value. This logic is handled by
        //         logIncome() and logExpense().
        latestBalance.add(amountDiff);

        // * Add an entry into `funds_balance` table with the following values:
        //      > `fund_id`           = 'fundID'
        //      > `balance_update_id` = 'balanceUpdateId'
        //      > `amountX100`        = the calculated latest balance
        ContentValues newBalanceEntry = new ContentValues();
        newBalanceEntry.put(fundIdCol, fundID);
        newBalanceEntry.put(balanceUpdateIdCol, balanceUpdateId);
        newBalanceEntry.put(amountX100Col, latestBalance.getAmountX100());
        boolean entryAdded = ENTRY_FAILED;
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            if (mWritableDB.insertOrThrow(fundsBalanceTable, null, newBalanceEntry) > 0)
                entryAdded = ENTRY_ADDED;

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(fundsBalanceTable, newBalanceEntry);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return entryAdded;
    }

    long insertEntry(String table, String name) throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        return insertEntry(table, values);
    }

    long insertEntry(String table, ContentValues values) throws SQLiteConstraintException {
        // * Add entry to 'table' with the given 'values' by calling mWritableDB.insert().
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }
            mWritableDB.insertOrThrow(table, null, values);

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(table, values);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return queryId(table, values);
    }

    long queryId(String table, String name) {
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);

        return queryId(table, values);
    }

    long queryId(String table, @NonNull ContentValues arguments) {
        long id;

        Set<Map.Entry<String, Object>> colValPairs = arguments.valueSet();

        String[] idColumn = new String[]{ID_COL};
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

        if (mReadableDB == null) mReadableDB = getReadableDatabase();

        Cursor queryId = mReadableDB.query(
                table, idColumn, whereClause.toString(), whereArgs, null, null, null);

        if (queryId.getCount() <= 0) id = ID_NOT_FOUND;

        else {
            int idColIndex = queryId.getColumnIndex(ID_COL);
            if (idColIndex < 0) id = NO_ID_COL;
            else {
                queryId.moveToFirst();
                id = queryId.getLong(idColIndex);
            }
        }

        queryId.close();

        return id;
    }

    StringBuilder buildSelectQuery(@NonNull String[] columnsSearch, String table, @NonNull ContentValues arguments) {
        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT `");

        for (int i = 0; i < columnsSearch.length; i++) {
            selectQuery.append(columnsSearch[i]);
            if (i < columnsSearch.length - 1) selectQuery.append("`, `");
        }

        selectQuery.append("` FROM `")
                .append(table)
                .append("` WHERE ");

        Set<Map.Entry<String, Object>> colValPairs = arguments.valueSet();
        int index = 0;
        int argsLastIndex = colValPairs.size() - 1;
        for (Map.Entry<String, Object> colValPair : colValPairs) {
            String column = colValPair.getKey();
            String value = colValPair.getValue().toString();

            selectQuery.append(" `").append(column).append("` = '").append(value).append("' ");

            if (index < argsLastIndex) selectQuery.append("AND");
            if (index == argsLastIndex) selectQuery.append(";");

            index++;
        }

        return selectQuery;
    }

    StringBuilder buildInsertQuery(String table, @NonNull ContentValues colVals) {
        Set<Map.Entry<String, Object>> colValPairs = colVals.valueSet();
        int index = 0;
        int argsLastIndex = colValPairs.size() - 1;
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (Map.Entry<String, Object> colValPair : colValPairs) {
            String column = colValPair.getKey();
            String value = colValPair.getValue().toString();

            columns.append("'").append(column);
            values.append("'").append(value);

            if (index < argsLastIndex) {
                columns.append("', ");
                values.append("', ");

            } else {
                columns.append("')");
                values.append("');");
            }

            index++;
        }

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("INSERT INTO `")
                .append(table)
                .append("` ")
                .append(columns)
                .append(" VALUES")
                .append(values);

        return selectQuery;
    }

    void logTablesTest() {
        String query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";
        Cursor cursor = null;

        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }
            cursor = mReadableDB.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                StringBuilder data = new StringBuilder("List of CFL Tables:\n");

                do {
                    data.append(cursor.getString(cursor.getColumnIndex("name")));
                    data.append("\n");
                } while (cursor.moveToNext());

                Log.d(TAG, data.toString());
            }

        } catch (Exception e) {
            Log.d(TAG, "EXCEPTION! " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    void queryIdTest(String table, String name) {
        Log.d(TAG, "Searching `id` of '" + name + "' from `" + table + "`....");

        long newEntryId = queryId(SourceEntry.TABLE_NAME, name);
        if (newEntryId == ID_NOT_FOUND) Log.d(TAG, "Entry does not exist!");
        else Log.d(TAG, name + " has id of " + newEntryId);
    }

    void insertIdTest(String table, String name) {
        Log.d(TAG, "Inserting '" + name + "' into `" + table + "`....");

        try {
            long newEntryId = insertEntry(table, name);
            if (newEntryId == ID_NOT_FOUND) Log.d(TAG, "Entry does not exist!");
            else Log.d(TAG, name + " has id of " + newEntryId);

        } catch (SQLiteConstraintException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

}
