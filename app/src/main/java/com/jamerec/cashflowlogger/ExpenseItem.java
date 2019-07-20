package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

class ExpenseItem implements Parcelable {
    //  Result for setting the product size.
    static final int SET_SIZE_OK = 24;      // Given size is good.
    static final int NO_SET_VALUE = 36;     // Given size has no numeric value.
    static final int NO_SET_UNIT = 48;      // Given size has no unit indicated.
    static final int SET_SIZE_EMPTY = 60;   // No given size.

    private String mProduct;        // Product name
    private String mBrand;          // Brand name
    private PhCurrency mPrice;      // Price per item
    private String mSize;           // Packaging/serving size of the product
    private double mQuantity;       // Quantity of item purchased
    private String mFund;           // Fund from where it is to be deducted
    private List<String> mTags;     // Tags where this product is categorized

    ExpenseItem() {
        this.mProduct = "";
        this.mBrand = "";
        this.mPrice = new PhCurrency();
        this.mSize = "";
        this.mQuantity = 0D;
        this.mFund = "";
        this.mTags = new ArrayList<>();
    }

    ExpenseItem(@NonNull ExpenseItem item) {
        this.mProduct = item.mProduct;
        this.mBrand = item.mBrand;
        this.mPrice.setValue(item.mPrice);
        this.mSize = item.mSize;
        this.mQuantity = item.mQuantity;
        this.mFund = item.mFund;
        this.mTags.addAll(item.mTags);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Getters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //

    protected ExpenseItem(@NonNull Parcel in) {
        mProduct = in.readString();
        mBrand = in.readString();
        mPrice = in.readParcelable(PhCurrency.class.getClassLoader());
        mSize = in.readString();
        mQuantity = in.readDouble();
        mFund = in.readString();
        mTags = in.createStringArrayList();
    }

    public static final Creator<ExpenseItem> CREATOR = new Creator<ExpenseItem>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public ExpenseItem createFromParcel(Parcel in) {
            return new ExpenseItem(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public ExpenseItem[] newArray(int size) {
            return new ExpenseItem[size];
        }
    };

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
        totalPrice.multiplyBy(this.mQuantity);
        return totalPrice;
    }

    /**
     * Get the packaging/serving size of this product.
     *
     * @return packaging/serving size.
     */
    String getSize() {
        return this.mSize;
    }

    /**
     * The quantity of the purchased product.
     *
     * @return the quantity of purchased product.
     */
    double getQuantity() {
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //

    void setItemName(String itemName) {
        if (this.mProduct.equals(itemName)) return;

        this.mProduct = itemName;

        // Change the tags according to the product in the database if already exist.
        this.mTags.clear();
        this.mTags.addAll(retrieveTags());
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
     * @param size of the product e.g. 1kilo, 1order, 2liters, 1pack
     * @return SET_SIZE_OK (24) if size is set successfully;
     * NO_SET_VALUE (36) if there is no numeric value given;
     * NO_SET_UNIT (48) if there is unit of measurement given.
     */
    int setSize(String size) {
        if (size.isEmpty()) return SET_SIZE_EMPTY;

        // Extract unit of measurement.
        String unitMeasure = size.replaceAll("[\\d.]", "");
        if (unitMeasure.isEmpty()) return NO_SET_UNIT;

        // Extract numeric value.
        String sizeVal = size.replaceAll("[^\\d.]", "");
        if (sizeVal.isEmpty()) return NO_SET_VALUE;

        this.mSize = size;
        return SET_SIZE_OK;
    }

    /**
     * Set the quantity of product purchased.
     *
     * @param quantity of the product purchased.
     */
    void setQuantity(double quantity) {
        this.mQuantity = quantity;
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
    void addTag(String tag) {
        this.mTags.add(tag);
    }

    /**
     * Remove a tag from this product.
     *
     * @param tag to be removed.
     */
    void removeTag(String tag) {
        this.mTags.remove(tag);
    }

    // ~~~~~~~Method for providing items in dropdown list in AutoCompleteTextViews~~~~~~~~~~~~ //

    /**
     * This retrieves the brands associated with this product and provided as
     * selection for AutoCompleteTextView brand input.
     *
     * @return List of brands for this product.
     */
    List<String> suggestBrands() {
        List<String> brandSelection = new ArrayList<>();

        if (this.mProduct.isEmpty()) return brandSelection;

        // List is to be populated with brands for this product
        // retrieved from the database.
        brandSelection.add("Liwayway");
        brandSelection.add("Mayumi");
        brandSelection.add("Dalisay");
        brandSelection.add("Mang Jose");
        brandSelection.add("Mama Rosa");

        return brandSelection;
    }

    /**
     * This retrieves the available packaging/serving sizes for this product
     * to be used as selection for AutoCompleteTextView size input.
     *
     * @return List of packaging sizes for this product.
     */
    List<String> suggestSizes() {
        List<String> sizeSelection = new ArrayList<>();

        if (this.mProduct.isEmpty()) return sizeSelection;

        // List is to be populated with available packaging sizes for
        // this product retrieved from database.
        sizeSelection.add("1kilo");
        sizeSelection.add("5kilo");
        sizeSelection.add("10Kilo");
        sizeSelection.add("25kilo");
        sizeSelection.add("50kilo");

        return sizeSelection;
    }

    /**
     * This retrieves the tags associated for this product.
     *
     * @return the tags of this product.
     */
    private List<String> retrieveTags() {
        List<String> tags = new ArrayList<>();

        if (this.mProduct.isEmpty()) return tags;

        // List is to be populated with tags for this product
        // retrieved from the database.
        tags.add("food");
        tags.add("grains");

        return tags;
    }

    boolean isReadyToLog() {
        return !mProduct.isEmpty() && !mBrand.isEmpty() && !mPrice.isZero() &&
                !mSize.isEmpty() && mQuantity != 0 && !mFund.isEmpty();
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
        dest.writeString(mSize);
        dest.writeDouble(mQuantity);
        dest.writeString(mFund);
        dest.writeStringList(mTags);
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
                "\nPrice per Item:\t\t" + getItemPrice().toString() +
                "\nPackaging Size:\t\t" + getSize() +
                "\nQuantity purchased:\t" + getQuantity() +
                "\nTotal Price:\t\t" + getTotalPrice().toString() +
                "\nDeduct From:\t\t" + getFund() +
                "\nTags:\t\t\t\t" + tags;
    }
}
