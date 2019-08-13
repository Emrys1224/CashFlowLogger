package com.jamerec.cashflowlogger;

import android.provider.BaseColumns;

public final class CFLoggerContract {
    public static final String DATABASE_NAME = "cash_flow_logger";

    public static final class FractionTextEntry implements BaseColumns {
        public static final String TABLE_NAME = "fraction_text";
        public static final String COL_ID = "id";
        public static final String COL_VAL_AS_TXT = "val_as_txt";
    }

    public static final class BalanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "balance";
        public static final String COL_ID = "id";
        public static final String COL_AMOUNTx100 = "amountX100";
        public static final String COL_INCOME_UPDATE_ID = "income_update_id";
        public static final String COL_EXPENSE_UPDATE_ID = "expense_update_id";
    }

    public static final class FundsEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class FundsAllocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds_allocation";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_FUND_ID = "fund_id";
        public static final String COL_PERCENT_ALLOCATION = "percent_allocation";
    }

    public static final class FundsBalanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "funds_balance";
        public static final String COL_ID = "id";
        public static final String COL_FUND_ID = "fund_id";
        public static final String COL_AMOUNTx100 = "amountX100";
        public static final String COL_BALANCE_UPDATE_ID = "balance_update_id";
    }

    public static final class SourceEntry implements BaseColumns {
        public static final String TABLE_NAME = "source";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class IncomeEntry implements BaseColumns {
        public static final String TABLE_NAME = "income";
        public static final String COL_ID = "id";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_SOURCE_ID = "source_id";
        public static final String COL_AMOUNTx100 = "amountX100";
    }

    public static final class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "product";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class TagEntry implements BaseColumns {
        public static final String TABLE_NAME = "tag";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class ProductTagsEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_tags";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_TAG_ID = "tag_id";
    }

    public static final class BrandEntry implements BaseColumns {
        public static final String TABLE_NAME = "brand";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class UnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "unit";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
    }

    public static final class ProductSizeEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_size";
        public static final String COL_ID = "id";
        public static final String COL_SIZE = "size";
        public static final String COL_SIZE_TXT = "size_txt";
        public static final String COL_UNIT_ID = "unit_id";
    }

    public static final class ProductVariantEntry implements BaseColumns {
        public static final String TABLE_NAME = "product_variant";
        public static final String COL_ID = "id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_BRAND_ID = "brand_id";
        public static final String COL_PRODUCT_SIZE_ID = "product_size_id";
    }

    public static final class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COL_ID = "id";
        public static final String COL_DATE = "date";
        public static final String COL_PRODUCT_VARIANT_ID = "product_variant_id";
        public static final String COL_PRICEx100 = "priceX100";
    }

    public static final class ExpenseEntry implements BaseColumns {
        public static final String TABLE_NAME = "expense";
        public static final String COL_ID = "id";
        public static final String COL_DATETIME = "datetime";
        public static final String COL_ITEM_ID = "item_id";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_QUANTITY_TXT = "quantity_txt";
        public static final String COL_REMARKS = "remarks";
    }
}
