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

    // Databases
    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    CFLoggerOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Construct CFLoggerOpenHelper");

//        logTablesTest();
//
//        String table = "source";
//        String name = "Restaurant";
//        queryIdTest(table, name);
//        insertIdTest(table, name);
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

    PhCurrency getCurrentBalance() {
        Log.d(TAG, "Retrieving the current balance....");

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
                            insertEntry(ProductTagsEntry.TABLE_NAME, newProductTagEntry);
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
                String limit = "LIMIT 1";
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

    private long updateCashBalance(PhCurrency amountDiff, long incomeId, long expenseId) throws IllegalArgumentException {
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

    Map<String, Integer> getFundsAllocationPercentage() {
        Map<String, Integer> fundsAllocationPercentage = new HashMap<>();

        String allocationQuery = "" +
                "SELECT " + FundsEntry.COL_NAME + ", " + FundsAllocationEntry.COL_PERCENT_ALLOCATION +
                " FROM " + FundsAllocationEntry.TABLE_NAME + " INNER JOIN " + FundsEntry.TABLE_NAME +
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

    boolean editFundsAllocationPercentage(@NonNull Map<String, Integer> fundsAllocationPercentage) {
        boolean editSuccess = false;
        try {
            if (mReadableDB == null) mReadableDB = getReadableDatabase();
            if (mWritableDB == null) mWritableDB = getWritableDatabase();

            mWritableDB.beginTransaction();

            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDateStr = dateFormat.format(currentDate);

            int percentSum = 0;
            for (Map.Entry<String, Integer> allocationEntry : fundsAllocationPercentage.entrySet()) {
                String fundName = allocationEntry.getKey();
                int percentAllocation = allocationEntry.getValue();

                long fundId = queryId(FundsEntry.TABLE_NAME, fundName);
                if (fundId == ID_NOT_FOUND) {
                    fundId = insertEntry(FundsEntry.TABLE_NAME, fundName);
                }

                ContentValues newFundAllocationEntry = new ContentValues();
                newFundAllocationEntry.put(FundsAllocationEntry.COL_DATE, currentDateStr);
                newFundAllocationEntry.put(FundsAllocationEntry.COL_FUND_ID, fundId);
                newFundAllocationEntry.put(FundsAllocationEntry.COL_PERCENT_ALLOCATION, percentAllocation);
                mWritableDB.insert(FundsAllocationEntry.TABLE_NAME, null, newFundAllocationEntry);

                percentSum += percentAllocation;
            }

            if (percentSum == 100) {
                editSuccess = true;
                mWritableDB.setTransactionSuccessful();
            }

        } finally {
            if (mWritableDB != null) mWritableDB.endTransaction();
        }

        return editSuccess;
    }

    long insertEntry(String table, String name) throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        return insertEntry(table, values);
    }

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

    private StringBuilder buildInsertQuery(String table, @NonNull ContentValues colVals) {
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

    private void printIncomeRecord() {
        StringBuilder incomeRecord = new StringBuilder("Income Record \nDate Time, Source, Amount_x100");
        String recordQuery = "SELECT " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + ", " +
                SourceEntry.TABLE_NAME + "." + SourceEntry.COL_NAME + ", " +
                IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_AMOUNTx100 +
                " FROM " + IncomeEntry.TABLE_NAME +
                " INNER JOIN " + SourceEntry.TABLE_NAME +
                " ON " + SourceEntry.TABLE_NAME + "." + SourceEntry.COL_ID +
                " = " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_SOURCE_ID +
                " ORDER BY " + IncomeEntry.TABLE_NAME + "." + IncomeEntry.COL_DATETIME + " ASC";

        Cursor recordCursor = null;
        try {
            recordCursor = mReadableDB.rawQuery(recordQuery, null);
            while (recordCursor.moveToNext()) {
                String dateTime = recordCursor.getString(
                        recordCursor.getColumnIndex(IncomeEntry.COL_DATETIME));
                String incomeSource = recordCursor.getString(
                        recordCursor.getColumnIndex(SourceEntry.COL_NAME));
                long amountX100 = recordCursor.getLong(
                        recordCursor.getColumnIndex(IncomeEntry.COL_AMOUNTx100));

                incomeRecord.append("\n").append(dateTime)
                        .append(", ").append(incomeSource)
                        .append(", ").append(amountX100);
            }

            Log.d(TAG, incomeRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }

    }

    private void printBalanceRecord() {
        StringBuilder balanceRecord = new StringBuilder("Balance Record\n Date_Time, Update_By, Amount_x100");

        // Column names of the returned table
        String colIncomeDatetime = "Income_Datetime";
        String colExpenseDatetime = "Expenses_DateTime";
        String colAmountX100 = "Amount_X100";

        // Join `balance` table with `income` table and `expense` table to get the complete balance record
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
                if (datetime.equals("null")) {
                    datetime = recordCursor.getString(
                            recordCursor.getColumnIndex(colExpenseDatetime));
                    updateBy = "Expense";
                }

                balanceRecord.append("\n").append(datetime)
                        .append(", ").append(updateBy)
                        .append(", ").append(amountX100);
            }

            Log.d(TAG, balanceRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }
    }

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

        // Join `funds` table with `balance` table, `income` table, and `expense` table
        // to get the complete record of the selected fund
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
                if (datetime.equals("null")) {
                    datetime = recordCursor.getString(
                            recordCursor.getColumnIndex(colExpenseDatetime));
                    updateBy = "Expense";
                }

                fundRecord.append("\n").append(datetime)
                        .append(", ").append(updateBy)
                        .append(", ").append(amountX100);
            }

            Log.d(TAG, fundRecord.toString());

        } finally {
            if (recordCursor != null) recordCursor.close();
        }
    }

    private void logTablesTest() {
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

    private void queryIdTest(String table, String name) {
        Log.d(TAG, "Searching `id` of '" + name + "' from `" + table + "`....");

        long newEntryId = queryId(SourceEntry.TABLE_NAME, name);
        if (newEntryId == ID_NOT_FOUND) Log.d(TAG, "Entry does not exist!");
        else Log.d(TAG, name + " has id of " + newEntryId);
    }

    private void insertIdTest(String table, String name) {
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
