package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Data model used displaying fund allocation list.
 */
public class FundItem implements Parcelable {

    private final String mFundName;
    private PhCurrency mFundAmount;

    /**
     * Create a new FundListItem
     * @param fundName name of the fund
     * @param fundAmount amount allocated in the fund
     */
    FundItem(String fundName, PhCurrency fundAmount) {
        this.mFundName = fundName;
        this.mFundAmount = new PhCurrency(fundAmount);
    }

    protected FundItem(Parcel in) {
        mFundName = in.readString();
        mFundAmount = in.readParcelable(PhCurrency.class.getClassLoader());
    }

    public static final Creator<FundItem> CREATOR = new Creator<FundItem>() {
        @Override
        public FundItem createFromParcel(Parcel in) {
            return new FundItem(in);
        }

        @Override
        public FundItem[] newArray(int size) {
            return new FundItem[size];
        }
    };

    /**
     * Get the fund name
     * @return name of the fund
     */
    public String getName() {
        return mFundName;
    }

    /**
     * Get the amount allocated in this fund
     * @return amount in the fund
     */
    public PhCurrency getAmount() {
        return mFundAmount;
    }

    /**
     * Update the amount of the fund
     * @param fundAmount new amount in the fund
     */
    public void updateAmount(PhCurrency fundAmount) {
        this.mFundAmount.setValue(fundAmount);
    }

    /**
     * Reset the amount of the fund to ₱0.00 .
     */
    public void resetAmount() {
        this.mFundAmount.setValue(0);
    }

    /**
     * Get the name and the amount of the fund for debugging purposes.
     * @return string showing the name and amount in this fund item
     */
    @NonNull
    @Override
    public String toString() {
        return "Fund name: " + mFundName + "\t\tAmount: " + mFundAmount.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFundName);
        dest.writeParcelable(mFundAmount, flags);
    }
}
