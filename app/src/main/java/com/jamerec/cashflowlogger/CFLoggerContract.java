package com.jamerec.cashflowlogger;

import android.provider.BaseColumns;

public final class CFLoggerContract {
    public static final String DATABASE_NAME = "cash_flow_logger";

    // Prevent instantiation
    CFLoggerContract() {
    }

    public static final class FractionTextEntry implements BaseColumns {
        public static final String TABLE_NAME = "fraction_text";
        public static final String COL_ID = "id";
        public static final String COL_VAL_AS_TXT = "val_as_txt";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Look-up table for text representation of fractional values entered by the user.\n" +
                        // "    -- This will be used to avoid confusion in re displaying values entered by the user\n" +
                        // "    -- since the equivalent values are stored as float.\n" +
                        "    " + COL_ID + "         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_VAL_AS_TXT + " TEXT NOT NULL UNIQUE);";
    }

    public static final class BalanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "balance";
        public static final String COL_ID = "id";
        public static final String COL_AMOUNTx100 = "amountX100";
        public static final String COL_INCOME_UPDATE_ID = "income_update_id";
        public static final String COL_EXPENSE_UPDATE_ID = "expense_update_id";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Records the cash at hand.\n" +
                        // "    -- Updated every time an income/expense entry is added\n" +
                        "    " + COL_ID + "                INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_AMOUNTx100 + "        INTEGER  NOT NULL,    -- Amount in Php times 10 to include centavo.\n" +
                        "    " + COL_INCOME_UPDATE_ID + "  INTEGER,              -- ID of income entry that is added to the balance.\n" +
                        "    " + COL_EXPENSE_UPDATE_ID + " INTEGER,              -- ID of expense entry that is deducted from the balance.\n" +
                        "    FOREIGN KEY(" + COL_INCOME_UPDATE_ID + ")\n" +
                        "    REFERENCES " + IncomeEntry.TABLE_NAME + "(" + IncomeEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_EXPENSE_UPDATE_ID + ")\n" +
                        "    REFERENCES " + ExpenseEntry.TABLE_NAME + "(" + ExpenseEntry.COL_ID + "),\n" +
                        "    CHECK((" + COL_INCOME_UPDATE_ID + " != NULL AND " + COL_EXPENSE_UPDATE_ID + " == NULL) OR\n" +  // -- Either income or expense should have
                        "          (" + COL_INCOME_UPDATE_ID + " == NULL AND " + COL_EXPENSE_UPDATE_ID + " != NULL)));\n";   // -- value at a time but not both.
    }

    public static final class FundsEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Look-up table of all declared funds.\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class FundsAllocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds_allocation";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_FUND_ID = "fund_id";
        public static final String COL_PERCENT_ALLOCATION = "percent_allocation";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Stores how the income is to be divided amongst the fund (in percent).\n" +
                        // "    -- This will also serve as how the allocation of fund changes over time.\n" +
                        // "    -- Unused fund will have an allocation of 0%.\n" +
                        "    " + COL_DATETIME + "           DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                        "    " + COL_FUND_ID + "            INTEGER  NOT NULL,\n" +
                        "    " + COL_PERCENT_ALLOCATION + " INTEGER  NOT NULL,\n" +
                        "    FOREIGN KEY(" + COL_FUND_ID + ")\n" +
                        "    REFERENCES " + FundsEntry.TABLE_NAME + "(" + FundsEntry.COL_ID + "),\n" +
                        "    PRIMARY KEY(" + COL_DATETIME + ", " + COL_FUND_ID + "));";
    }

    public static final class FundsBalanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds_balance";
        public static final String COL_ID = "id";
        public static final String COL_FUND_ID = "fund_id";
        public static final String COL_AMOUNTx100 = "amountX100";
        public static final String COL_BALANCE_UPDATE_ID = "balance_update_id";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Records the current balance for each fund.\n" +
                        // "    -- Fund amount is increased as per fund allocation percentage when\n" +
                        // "    -- income is added.\n" +
                        // "    -- Expense amount is subtracted to the corresponding fund where it\n" +
                        // "    -- is to be deducted from.\n" +
                        "    " + COL_ID + "                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_FUND_ID + "           INTEGER NOT NULL,\n" +
                        "    " + COL_AMOUNTx100 + "        INTEGER NOT NULL,\n" +   // -- Amount in Php times 10 to include centavo.
                        "    " + COL_BALANCE_UPDATE_ID + " INTEGER NOT NULL,\n" +   // -- References which cuased the update.
                        "    FOREIGN KEY(" + COL_BALANCE_UPDATE_ID + ")\n" +
                        "    REFERENCES " + BalanceEntry.TABLE_NAME + "(" + BalanceEntry.COL_ID + "));\n";
    }

    public static final class SourceEntry implements BaseColumns {
        public static final String TABLE_NAME = "source";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Source of income(e.g. commission, royalties, salary)\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class IncomeEntry implements BaseColumns {
        public static final String TABLE_NAME = "income";
        public static final String COL_ID = "id";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_SOURCE_ID = "source_id";
        public static final String COL_AMOUNTx100 = "amountX100";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "( \n" +
                        // "    -- Income record\n" +
                        "    " + COL_ID + "          INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_DATETIME + "    DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                        "    " + COL_SOURCE_ID + "   INTEGER  NOT NULL,\n" +
                        "    " + COL_AMOUNTx100 + "  INTEGER  NOT NULL,\n" +   // -- Amount in Php times 10 to include centavo.
                        "    FOREIGN KEY(" + COL_SOURCE_ID + ")\n" +
                        "    REFERENCES " + SourceEntry.TABLE_NAME + "(" + SourceEntry.COL_ID + "));";
    }

    public static final class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "product";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Look_up table of product name/type(e.g. salt, shirt, internet)\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class TagEntry implements BaseColumns {
        public static final String TABLE_NAME = "tag";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TagEntry.TABLE_NAME + "(\n" +
                        // "    -- Look-up table of tags/categories of products as specified by the user.\n" +
                        // "    -- This will be used for searching a particular category of items for\n" +
                        // "    -- price comparison or analyzing the expense patern for that category.\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class ProductTagsEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_tags";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_TAG_ID = "tag_id";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Intermediary table for products and tags since a product can have\n" +
                        // "    -- multiple tags.\n" +
                        "    " + COL_PRODUCT_ID + " INTEGER NOT NULL,\n" +
                        "    " + COL_TAG_ID + "     INTEGER NOT NULL,\n" +
                        "    FOREIGN KEY(" + COL_PRODUCT_ID + ")\n" +
                        "    REFERENCES " + ProductEntry.TABLE_NAME + "(" + ProductEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_TAG_ID + ")\n" +
                        "    REFERENCES " + TagEntry.TABLE_NAME + "(" + TagEntry.COL_ID + "),\n" +
                        "    PRIMARY KEY(" + COL_PRODUCT_ID + ", " + COL_TAG_ID + "));";
    }

    public static final class BrandEntry implements BaseColumns {
        public static final String TABLE_NAME = "brand";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Look-up table for product brands\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class UnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "unit";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Look-up table for unit of measurement\n" +
                        "    " + COL_ID + "   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_NAME + " TEXT    NOT NULL UNIQUE COLLATE NOCASE);";
    }

    public static final class ProductSizeEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_size";
        public static final String COL_ID = "id";
        public static final String COL_SIZE = "size";
        public static final String COL_SIZE_TXT = "size_txt";
        public static final String COL_UNIT_ID = "unit_id";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "   -- Look-up table of packaging/serving size of products\n" +
                        "    " + COL_ID + "       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_SIZE + "     REAL    NOT NULL,\n" +
                        "    " + COL_SIZE_TXT + " INTEGER,\n" +          // -- ID from look-up table fraction_text.
                        "    " + COL_UNIT_ID + "  INTEGER NOT NULL,\n" +
                        "    FOREIGN KEY(" + COL_SIZE_TXT + ")\n" +
                        "    REFERENCES " + FractionTextEntry.TABLE_NAME + "(" + FractionTextEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_UNIT_ID + ")\n" +
                        "    REFERENCES " + UnitEntry.TABLE_NAME + "(" + UnitEntry.COL_ID + "),\n" +
                        "    CONSTRAINT " + TABLE_NAME + "\n" +
                        "    UNIQUE(" + COL_SIZE + ", " + COL_UNIT_ID + "));";
    }

    public static final class ProductVariantEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_variant";
        public static final String COL_ID = "id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_BRAND_ID = "brand_id";
        public static final String COL_PRODUCT_SIZE_ID = "product_size_id";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- List of product for each brand and package size\n" +
                        "    " + COL_ID + "              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_PRODUCT_ID + "      INTEGER NOT NULL,\n" +
                        "    " + COL_BRAND_ID + "        INTEGER NOT NULL,\n" +
                        "    " + COL_PRODUCT_SIZE_ID + " INTEGER NOT NULL,\n" +
                        "    FOREIGN KEY(" + COL_PRODUCT_ID + ")\n" +
                        "    REFERENCES " + ProductEntry.TABLE_NAME + "(" + ProductEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_BRAND_ID + ")\n" +
                        "    REFERENCES " + BrandEntry.TABLE_NAME + "(" + BrandEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_PRODUCT_SIZE_ID + ")\n" +
                        "    REFERENCES " + ProductSizeEntry.TABLE_NAME + "(" + ProductSizeEntry.TABLE_NAME + "),\n" +
                        "    CONSTRAINT " + TABLE_NAME + "\n" +
                        "    UNIQUE(" + COL_PRODUCT_ID + ", " + COL_BRAND_ID + ", " + COL_PRODUCT_SIZE_ID + "));";
    }

    public static final class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COL_ID = "id";
        public static final String COL_DATE = "date";
        public static final String COL_PRODUCT_VARIANT_ID = "product_variant_id";
        public static final String COL_PRICEx100 = "priceX100";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Purchased item defined as the combination of a particular product and\n" +
                        // "    -- its price at a certain date. It will serve as a record of the change\n" +
                        // "    -- in price over time of a product.\n" +
                        "    " + COL_ID + "                 INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_DATE + "               DATE     DEFAULT CURRENT_DATE,\n" +
                        "    " + COL_PRODUCT_VARIANT_ID + " INTEGER  NOT NULL,\n" +
                        "    " + COL_PRICEx100 + "          INTEGER  NOT NULL,\n" +   // -- Price in Php times 10 to include centavo.
                        "    FOREIGN KEY(" + COL_PRODUCT_VARIANT_ID + ")\n" +
                        "    REFERENCES " + ProductVariantEntry.TABLE_NAME + "(" + ProductVariantEntry.COL_ID + "));";
    }

    public static final class ExpenseEntry implements BaseColumns {
        public static final String TABLE_NAME = "expense";
        public static final String COL_ID = "id";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_ITEM_ID = "item_id";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_QUANTITY_TXT = "quantity_txt";
        public static final String COL_REMARKS = "remarks";
        public static final String CREATE_TABLE =
                "  CREATE TABLE " + TABLE_NAME + "(\n" +
                        // "    -- Expenses record\n" +
                        "    " + COL_ID + "            INTEGER  PRIMARY KEY AUTOINCREMENT,\n" +
                        "    " + COL_DATETIME + "      DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                        "    " + COL_ITEM_ID + "       INTEGER  NOT NULL,\n" +
                        "    " + COL_QUANTITY + "      REAL     NOT NULL,\n" +
                        "    " + COL_QUANTITY_TXT + "  INTEGER,\n" +          // -- ID from look-up table fraction_text.
                        "    " + COL_REMARKS + "       TEXT,\n" +
                        "    FOREIGN KEY(" + COL_QUANTITY_TXT + ")\n" +
                        "    REFERENCES " + FractionTextEntry.TABLE_NAME + "(" + FractionTextEntry.COL_ID + "),\n" +
                        "    FOREIGN KEY(" + COL_ITEM_ID + ")\n" +
                        "    REFERENCES " + ItemEntry.TABLE_NAME + "(" + ItemEntry.COL_ID + "));";
    }
}
