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

    private String mProduct;        // Product name
    private String mBrand;          // Brand name
    private PhCurrency mPrice;      // Price per item
    private Measures mSize;         // Packaging/serving size of the product
    private Measures mQuantity;     // Quantity of item purchased
    private String mFund;           // Fund from where it is to be deducted
    private List<String> mTags;     // Tags where this product is categorized

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
    }

    protected ExpenseItem(Parcel in) {
        mProduct = in.readString();
        mBrand = in.readString();
        mPrice = in.readParcelable(PhCurrency.class.getClassLoader());
        mSize = in.readParcelable(Measures.class.getClassLoader());
        mQuantity = in.readParcelable(Measures.class.getClassLoader());
        mFund = in.readString();
        mTags = in.createStringArrayList();
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Getters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //

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
     * @param size of the product e.g. 1kilo, 1order, 2liters, 1pack.
     * @throws Measures.InvalidValueException    if no numeric value found.
     * @throws Measures.NoValidUnitException     if no valid unit of measure found.
     * @throws Measures.ZeroDenominatorException if a fractional value with zero as denominator is the given string.
     */
    void setSize(String size) throws
            Measures.InvalidValueException,
            Measures.NoValidUnitException,
            Measures.ZeroDenominatorException {

        this.mSize.setValue(size);
    }

    /**
     * Set the quantity of product purchased.
     *
     * @param quantity of the product purchased.
     */
    void setQuantity(String quantity) {
        try {
            this.mQuantity.setValue(quantity + "orders");
        }

        // Since this is to be set with a number EditText so the chance of triggering
        // these of exception is low unless I forgot about it and used it somewhere else....
        catch (Measures.InvalidValueException ive) {
            System.out.println(ive.getMessage());
            ive.printStackTrace();

        } catch (Measures.NoValidUnitException iue) {
            System.out.println(iue.getMessage());
            iue.printStackTrace();

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
     * This is used to update the mTags for this ExpenseItem whenever the
     * mProduct is changed.
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
