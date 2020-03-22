package com.jamerec.cashflowlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jamerec.cashflowlogger.CFLoggerContract.*;

public class CFLoggerOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "CFLoggerOpenHelper";

    private static final int DATABASE_VERSION = 1;    // has to be 1 first time or app will crash

    static final boolean ENTRY_FAILED = false;
    static final boolean ENTRY_ADDED = true;
    static final boolean IS_100_PERCENT = true;
    static final int NO_MATCH_FOUND = 0;
    static final long NO_ID_COL = -123;
    static final long ID_NOT_FOUND = -456;
    static final long ID_NULL = -789;
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

    // Database
    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    CFLoggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Construct CFLoggerOpenHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating CFLogger DB Schema");
        for (String[] createTable : CFL_TABLES) {
            db.execSQL(createTable[1]);
        }
        Log.d(TAG, "Created CFLogger DB Schema");

        initializeDB(db);
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

    /**
     * Updates the database values for the first time. Initial values are set for tables that are
     * queried every time and update is made, such as `funds`, `funds_allocation`, `income`,
     * `balance`, `funds_balance` tables.
     *
     * @param db the database to be initialized
     */
    private void initializeDB(@NonNull SQLiteDatabase db) {
        Log.d(TAG, "Initializing CFLogger DB");

        String defaultFundName = "Basic Needs";
        int defaultAllocationPercentage = 100;
        String initialBalance = "Initial Balance";
        int initialAmount = 0;

        // * Add 'Basic Needs' as a default Fund in `funds` table then get its id.
        db.execSQL("INSERT INTO " + FundsEntry.TABLE_NAME +
                " (" + FundsEntry.COL_NAME + ")" +
                " VALUES('" + defaultFundName + "')");

        Cursor fundIdRes = db.rawQuery(
                "SELECT " + FundsEntry.COL_ID + " FROM " + FundsEntry.TABLE_NAME +
                        " WHERE " + FundsEntry.COL_NAME + " = '" + defaultFundName + "'",
                null);
        fundIdRes.moveToFirst();
        long defaultFundId = fundIdRes.getLong(fundIdRes.getColumnIndex(ID_COL));
        fundIdRes.close();

        // * Add the allocation(100 percent) for the default fund in `funds_allocation`.
        db.execSQL("INSERT INTO " + FundsAllocationEntry.TABLE_NAME +
                " (" + FundsAllocationEntry.COL_FUND_ID + ", " + FundsAllocationEntry.COL_PERCENT_ALLOCATION + ")" +
                " VALUES('" + defaultFundId + "', '" + defaultAllocationPercentage + "')");

        // * Add 'Initial Balance' to source of `source` table then get its id.
        db.execSQL("INSERT INTO " + SourceEntry.TABLE_NAME +
                " (" + SourceEntry.COL_NAME + ")" +
                " VALUES('" + initialBalance + "')");

        Cursor sourceIdRes = db.rawQuery(
                "SELECT " + SourceEntry.COL_ID + " FROM " + SourceEntry.TABLE_NAME +
                        " WHERE " + SourceEntry.COL_NAME + " = '" + initialBalance + "'",
                null);
        sourceIdRes.moveToFirst();
        long sourceId = sourceIdRes.getLong(sourceIdRes.getColumnIndex(ID_COL));
        sourceIdRes.close();

        // * Add the initial entry into `income` table with the following values:
        //   > 'source_id'  = sourceId
        //   > 'amountX100' = 0
        //   and retrieve its id.
        db.execSQL("INSERT INTO " + IncomeEntry.TABLE_NAME +
                " (" + IncomeEntry.COL_SOURCE_ID + ", " + IncomeEntry.COL_AMOUNTx100 + ")" +
                " VALUES('" + sourceId + "', '" + initialAmount + "')");

        Cursor incomeIdRes = db.rawQuery(
                "SELECT " + IncomeEntry.COL_ID + " FROM " + IncomeEntry.TABLE_NAME +
                        " WHERE " + IncomeEntry.COL_SOURCE_ID + " = '" + sourceId +
                        "' AND " + IncomeEntry.COL_AMOUNTx100 + " = '" + initialAmount + "'",
                null);
        incomeIdRes.moveToFirst();
        long incomeId = incomeIdRes.getLong(incomeIdRes.getColumnIndex(ID_COL));
        incomeIdRes.close();

        // * Add the initial entry for `balance` table with the following values:
        //   > 'amountX100'        = initialAmount
        //   > 'income_update_id'  = incomeId
        //   > 'expense_update_id' = NULL
        // and retrieve its id.
        db.execSQL("INSERT INTO " + BalanceEntry.TABLE_NAME +
                " (" + BalanceEntry.COL_AMOUNTx100 + ", " + BalanceEntry.COL_INCOME_UPDATE_ID + ")" +
                " VALUES('" + initialAmount + "', '" + incomeId + "')");

        Cursor balanceIdRes = db.rawQuery(
                "SELECT " + BalanceEntry.COL_ID + " FROM " + BalanceEntry.TABLE_NAME +
                        " WHERE " + BalanceEntry.COL_AMOUNTx100 + " = '" + initialAmount +
                        "' AND " + BalanceEntry.COL_INCOME_UPDATE_ID + " = '" + incomeId + "'",
                null);
        balanceIdRes.moveToFirst();
        long balanceId = balanceIdRes.getLong(balanceIdRes.getColumnIndex(ID_COL));
        balanceIdRes.close();

        // * Add the initial entry for `funds_balance` table with the following values:
        //   > 'fund_id'           = defaultFundId
        //   > 'balance_update_id' = balanceId
        //   > 'amountX100'        = initialAmount
        db.execSQL("INSERT INTO " + FundsBalanceEntry.TABLE_NAME +
                " (" + FundsBalanceEntry.COL_FUND_ID + ", " + FundsBalanceEntry.COL_BALANCE_UPDATE_ID + ", "
                + FundsBalanceEntry.COL_AMOUNTx100 + ")" +
                " VALUES('" + defaultFundId + "', '" + balanceId + "', '" + initialAmount + "')");

        Log.d(TAG, "Initialized CFLogger DB");
    }

    /**
     * Adds entry for tables for logging income.
     *
     * @param incomeSource source of income to be logged
     * @param amount       the amount of income to be logged
     * @param fundsList    the list of funds and the allocation amount for each fund
     */
    void logIncome(String incomeSource, PhCurrency amount, ArrayList<FundItem> fundsList) {
        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            if (mWritableDB == null) mWritableDB = getWritableDatabase();

            mWritableDB.beginTransaction();

            // * Get the `id` of the 'incomeSource' from `source` table. If not found add an entry
            //   for it.
            long incomeSourceId = queryId(SourceEntry.TABLE_NAME, incomeSource);
            if (incomeSourceId == ID_NOT_FOUND)
                incomeSourceId = insertEntry(SourceEntry.TABLE_NAME, incomeSource);

            // * Add an entry in `income` table with the following values:
            //      > `source_id`  = the id of 'incomeSource'
            //      > `amountX100` = the value from amount.getAmountX100()
            //   and get its id.
            ContentValues newIncomeEntry = new ContentValues();
            newIncomeEntry.put(IncomeEntry.COL_SOURCE_ID, incomeSourceId);
            newIncomeEntry.put(IncomeEntry.COL_AMOUNTx100, amount.getAmountX100());
            long incomeId = insertEntry(IncomeEntry.TABLE_NAME, newIncomeEntry);

            // Verify database income update
            printIncomeRecord();

            // * Update the record for cash at hand (`balance` table) by calling
            //   updateCashBalance() with the following arguments:
            //      > amountDiff = amount
            //      > incomeId   = ID of the recently added entry in `income` table
            //      > expenseId  = ID_NULL (-789)
            long balanceUpdateId = updateCashBalance(amount, incomeId, ID_NULL);

            // * Update the `funds_balance` table by calling updateFundBalance() as
            //   per the allocation amount from 'fundsList' ArrayList and the id retrieved from
            //   updateCashBalance().
            for (FundItem fundAllocation : fundsList) {
                long fundId = queryId(FundsEntry.TABLE_NAME, fundAllocation.getName());
                updateFundBalance(fundId, balanceUpdateId, fundAllocation.getAmount());
            }

            mWritableDB.setTransactionSuccessful();

        } finally {
            if (mWritableDB != null) mWritableDB.endTransaction();
        }
    }

    /**
     * Adds entry for tables related for logging expense.
     *
     * @param expenseItem that contains the details of item/service etc. purchased
     * @throws Exception
     */
    void logExpense(ExpenseItem expenseItem) throws Exception {
        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            if (mWritableDB == null) mWritableDB = getWritableDatabase();

            mWritableDB.beginTransaction();

            // * Get the id of the product from the `product` table. If none was
            //   found add an entry for it in the table.
            long productId = queryId(ProductEntry.TABLE_NAME, expenseItem.getItemName());
            if (productId == ID_NOT_FOUND)
                productId = insertEntry(ProductEntry.TABLE_NAME, expenseItem.getItemName());

            // * Get the ids of the tags associated with the product from the `tags` table.
            //   Add to the `tags` table those that are not yet recorded.
            List productTags = expenseItem.getTags();
            long[] tagIds = new long[productTags.size()];
            int index = 0;
            for (Object tag : productTags) {
                tagIds[index] = queryId(TagEntry.TABLE_NAME, (String) tag);

                if (tagIds[index] == ID_NOT_FOUND)
                    tagIds[index] = insertEntry(TagEntry.TABLE_NAME, (String) tag);

                index++;
            }

            // * Check if the tags given are matched in the `product_tags` intermediary table and
            //   add it if it is not yet matched.
            String matchQuery = "SELECT EXISTS(SELECT 1 FROM " + ProductTagsEntry.TABLE_NAME +
                    " WHERE " + ProductTagsEntry.COL_PRODUCT_ID + " = '" +
                    productId + "' AND " + ProductTagsEntry.COL_TAG_ID + " = ? LIMIT 1);";
            Cursor matchCursor = null;
            try {
                for (long tagId : tagIds) {
                    matchCursor = mReadableDB.rawQuery(
                            matchQuery, new String[]{String.valueOf(tagId)});
                    if (matchCursor.moveToFirst()) {
                        if (matchCursor.getInt(0) == NO_MATCH_FOUND) {
                            ContentValues newProductTagEntry = new ContentValues();
                            newProductTagEntry.put(ProductTagsEntry.COL_PRODUCT_ID, productId);
                            newProductTagEntry.put(ProductTagsEntry.COL_TAG_ID, tagId);
                            mWritableDB.insertOrThrow(
                                    ProductTagsEntry.TABLE_NAME,
                                    null,
                                    newProductTagEntry);
                        }
                    }
                }

            } catch (Exception e) {
                Log.d(TAG, "EXCEPTION!!! " + e);

            } finally {
                if (matchCursor != null)
                    matchCursor.close();
            }

            // * Get the id of the brand from the `brand` table, add an entry for it if none
            //   was found.
            long brandId = queryId(BrandEntry.TABLE_NAME, expenseItem.getBrand());
            if (brandId == ID_NOT_FOUND)
                brandId = insertEntry(BrandEntry.TABLE_NAME, expenseItem.getBrand());

            // * Get the id of the unit from the `unit` table, add an entry for it if none
            //   was found.
            Measures productSize = expenseItem.getSize();
            String unitOfMeasure = productSize.getUnitOfMeasure();
            long unitId = queryId(UnitEntry.TABLE_NAME, unitOfMeasure);
            if (unitId == ID_NOT_FOUND)
                unitId = insertEntry(UnitEntry.TABLE_NAME, unitOfMeasure);

            // * Check if the size value is a fraction. If it is get its ID from the `fraction_text`
            //   look_up table, or add an entry to it first if its not yet added.
            String sizeTextId = "NULL";
            try {
                String sizeText = productSize.getFractionString();
                long sizeTxtId = queryId(FractionTextEntry.TABLE_NAME, sizeText);
                if (sizeTxtId == ID_NOT_FOUND)
                    sizeTxtId = insertEntry(FractionTextEntry.TABLE_NAME, sizeText);
                sizeTextId = String.valueOf(sizeTxtId);

            } catch (Measures.NotFractionException e) {
                // Do nothing....
            }

            // * Get the id of the packaging size with the corresponding id for unit of measurement
            //   from the `product_size` table, add an entry of it if none was found.
            ContentValues productSizeEntry = new ContentValues();
            productSizeEntry.put(ProductSizeEntry.COL_SIZE, String.valueOf(productSize.getDouble()));
            productSizeEntry.put(ProductSizeEntry.COL_SIZE_TXT, sizeTextId);
            productSizeEntry.put(ProductSizeEntry.COL_UNIT_ID, unitId);

            long productSizeId = queryId(ProductSizeEntry.TABLE_NAME, productSizeEntry);
            if (productSizeId == ID_NOT_FOUND)
                productSizeId = insertEntry(ProductSizeEntry.TABLE_NAME, productSizeEntry);

            // * Get the id for the product variant with the corresponding product id, brand id, and
            //   product size id in the 'product_variant' table, add an entry for it if none was found.
            ContentValues productVariantEntry = new ContentValues();
            productVariantEntry.put(ProductVariantEntry.COL_PRODUCT_ID, productId);
            productVariantEntry.put(ProductVariantEntry.COL_BRAND_ID, brandId);
            productVariantEntry.put(ProductVariantEntry.COL_PRODUCT_SIZE_ID, productSizeId);

            long productVariantId = queryId(ProductVariantEntry.TABLE_NAME, productVariantEntry);
            if (productVariantId == ID_NOT_FOUND)
                productVariantId = insertEntry(ProductVariantEntry.TABLE_NAME, productVariantEntry);

            // * Get the id and latest price for this product variant in the 'item' table. If the
            //   price is different add a new entry with it's current price for this
            //   product variant and get its ID.
            long itemId = ID_NULL;
            long prevItemPrice = -1;
            long curItemPrice = expenseItem.getItemPrice().getAmountX100();
            Cursor itemEntryCursor = null;
            try {
                String[] searchFor = {ID_COL, ItemEntry.COL_PRICEx100};
                String whereClause = ItemEntry.COL_PRODUCT_VARIANT_ID + " = " + productVariantId;
                String orderBy = ItemEntry.COL_DATE + " DESC";
                String limit = "1";
                itemEntryCursor = mReadableDB.query(
                        ItemEntry.TABLE_NAME, searchFor, whereClause,
                        null, null, null,
                        orderBy, limit);

                if (itemEntryCursor.getCount() > 0) {
                    itemEntryCursor.moveToFirst();
                    itemId = itemEntryCursor.getLong(itemEntryCursor.getColumnIndex(ID_COL));
                    prevItemPrice = itemEntryCursor.getLong(
                            itemEntryCursor.getColumnIndex(ItemEntry.COL_PRICEx100));
                }

                // Insert new entry if there was no item entry for this `product_variant` or
                // the price have changed.
                if (prevItemPrice < 0 || prevItemPrice != curItemPrice) {
                    ContentValues newItemEntry = new ContentValues();
                    newItemEntry.put(ItemEntry.COL_PRODUCT_VARIANT_ID, productVariantId);
                    newItemEntry.put(ItemEntry.COL_PRICEx100, curItemPrice);
                    itemId = insertEntry(ItemEntry.TABLE_NAME, newItemEntry);
                }

            } catch (Exception e) {
                Log.d(TAG, "EXCEPTION!!! " + e);

            } finally {
                if (itemEntryCursor != null)
                    itemEntryCursor.close();
            }

            // Just in case....
            if (itemId == ID_NULL)
                throw new Exception("Getting itemId went wrong somewhere!!!....");

            // * Check if the value of quantity is a fraction. If it is get its ID from the `fraction_text`
            //   look_up table, or add an entry of it if its not yet added.
            Measures itemQty = expenseItem.getQuantity();
            String qtyTextId = "NULL";
            try {
                String sizeText = itemQty.getFractionString();
                long qtyTxtId = queryId(FractionTextEntry.TABLE_NAME, sizeText);
                if (qtyTxtId == ID_NOT_FOUND)
                    qtyTxtId = insertEntry(FractionTextEntry.TABLE_NAME, sizeText);
                qtyTextId = String.valueOf(qtyTxtId);

            } catch (Measures.NotFractionException e) {
                // Do nothing....
            }

            // * Add new entry into `expense` table with the given item ID, quantity, and
            //   remarks if there is any, and get the id for the new entry.
            ContentValues newExpenseEntry = new ContentValues();
            newExpenseEntry.put(ExpenseEntry.COL_ITEM_ID, itemId);
            newExpenseEntry.put(ExpenseEntry.COL_QUANTITY, String.valueOf(itemQty.getDouble()));
            newExpenseEntry.put(ExpenseEntry.COL_QUANTITY_TXT, qtyTextId);
            newExpenseEntry.put(ExpenseEntry.COL_REMARKS, expenseItem.getRemarks());
            long expenseId = insertEntry(ExpenseEntry.TABLE_NAME, newExpenseEntry);

            // Verify database update of `expense` table
            printExpenseRecord();

            // * Update the record for cash at hand (`balance` table) by calling
            //   updateCashBalance() with the following arguments:
            //      > amountDiff = expenseItem.getTotalPrice() * -1
            //      > incomeId   =  ID_NULL (-789)
            //      > expenseId  = ID of the recently added entry in `expense` table
            PhCurrency updateAmt = new PhCurrency(expenseItem.getTotalPrice());
            updateAmt.multiplyBy(-1);
            long balanceUpdateId = updateCashBalance(updateAmt, ID_NULL, expenseId);

            // * Update the `funds_balance` table by calling updateFundBalance()
            //   with the following arguments:
            //      > 'fundID'          = id of the expenseItem.mFund
            //      > 'balanceUpdateId' = the returned id of updateCashBalance()
            //      > 'amountDiff'      = expenseItem.getTotalPrice() * -1
            long fundId = queryId(FundsEntry.TABLE_NAME, expenseItem.getFund());
            updateFundBalance(fundId, balanceUpdateId, updateAmt);

            mWritableDB.setTransactionSuccessful();

        } finally {
            if (mWritableDB != null) mWritableDB.endTransaction();
        }

    }

    /**
     * Adds entry for tables for recording the account balance.
     *
     * @param amountDiff the amount to be added or deducted from the current balance
     * @param incomeId   the id of `income` entry during update of the balance; NULL if
     *                   the update of balance is from expense
     * @param expenseId  the id of `expense` entry during update of the balance; NULL if
     *                   the update of balance is from income
     * @return the id of `balance` entry
     * @throws IllegalArgumentException when both of incomeId and expenseId has value
     *                                  or if both values are NULL
     */
    private long updateCashBalance(PhCurrency amountDiff, long incomeId, long expenseId)
            throws IllegalArgumentException {
        // * Check that only one of 'incomeId' and 'expenseId' has a valid id
        //   while the other should have a 'NULL_ID' as value, otherwise throw a IllegalArgumentException.
        if ((incomeId >= 0 && expenseId >= 0) || (incomeId < 0 && expenseId < 0))
            throw new IllegalArgumentException(
                    "Either 'incomeId' or 'incomeId' should have a valid ID but not both....");

        // * Get the latest `amountX100` (cash balance) from `balance` table.
        String balanceTable = BalanceEntry.TABLE_NAME;
        String amountX100Col = BalanceEntry.COL_AMOUNTx100;
        String incomeUpdateIdCol = BalanceEntry.COL_INCOME_UPDATE_ID;
        String expenseUpdateIdCol = BalanceEntry.COL_EXPENSE_UPDATE_ID;

        PhCurrency latestBalance = getCurrentBalance();

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
            mWritableDB.insertOrThrow(balanceTable, null, newBalanceEntry);

            // Verify database balance update
            printBalanceRecord();

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(balanceTable, newBalanceEntry);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return queryId(balanceTable, newBalanceEntry);
    }

    /**
     * Adds entry for tables logging the fund balance.
     *
     * @param fundId          the id of the fund which the balance is to be updated
     * @param balanceUpdateId the id of the `balance` entry that has been added by
     *                        logging an income or expense
     * @param amountDiff      the amount to be added or subtracted from the fund
     * @return                true if logging the record was successful
     */
    private boolean updateFundBalance(long fundId, long balanceUpdateId, PhCurrency amountDiff) {
        String fundsBalanceTable = FundsBalanceEntry.TABLE_NAME;
        String fundIdCol = FundsBalanceEntry.COL_FUND_ID;
        String balanceUpdateIdCol = FundsBalanceEntry.COL_BALANCE_UPDATE_ID;
        String amountX100Col = FundsBalanceEntry.COL_AMOUNTx100;

        // * Get the latest `amountX100` (cash balance) from `funds_balance` table for the
        //   selected fund as per the 'fundId' given.
        PhCurrency latestBalance = new PhCurrency();
        String whereClause = "`" + fundIdCol + "` = '" + fundId + "'";
        String orderBy = "`" + balanceUpdateIdCol + "` DESC";
        String limit = "1";

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
        //      > `fund_id`           = 'fundId'
        //      > `balance_update_id` = 'balanceUpdateId'
        //      > `amountX100`        = the calculated latest balance
        ContentValues newBalanceEntry = new ContentValues();
        newBalanceEntry.put(fundIdCol, fundId);
        newBalanceEntry.put(balanceUpdateIdCol, balanceUpdateId);
        newBalanceEntry.put(amountX100Col, latestBalance.getAmountX100());
        boolean entryAdded = ENTRY_FAILED;
        try {
            if (mWritableDB.insertOrThrow(fundsBalanceTable, null, newBalanceEntry) > 0)
                entryAdded = ENTRY_ADDED;

            // Verify database `fund_balance` update
            printFundRecord(fundId);

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(fundsBalanceTable, newBalanceEntry);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return entryAdded;
    }

    /**
     * Adds an entry to a look-up table whose column are just id and name.
     *
     * @param table to where an entry is to be added
     * @param name  of the new entry to the look-up table
     * @return      the id of the new entry
     * @throws SQLiteConstraintException when an entry of the same name already exist
     */
    long insertEntry(String table, String name) throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        return insertEntry(table, values);
    }

    /**
     * Adds an entry to a table with the corresponding column and values.
     *
     * @param table  the table where an entry is to be added
     * @param values the column-value pairs of the new entry
     * @return       the id of the new entry
     * @throws SQLiteConstraintException when an entry with the same values
     *                                   already exist
     */
    long insertEntry(String table, ContentValues values) throws SQLiteConstraintException {
        // * Add entry to 'table' with the given 'values' by calling mWritableDB.insert().
        try {
            mWritableDB.insertOrThrow(table, null, values);

        } catch (SQLiteConstraintException e) {
            StringBuilder insertQuery = buildInsertQuery(table, values);
            throw new SQLiteConstraintException(
                    "Duplicate entry is not allowed!!!\nInsert query:\n" + insertQuery);
        }

        return queryId(table, values);
    }

    /**
     * Get the id of an entry in a look-up table.
     *
     * @param table the look-up table where to get the id
     * @param name  the name value of the entry
     * @return      > the id of the selected name;
     *              > ID_NOT_FOUND if the entry does not exist;
     *              > NO_ID_COL if the table does not have an id column
     */
    long queryId(String table, String name) {
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);

        return queryId(table, values);
    }

    /**
     * Get the id of an entry in a look-up table.
     *
     * @param table     the look-up table where to get the id
     * @param arguments the values of the entry which the id is being searched
     * @return          > the id of the selected name;
     *                  > ID_NOT_FOUND if the entry does not exist;
     *                  > NO_ID_COL if the table does not have an id column
     */
    long queryId(String table, @NonNull ContentValues arguments) {
        long id;

        Set<Map.Entry<String, Object>> colValPairs = arguments.valueSet();

        // Database query arguments
        String[] idColumn = new String[]{ID_COL};
        StringBuilder whereClause = new StringBuilder();
        String[] whereArgs = new String[colValPairs.size()];

        // Build the whereClause
        // Track loop count using local variables because the usual
        // for loop version for this looks ugly :P
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

        Cursor queryId = mReadableDB.query(
                table, idColumn, whereClause.toString(), whereArgs, null, null, null);

        // No entry of the given values was found
        if (queryId.getCount() <= 0) {
            id = ID_NOT_FOUND;

        } else {
            int idColIndex = queryId.getColumnIndex(ID_COL);

            // The table has no id column
            if (idColIndex < 0) {
                id = NO_ID_COL;

            } else {
                queryId.moveToFirst();
                id = queryId.getLong(idColIndex);
            }
        }

        queryId.close();

        return id;
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Helper Methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Get the current balance amount of the account.
     *
     * @return the current balance amount
     */
    PhCurrency getCurrentBalance() {
        Log.d(TAG, "Retrieving the current balance....");

        // Check for database initialization
        if (mReadableDB == null) mReadableDB = getReadableDatabase();

        PhCurrency latestBalance = new PhCurrency();
        Cursor queryBalance = mReadableDB.query(
                BalanceEntry.TABLE_NAME, new String[]{BalanceEntry.COL_AMOUNTx100}, null,
                null, null, null, BalanceEntry.COL_ID + " DESC", "1");

        if (queryBalance.getCount() > 0) {
            queryBalance.moveToFirst();
            latestBalance.setValue(
                    queryBalance.getLong(
                            queryBalance.getColumnIndex(BalanceEntry.COL_AMOUNTx100)));
        }
        queryBalance.close();

        Log.d(TAG, "Current balance is " + latestBalance.toString());

        return latestBalance;
    }

    /**
     * Get the list of funds and percentage allocation when dividing the income using
     *   the auto allocate feature in logging the income.
     *
     * @return list of funds and percentage allocation
     */
    Map<String, Integer> getFundsAllocationPercentage() {
        Map<String, Integer> fundsAllocationPercentage = new HashMap<>();

        // Funds Allocation Preference List query:
        /* SELECT
             funds.name, funds_allocation.percent_allocation

           FROM funds_allocation
           JOIN funds ON funds.id = funds_allocation.fund_id

           WHERE funds_allocation._date =
             (SELECT MAX(funds_allocation._date) FROM funds_allocation);
         */
        String allocationQuery = "" +
                "SELECT " + FundsEntry.COL_NAME + ", " + FundsAllocationEntry.COL_PERCENT_ALLOCATION +
                " FROM " + FundsAllocationEntry.TABLE_NAME +
                " JOIN " + FundsEntry.TABLE_NAME +
                " ON " + FundsEntry.COL_ID + " = " + FundsAllocationEntry.COL_FUND_ID +
                " WHERE " + FundsAllocationEntry.COL_DATE + "  =" +
                " (SELECT MAX(" + FundsAllocationEntry.COL_DATE + ") FROM " + FundsAllocationEntry.TABLE_NAME + ");";

        Cursor allocationCursor = null;
        if (mReadableDB == null) mReadableDB = getReadableDatabase();
        try {
            allocationCursor = mReadableDB.rawQuery(allocationQuery, null);
            for (allocationCursor.moveToFirst(); !allocationCursor.isAfterLast(); allocationCursor.moveToNext()) {
                String fundName = allocationCursor.getString(
                        allocationCursor.getColumnIndex(FundsEntry.COL_NAME));
                int percentAllocation = allocationCursor.getInt(
                        allocationCursor.getColumnIndex(FundsAllocationEntry.COL_PERCENT_ALLOCATION));

                fundsAllocationPercentage.put(fundName, percentAllocation);
            }

        } finally {
            if (allocationCursor != null) allocationCursor.close();
        }

        return fundsAllocationPercentage;
    }

    /**
     * Adds new entry for each fund along with latest allocation percentage of the fund
     *   which is used for auto allocation feature for logging of income
     *
     * @param fundsAllocationPercentage the list of fund with its new allocation percentage
     * @return true if update is successful;
     *         false if the allocation sum is not 100 percent
     */
    boolean editFundsAllocationPercentage(@NonNull Map<String, Integer> fundsAllocationPercentage) {
        boolean editSuccess = false;
        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            if (mWritableDB == null) mWritableDB = getWritableDatabase();

            // Use transaction to roll back the database values if problem occurs
            mWritableDB.beginTransaction();

            // Get the current date
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDateStr = dateFormat.format(currentDate);

            // Iterate through the list of fund-percent allocation pair and
            //   insert an entry accordingly.
            int percentSum = 0;     // track running sum making sure it is 100
            for (Map.Entry<String, Integer> allocationEntry : fundsAllocationPercentage.entrySet()) {
                String fundName = allocationEntry.getKey();
                int percentAllocation = allocationEntry.getValue();

                // Get the id of the fund and insert new entry if it does not exist.
                long fundId = queryId(FundsEntry.TABLE_NAME, fundName);
                if (fundId == ID_NOT_FOUND) {
                    fundId = insertEntry(FundsEntry.TABLE_NAME, fundName);
                }

                // Insert new entry with the corresponding column values
                ContentValues newFundAllocationEntry = new ContentValues();
                newFundAllocationEntry.put(FundsAllocationEntry.COL_DATE, currentDateStr);
                newFundAllocationEntry.put(FundsAllocationEntry.COL_FUND_ID, fundId);
                newFundAllocationEntry.put(FundsAllocationEntry.COL_PERCENT_ALLOCATION, percentAllocation);
                mWritableDB.insert(FundsAllocationEntry.TABLE_NAME, null, newFundAllocationEntry);

                percentSum += percentAllocation;    // running sum of the percent allocation
            }

            // Check if the allocation sum is 100
            if (percentSum == 100) {
                editSuccess = true;
                mWritableDB.setTransactionSuccessful();
            }

        } finally {
            if (mWritableDB != null) mWritableDB.endTransaction();
        }

        return editSuccess;
    }

    /*~~~~~~~~~~~~~~~~ Method for providing items in dropdown list in AutoCompleteTextViews ~~~~~~~~~~~~~~~~*/

    List<String> getProductsList() {
        List<String> productsList = new ArrayList<>();

        // Products List SQL query:
        /*
            SELECT product.name FROM product;
         */
        String productsQuery = "" +
                " SELECT " + ProductEntry.COL_NAME +
                " FROM " + ProductEntry.TABLE_NAME;

        Cursor productsCursor = null;
        try {
            productsCursor = mReadableDB.rawQuery(productsQuery, null);
            while (productsCursor.moveToNext()) {
                String productName = productsCursor.getString(
                        productsCursor.getColumnIndex(ProductEntry.COL_NAME));
                productsList.add(productName);
            }

        } finally {
            if (productsCursor != null) productsCursor.close();
        }

        return productsList;
    }

    /**
     * Returns the list of funds that are active (allocation percentage is not zero)
     *  as selection for drop down item in 'Deduct From' input fields.
     *
     * @return the list of active funds
     */
    List<String> getFundsList() {
        List<String> fundsList = new ArrayList<>();
        Map<String, Integer> fundAllocations = getFundsAllocationPercentage();

        // Populate list with the active funds
        for (Map.Entry<String, Integer> fundAllocation : fundAllocations.entrySet()) {
            String fundName = fundAllocation.getKey();
            int percentAllocation = fundAllocation.getValue();

            // Check if fund is active
            if (percentAllocation > 0)
                fundsList.add(fundName);
        }

        return fundsList;
    }

    /**
     * Returns the list of brand that was recorded for this product to be used for
     *  auto suggest feature of the 'Brand' input field.
     *
     * @param product the name of item/service purchased
     * @return        the list of brand that was recorded for this product
     */
    List<String> getBrandList(ExpenseItem product) {
        List<String> brandsList = new ArrayList<>();

        if (product == null) return brandsList;

        String productName = product.getItemName();
        if (productName.isEmpty()) return brandsList;

        // Result column name
        String colBrandName = "Brand_Name";

        // Brands List SQL query:
        /*  SELECT brand.name AS Brand_Name
            FROM   product_variant

            JOIN brand   ON product_variant.brand_id   = brand.id

            WHERE product_variant.product_id =
                (SELECT product.id
                 FROM   product
                 WHERE  product.name = 'Product Name');
         */
        String brandsQuery = "" +
                " SELECT " + BrandEntry.TABLE_NAME + "." + BrandEntry.COL_NAME +
                " AS " + colBrandName +
                " FROM " + ProductVariantEntry.TABLE_NAME +
                "" +
                " JOIN " + BrandEntry.TABLE_NAME +
                " ON " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_BRAND_ID +
                " = " + BrandEntry.TABLE_NAME + "." + BrandEntry.COL_ID +
                "" +
                " WHERE " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_PRODUCT_ID + " = " +
                "  (SELECT " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_ID +
                "   FROM " + ProductEntry.TABLE_NAME +
                "   WHERE " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_NAME +
                "      = '" + productName + "')";

        Cursor brandsCursor = null;
        try {
            brandsCursor = mReadableDB.rawQuery(brandsQuery, null);
            while (brandsCursor.moveToNext()) {
                String brandName = brandsCursor.getString(
                        brandsCursor.getColumnIndex(colBrandName));
                brandsList.add(brandName);
            }

        } finally {
            if (brandsCursor != null) brandsCursor.close();
        }

        return brandsList;
    }

    /**
     * Returns the list of packaging/order size that was recorded for an item/service
     *  (if you know what I mean) to be used in auto suggest feature of 'Item Size' input field.
     *
     * @param product the name of item/service purchased
     * @return        the list of sizes that was recorded for this product
     */
    List<String> getSizesList(ExpenseItem product) {
        List<String> sizesList = new ArrayList<>();

        if (product == null) return sizesList;

        String productName = product.getItemName();
        if (productName.isEmpty()) return sizesList;

        // Result column name
        String colSizeReal = "Size_Real";
        String colSizeTxt = "Size_Txt";
        String colUnit = "Unit";

        // Sizes List SQL Query:
        /*
          SELECT
            product_size.size        AS Size_Real,
            fraction_text.val_as_txt AS Size_Txt,
            unit.name                AS Unit

          FROM product_variant

          JOIN      product_size   ON product_variant.product_size_id = product_size.id
          JOIN      unit           ON product_size.unit_id            = unit.id
          LEFT JOIN fraction_text  ON product_size.size_txt           = fraction_text.id

          WHERE product_variant.product_id =
            (SELECT product.id FROM product WHERE product.name = 'Product Name');
         */
        String sizesQuery = "" +
                " SELECT" +
                " " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_SIZE + " AS " + colSizeReal + "," +
                " " + FractionTextEntry.TABLE_NAME + "." + FractionTextEntry.COL_VAL_AS_TXT + " AS " + colSizeTxt + "," +
                " " + UnitEntry.TABLE_NAME + "." + UnitEntry.COL_NAME + " AS " + colUnit +
                " FROM " + ProductVariantEntry.TABLE_NAME +
                "" +
                " JOIN " + ProductSizeEntry.TABLE_NAME +
                " ON " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_PRODUCT_SIZE_ID +
                " = " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_ID +
                " JOIN " + UnitEntry.TABLE_NAME +
                " ON " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_UNIT_ID +
                " = " + UnitEntry.TABLE_NAME + "." + UnitEntry.COL_ID +
                " LEFT JOIN " + FractionTextEntry.TABLE_NAME +
                " ON " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_SIZE_TXT +
                " = " + FractionTextEntry.TABLE_NAME + "." + FractionTextEntry.COL_ID +
                "" +
                " WHERE " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_PRODUCT_ID + " = " +
                " (SELECT " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_ID +
                " FROM " + ProductEntry.TABLE_NAME +
                " WHERE " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_NAME + " = '" + productName + "')";

        Cursor sizesCursor = null;
        try {
            sizesCursor = mReadableDB.rawQuery(sizesQuery, null);
            while (sizesCursor.moveToNext()) {
                StringBuilder packagingSize = new StringBuilder();

                // For sizes which the numeric value recorded is in fraction form
                String sizeTxt = sizesCursor.getString(
                        sizesCursor.getColumnIndex(colSizeTxt));

                if (sizeTxt == null) {
                    // For sizes which the numeric value recorded is in decimal form
                    double sizeReal = sizesCursor.getDouble(
                            sizesCursor.getColumnIndex(colSizeReal));
                    int sizeInt = (int) sizeReal;

                    if (sizeReal - sizeInt != 0) {
                        // Values with decimal place value
                        packagingSize.append(sizeReal);

                    } else {
                        // Whole number values
                        packagingSize.append(sizeInt);
                    }

                } else {
                    packagingSize.append(sizeTxt);
                }

                String unit = sizesCursor.getString(sizesCursor.getColumnIndex(colUnit));
                packagingSize.append(unit);

                sizesList.add(packagingSize.toString());
            }

        } finally {
            if (sizesCursor != null) sizesCursor.close();
        }

        return sizesList;
    }

    /**
     * Returns the Tags associated with the given product to be used in
     * RecyclerView shown in ExpenseLogDetailsFragment.
     *
     * @param product the product which the tags being searched
     * @return
     */
    List<String> retrieveTags(ExpenseItem product) {
        List<String> tagsList = new ArrayList<>();

        if (product == null) return tagsList;

        String productName = product.getItemName();
        if (productName.isEmpty()) return tagsList;

        // Tags List SQL query:
        /*
            SELECT tag.name
            FROM product_tags
            JOIN tag ON product_tags.tag_id = tag.id
            WHERE product_tags.product_id =
                (SELECT product.id
                 FROM product
                 WHERE product.name = ItemName);
         */
        String tagsQuery = "" +
                " SELECT " + TagEntry.TABLE_NAME + "." + TagEntry.COL_NAME +
                " FROM " + ProductTagsEntry.TABLE_NAME +
                " JOIN " + TagEntry.TABLE_NAME +
                " ON " + ProductTagsEntry.TABLE_NAME + "." + ProductTagsEntry.COL_TAG_ID +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.COL_ID +
                " WHERE " + ProductTagsEntry.TABLE_NAME + "." + ProductTagsEntry.COL_PRODUCT_ID +
                " = (SELECT " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_ID +
                "    FROM " + ProductEntry.TABLE_NAME +
                "    WHERE " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_NAME +
                "    = '" + productName + "')";

        Cursor tagsCursor = null;
        try {
            tagsCursor = mReadableDB.rawQuery(tagsQuery, null);
            while (tagsCursor.moveToNext()) {
                String tag = tagsCursor.getString(
                        tagsCursor.getColumnIndex(TagEntry.COL_NAME));
                tagsList.add(tag);
            }

        } finally {
            if (tagsCursor != null) tagsCursor.close();
        }
        return tagsList;
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Methods for verifying logging of records ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /**
     * Prints the Income Record into Logcat showing the Date Time of log, Source of income,
     *   and the amount(x100) of income added.
     */
    private void printIncomeRecord() {
        StringBuilder incomeRecord = new StringBuilder("Income Record \nDate Time, Source, Amount_x100");

        // Income Record SQL query:
        /* SELECT
             income.datetime, source.name, income.amountX100

           FROM income
           INNER JOIN source ON source.id = income.source_id

           ORDER BY income.datetime ASC;
         */
        String recordQuery = "SELECT " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + ", " +
                SourceEntry.TABLE_NAME + "." + SourceEntry.COL_NAME + ", " +
                IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_AMOUNTx100 +
                " FROM " + IncomeEntry.TABLE_NAME +
                " INNER JOIN " + SourceEntry.TABLE_NAME +
                " ON " + SourceEntry.TABLE_NAME + "." + SourceEntry.COL_ID +
                " = " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_SOURCE_ID +
                " ORDER BY " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + " ASC";

        // Retrieve result from database and build the Income Record
        Cursor recordCursor = null;
        try {
            recordCursor = mReadableDB.rawQuery(recordQuery, null);
            while (recordCursor.moveToNext()) {
                // Get the entry details
                String dateTime = recordCursor.getString(
                        recordCursor.getColumnIndex(IncomeEntry.COL_DATETIME));
                String incomeSource = recordCursor.getString(
                        recordCursor.getColumnIndex(SourceEntry.COL_NAME));
                long amountX100 = recordCursor.getLong(
                        recordCursor.getColumnIndex(IncomeEntry.COL_AMOUNTx100));

                // Build the new record row
                incomeRecord.append("\n")
                        .append(dateTime).append(", ")
                        .append(incomeSource).append(", ")
                        .append(amountX100);
            }

            Log.d(TAG, incomeRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }

    }

    /**
     * Prints the Expenses Record into Logcat showing the details as follows:
     *  Item Name, Brand Name, Item Size, Quantity, Unit Price, Total Price, Remarks.
     */
    private void printExpenseRecord() {
        StringBuilder expenseRecord = new StringBuilder(
                "Expense Record\nItem Name, Brand Name, Item Size, Quantity, Unit Price, Total Price, Remarks");

        // Column names of returned table
        String colDateTime = "Date_Time";
        String colItem = "Item";
        String colBrand = "Brand";
        String colSizeReal = "Size_Real";
        String colSizeTextID = "Size_Text_ID";
        String colUnit = "Unit";
        String colQtyReal = "Qty_Real";
        String colQtyTextID = "Qty_Text_ID";
        String colUnitPrice = "Unit_PriceX100";
        String colRemarks = "Remarks";

        // Expense Record SQL query:
        /* SELECT
             expense.datetime        AS Date_Time,
             product.name            AS Item,
             brand.name              AS Brand,
             product_size.size       AS Size_Real,
             product_size.size_txt   AS Size_Text,
             unit.name               AS Unit,
             expense.quantity        AS Qty_Real,
             expense.quantity_txt    AS Qty_Text,
             item.priceX100          AS Unit_Price,
             expense.remarks         AS Remarks

           FROM expense
           JOIN item            ON expense.item_id                 = item.id
           JOIN product_variant ON item.product_variant_id         = product_variant.id
           JOIN product         ON product_variant.product_id      = product.id
           JOIN brand           ON product_variant.brand_id        = brand.id
           JOIN product_size    ON product_variant.product_size_id = product_size.id
           JOIN unit            ON product_size.unit_id

           ORDER BY expense.id ASC;
         */
        String recordQuery = "SELECT " +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_DATETIME + " AS " + colDateTime + "," +
                ProductEntry.TABLE_NAME + "." + ProductEntry.COL_NAME + " AS " + colItem + "," +
                BrandEntry.TABLE_NAME + "." + BrandEntry.COL_NAME + " AS " + colBrand + "," +
                ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_SIZE + " AS " + colSizeReal + "," +
                ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_SIZE_TXT + " AS " + colSizeTextID + "," +
                UnitEntry.TABLE_NAME + "." + UnitEntry.COL_NAME + " AS " + colUnit + "," +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_QUANTITY + " AS " + colQtyReal + "," +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_QUANTITY_TXT + " AS " + colQtyTextID + "," +
                ItemEntry.TABLE_NAME + "." + ItemEntry.COL_PRICEx100 + " AS " + colUnitPrice + "," +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_REMARKS + " AS " + colRemarks +
                "" +
                " FROM " + ExpenseEntry.TABLE_NAME +
                " JOIN " + ItemEntry.TABLE_NAME +
                " ON " + ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_ITEM_ID +
                " = " + ItemEntry.TABLE_NAME + "." + ItemEntry.COL_ID +
                " JOIN " + ProductVariantEntry.TABLE_NAME +
                " ON " + ItemEntry.TABLE_NAME + "." + ItemEntry.COL_PRODUCT_VARIANT_ID +
                " = " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_ID +
                " JOIN " + ProductEntry.TABLE_NAME +
                " ON " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_PRODUCT_ID +
                " = " + ProductEntry.TABLE_NAME + "." + ProductEntry.COL_ID +
                " JOIN " + BrandEntry.TABLE_NAME +
                " ON " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_BRAND_ID +
                " = " + BrandEntry.TABLE_NAME + "." + BrandEntry.COL_ID +
                " JOIN " + ProductSizeEntry.TABLE_NAME +
                " ON " + ProductVariantEntry.TABLE_NAME + "." + ProductVariantEntry.COL_PRODUCT_SIZE_ID +
                " = " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_ID +
                " JOIN " + UnitEntry.TABLE_NAME +
                " ON " + ProductSizeEntry.TABLE_NAME + "." + ProductSizeEntry.COL_UNIT_ID +
                " = " + UnitEntry.TABLE_NAME + "." + UnitEntry.COL_ID +
                "" +
                " ORDER BY " + ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_ID + " ASC";

        // Retrieve result from database and build the Expense Record.
        Cursor recordCursor = null;
        try {
            recordCursor = mReadableDB.rawQuery(recordQuery, null);
            while (recordCursor.moveToNext()) {
                String dateTime = recordCursor.getString(
                        recordCursor.getColumnIndex(colDateTime));
                String itemName = recordCursor.getString(
                        recordCursor.getColumnIndex(colItem));
                String brandName = recordCursor.getString(
                        recordCursor.getColumnIndex(colBrand));
                double sizeReal = recordCursor.getDouble(
                        recordCursor.getColumnIndex(colSizeReal));
                long sizeTextId = recordCursor.getLong(
                        recordCursor.getColumnIndex(colSizeTextID));
                String unit = recordCursor.getString(
                        recordCursor.getColumnIndex(colUnit));
                double qtyReal = recordCursor.getDouble(
                        recordCursor.getColumnIndex(colQtyReal));
                long qtyTextId = recordCursor.getLong(
                        recordCursor.getColumnIndex(colQtyTextID));
                long unitPriceX100 = recordCursor.getLong(
                        recordCursor.getColumnIndex(colUnitPrice));
                String remarks = recordCursor.getString(
                        recordCursor.getColumnIndex(colRemarks));

                expenseRecord.append("\n")
                        .append(dateTime).append(", ")
                        .append(itemName).append(", ")
                        .append(brandName).append(", ")
                        .append(sizeReal).append(", ")
                        .append(sizeTextId).append(", ")
                        .append(unit).append(", ")
                        .append(qtyReal).append(", ")
                        .append(qtyTextId).append(", ")
                        .append(unitPriceX100).append(", ")
                        .append(remarks);
            }

            Log.d(TAG, expenseRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }
    }

    /**
     * Prints the Total Balance Record into Logcat showing the details as follows:
     *      Date_Time, Update_By, Amount_x100,
     *  where Date_Time is the time of log,
     *        Update_By is whether the update was during an income or expense record log,
     *        Amount_x100 is the amount x100.
     */
    private void printBalanceRecord() {
        StringBuilder balanceRecord = new StringBuilder("Balance Record\n Date_Time, Update_By, Amount_x100");

        // Column names of the returned table
        String colIncomeDatetime = "Income_Datetime";
        String colExpenseDatetime = "Expenses_DateTime";
        String colAmountX100 = "Amount_X100";

        // Balance Record SQL query:
        /* SELECT
             income.datetime    AS Income_Datetime,
             expense.datetime   AS Expenses_DateTime,
             balance.amountX100 AS Amount_X100

           FROM balance
           LEFT JOIN income  ON income.id  = balance.income_update_id
           LEFT JOIN expense ON expense.id = balance.expense_update_id

           ORDER BY balance.id ASC;
         */
        String recordQuery = "SELECT " +
                IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + " AS " + colIncomeDatetime + "," +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_DATETIME + " AS " + colExpenseDatetime + "," +
                BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_AMOUNTx100 + " AS " + colAmountX100 +
                " FROM " + BalanceEntry.TABLE_NAME +
                " LEFT JOIN " + IncomeEntry.TABLE_NAME +
                " ON " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_ID +
                " = " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_INCOME_UPDATE_ID +
                " LEFT JOIN " + ExpenseEntry.TABLE_NAME +
                " ON " + ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_ID +
                " = " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_EXPENSE_UPDATE_ID +
                " ORDER BY " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_ID + " ASC";

        // Retrieve result from database and build the Balance Record
        Cursor recordCursor = null;
        try {
            recordCursor = mReadableDB.rawQuery(recordQuery, null);
            while (recordCursor.moveToNext()) {
                String datetime = recordCursor.getString(
                        recordCursor.getColumnIndex(colIncomeDatetime));
                String updateBy = "Income";
                long amountX100 = recordCursor.getLong(
                        recordCursor.getColumnIndex(colAmountX100));

                // Determine the update source
                if (datetime == null) {
                    datetime = recordCursor.getString(
                            recordCursor.getColumnIndex(colExpenseDatetime));
                    updateBy = "Expense";
                }

                balanceRecord.append("\n")
                        .append(datetime).append(", ")
                        .append(updateBy).append(", ")
                        .append(amountX100);
            }

            Log.d(TAG, balanceRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }
    }

    /**
     * Prints the Fund Balance Record of a particular into the Logcat showing
     *  the details as follows:
     *      Date_Time, Update_By, Amount_x100
     *  where Date_Time is the time of log,
     *        Update_By is whether the update was during an income or expense record log,
     *        Amount_x100 is the amount x100.
     *
     * @param fundId the id of the fund which the record is to printed
     */
    private void printFundRecord(long fundId) {
        // Get the fund name
        String fundName;
        Cursor nameCursor = null;
        try {
            nameCursor = mReadableDB.rawQuery(
                    "SELECT " + FundsEntry.COL_NAME +
                            " FROM " + FundsEntry.TABLE_NAME +
                            " WHERE " + FundsEntry.COL_ID + " = '" + fundId + "'",
                    null);

            nameCursor.moveToFirst();
            fundName = nameCursor.getString(nameCursor.getColumnIndex(FundsEntry.COL_NAME));

        } finally {
            if (nameCursor != null) nameCursor.close();
        }

        StringBuilder fundRecord = new StringBuilder(
                fundName + " Fund Record\n Date_Time, Update_By, Amount_x100");

        // Column names of the returned table
        String colIncomeDatetime = "Income_Datetime";
        String colExpenseDatetime = "Expenses_DateTime";
        String colAmountX100 = "Amount_X100";

        // Funds Balance Record SQL query:
        /* SELECT
             income.datetime          AS Income_Datetime,
             expense.datetime         AS Expenses_DateTime,
             funds_balance.amountX100 AS Amount_X100

           FROM funds_balance
           JOIN      balance ON balance.id = funds_balance.balance_update_id
           LEFT JOIN income  ON income.id  = balance.income_update_id
           LEFT JOIN expense ON expense.id = balance.expense_update_id

           WHERE funds_balance.fund_id = 'Fund_Id'

           ORDER BY balance.id ASC;
         */
        String recordQuery = "SELECT " +
                IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + " AS " + colIncomeDatetime + "," +
                ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_DATETIME + " AS " + colExpenseDatetime + "," +
                FundsBalanceEntry.TABLE_NAME + "." + FundsBalanceEntry.COL_AMOUNTx100 + " AS " + colAmountX100 +
                " FROM " + FundsBalanceEntry.TABLE_NAME +
                " JOIN " + BalanceEntry.TABLE_NAME +
                " ON " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_ID +
                " = " + FundsBalanceEntry.TABLE_NAME + "." + FundsBalanceEntry.COL_BALANCE_UPDATE_ID +
                " LEFT JOIN " + IncomeEntry.TABLE_NAME +
                " ON " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_ID +
                " = " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_INCOME_UPDATE_ID +
                " LEFT JOIN " + ExpenseEntry.TABLE_NAME +
                " ON " + ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COL_ID +
                " = " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_EXPENSE_UPDATE_ID +
                " WHERE " + FundsBalanceEntry.TABLE_NAME + "." + FundsBalanceEntry.COL_FUND_ID +
                " = '" + fundId + "'" +
                " ORDER BY " + BalanceEntry.TABLE_NAME + "." + BalanceEntry.COL_ID + " ASC";

        // Retrieve result from database and build the Fund Balance Record
        Cursor recordCursor = null;
        try {
            recordCursor = mReadableDB.rawQuery(recordQuery, null);
            while (recordCursor.moveToNext()) {
                // Get the entry details
                String datetime = recordCursor.getString(
                        recordCursor.getColumnIndex(colIncomeDatetime));    // Income Date_Time
                String updateBy = "Income";
                long amountX100 = recordCursor.getLong(
                        recordCursor.getColumnIndex(colAmountX100));

                // Determine the update source
                if (datetime == null) {
                    datetime = recordCursor.getString(
                            recordCursor.getColumnIndex(colExpenseDatetime)); // Expense Date_Time
                    updateBy = "Expense";
                }

                // Build the record row
                fundRecord.append("\n")
                        .append(datetime).append(", ")
                        .append(updateBy).append(", ")
                        .append(amountX100);
            }

            Log.d(TAG, fundRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }
    }

    /**
     * Builds the SELECT query which is to displayed for debugging purposes. This is limited only
     * to SELECT queries which does not require table JOINs and has WHERE clause only.
     *
     * @param table         the table being queried
     * @param columnsSearch the column of the result needed
     * @param arguments     the arguments for the WHERE clause
     * @return              the SQL query in String format
     */
    StringBuilder buildSelectQuery(String table, @NonNull String[] columnsSearch, @NonNull ContentValues arguments) {
        StringBuilder selectQuery = new StringBuilder("SELECT `");

        // Build the columns being searched
        for (int i = 0; i < columnsSearch.length; i++) {
            selectQuery.append(columnsSearch[i]);
            if (i < columnsSearch.length - 1) selectQuery.append("`, `");
        }

        selectQuery
                .append("` FROM `")
                .append(table)
                .append("` WHERE ");

        // Build the WHERE clause
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

    /**
     * Builds the INSERT query which is to be displayed for debugging purposes.
     *
     * @param table   the table where the new entry is to be added
     * @param colVals the column-value pairs of the entry to be added
     * @return        the String format of the INSERT query
     */
    @NonNull
    private StringBuilder buildInsertQuery(String table, @NonNull ContentValues colVals) {
        Set<Map.Entry<String, Object>> colValPairs = colVals.valueSet();
        int index = 0;
        int argsLastIndex = colValPairs.size() - 1;
        StringBuilder columns = new StringBuilder("("); // For columns to be updated
        StringBuilder values = new StringBuilder("(");  // For the values of each column
        for (Map.Entry<String, Object> colValPair : colValPairs) {
            String column = colValPair.getKey();
            String value = colValPair.getValue().toString();

            // Opening apostrophe
            columns.append("'").append(column);
            values.append("'").append(value);

            // Closing apostrophe and coma between columns and values
            if (index < argsLastIndex) {
                columns.append("', ");
                values.append("', ");

            } else {
                // Closing apostrophe and parenthesis for the last column-value pair
                columns.append("')");
                values.append("');");
            }

            index++;
        }

        // Build the INSERT query
        StringBuilder insertQuery = new StringBuilder();
        insertQuery
                .append("INSERT INTO `")
                .append(table)
                .append("` ")
                .append(columns)
                .append(" VALUES")
                .append(values);

        return insertQuery;
    }

}
