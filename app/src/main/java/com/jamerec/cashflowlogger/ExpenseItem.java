package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant to hold the details for expenses to easily manipulate
 * and get the details between Activities and Fragments.
 */
class ExpenseItem implements Parcelable {
    // Measures value setting return values.
    static final int DONE = 999;
    static final int EMPTY_STRING = 1000;
    static final int INVALID_VALUE = 1001;
    static final int NO_UNIT = 1002;
    static final int ZERO_DENOMINATOR = 1003;

    private String mProduct;        // Product name
    private String mBrand;          // Brand name
    private PhCurrency mPrice;      // Price per item
    private Measures mSize;         // Packaging/serving size of the product
    private Measures mQuantity;     // Quantity of item purchased
    private String mFund;           // Fund from where it is to be deducted
    private List<String> mTags;     // Tags where this product is categorized
    private String mRemarks;

    /**
     * Creates an ExpenseItem with empty details.
     */
    ExpenseItem() {
        this.mProduct = "";
        this.mBrand = "";
        this.mPrice = new PhCurrency();
        this.mSize = new Measures();
        this.mQuantity = new Measures();
        this.mFund = "";
        this.mTags = new ArrayList<>();
        this.mRemarks = "";
    }

    /**
     * Creates a deep copy of an ExpenseItem.
     *
     * @param item to copy the details from.
     */
    ExpenseItem(@NonNull ExpenseItem item) {
        this.mProduct = item.mProduct;
        this.mBrand = item.mBrand;
        this.mPrice.setValue(item.mPrice);
        this.mSize = item.mSize;
        this.mQuantity = item.mQuantity;
        this.mFund = item.mFund;
        this.mTags.addAll(item.mTags);
        this.mRemarks = item.mRemarks;
    }

    protected ExpenseItem(Parcel in) {
        mProduct = in.readString();
        mBrand = in.readString();
        mPrice = in.readParcelable(PhCurrency.class.getClassLoader());
        mSize = in.readParcelable(Measures.class.getClassLoader());
        mQuantity = in.readParcelable(Measures.class.getClassLoader());
        mFund = in.readString();
        mTags = in.createStringArrayList();
        mRemarks = in.readString();
    }

    public static final Creator<ExpenseItem> CREATOR = new Creator<ExpenseItem>() {
        @Override
        public ExpenseItem createFromParcel(Parcel in) {
            return new ExpenseItem(in);
        }

        @Override
        public ExpenseItem[] newArray(int size) {
            return new ExpenseItem[size];
        }
    };

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Getters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /**
     * Get the name the product.
     *
     * @return the name of the product.
     */
    String getItemName() {
        return this.mProduct;
    }

    /**
     * Get the brand of the product.
     *
     * @return the brand name.
     */
    String getBrand() {
        return this.mBrand;
    }

    /**
     * Get the price per unit item of this product.
     *
     * @return the price per item.
     */
    PhCurrency getItemPrice() {
        return this.mPrice;
    }

    /**
     * Get the total price for this expense item.
     *
     * @return total price of expense item.
     */
    PhCurrency getTotalPrice() {
        PhCurrency totalPrice = new PhCurrency(this.mPrice);
        try {
            totalPrice.multiplyBy(this.mQuantity.getDouble());

        } catch (IllegalAccessException iae) {
            totalPrice.multiplyBy(0);
        }

        return totalPrice;
    }

    /**
     * Get the packaging/serving size of this product.
     *
     * @return packaging/serving size.
     */
    Measures getSize() {
        return this.mSize;
    }

    /**
     * The quantity of the purchased product.
     *
     * @return the quantity of purchased product.
     */
    Measures getQuantity() {
        return this.mQuantity;
    }

    /**
     * Get the fund from where the payment for this expense item is to be deducted.
     *
     * @return fund to deduct from.
     */
    String getFund() {
        return this.mFund;
    }

    /**
     * Get the tags associated with this product.
     *
     * @return tags of this product.
     */
    List<String> getTags() {
        return new ArrayList<>(this.mTags);
    }

    /**
     * Get the remarks for this expense item.
     *
     * @return remarks for this item.
     */
    String getRemarks() {
        return this.mRemarks;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Setters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /**
     * Sets the name of the product, at the same time updates the tags for this product
     * accordingly.
     *
     * @param itemName the name of the product purchased
     * @param db       the database where to retrieve the tags associated with the product
     */
    void setItemName(String itemName, CFLoggerOpenHelper db) {
        if (this.mProduct.equals(itemName)) return;

        this.mProduct = itemName;

        // Change the tags according to the product in the database if already exist.
        this.mTags.clear();
        if (db != null)
            this.mTags.addAll(db.retrieveTags(this));
    }

    /**
     * Set the brand name of the product
     *
     * @param brand brand name
     */
    void setBrand(String brand) {
        this.mBrand = brand;
    }

    /**
     * Set the price per unit item
     *
     * @param price of the item per piece/unit
     */
    void setPrice(PhCurrency price) {
        this.mPrice.setValue(price);
    }

    /**
     * Set the packaging size etc. of the product.
     *
     * @param size of the product e.g. 1kilo, 1order, 2liters, 1pack.
     * @return DONE if update of value was successful;
     *         EMPTY_STRING if the given String is empty;
     *         INVALID_VALUE if no numeric value format was found;
     *         NO_UNIT if no unit of measure was found;
     *         ZERO_DENOMINATOR if the denominator for fraction formatted String is zero
     */
    int setSize(String size) {
        if (size.isEmpty()) return EMPTY_STRING;

        try {
            this.mSize.setValue(size);

        } catch (Measures.InvalidValueException ive) {
            return INVALID_VALUE;
        } catch (Measures.NoValidUnitException nvue) {
            return NO_UNIT;
        } catch (Measures.ZeroDenominatorException zde) {
            return ZERO_DENOMINATOR;
        }

        return DONE;
    }

    /**
     * Set the quantity of product purchased.
     *
     * @param quantity of the product purchased.
     */
    void setQuantity(double quantity) {
        try {
            this.mQuantity.setValue(quantity + "orders");
        }

        // Since this is to be set with a number EditText so the chance of triggering
        // these of exception is low unless I forgot about it and used it somewhere else....
        catch (Measures.InvalidValueException ive) {
            System.out.println(ive.getMessage());
            ive.printStackTrace();

        } catch (Measures.NoValidUnitException nvue) {
            System.out.println(nvue.getMessage());
            nvue.printStackTrace();

        } catch (Measures.ZeroDenominatorException zde) {
            System.out.println(zde.getMessage());
            zde.printStackTrace();
        }
    }

    /**
     * Set the fund from where payment is to be deducted.
     *
     * @param fund from where to deduct the payment.
     */
    void setFund(String fund) {
        this.mFund = fund;
    }

    /**
     * Add a tag for this product.
     *
     * @param tag to be added.
     */
    boolean addTag(String tag) {
        if (this.mTags.contains(tag)) return false;

        this.mTags.add(tag);
        return true;
    }

    /**
     * Remove a tag from this product.
     *
     * @param tag to be removed.
     */
    void removeTag(String tag) {
        this.mTags.remove(tag);
    }

    /**
     * Set the remarks for the expense item.
     *
     * @param remarks for this expense item.
     */
    void setRemarks(String remarks) {
        this.mRemarks = remarks;
    }

    /**
     * Confirms that all the fields are filled and are ready to be saved in the database.
     *
     * @return true when the ExpenseItem fields are complete, false otherwise.
     */
    boolean readyToLog() {
        return !mProduct.isEmpty() && !mBrand.isEmpty() && !mPrice.isZero() &&
                !mSize.isCleared() && !mQuantity.isCleared() && !mFund.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder tags = new StringBuilder();
        for (String tag : mTags) {
            tags.append(tag).append(", ");
        }
        return "Expense Item Details\n" +
                "Product:\t\t\t" + getItemName() +
                "\nBrand:\t\t\t\t" + getBrand() +
                "\nPrice per Item:\t\t" + getItemPrice() +
                "\nPackaging Size:\t\t" + getSize() +
                "\nQuantity purchased:\t" + getQuantity() +
                "\nTotal Price:\t\t" + getTotalPrice() +
                "\nDeduct From:\t\t" + getFund() +
                "\nTags:\t\t\t\t" + tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProduct);
        dest.writeString(mBrand);
        dest.writeParcelable(mPrice, flags);
        dest.writeParcelable(mSize, flags);
        dest.writeParcelable(mQuantity, flags);
        dest.writeString(mFund);
        dest.writeStringList(mTags);
    }
}
