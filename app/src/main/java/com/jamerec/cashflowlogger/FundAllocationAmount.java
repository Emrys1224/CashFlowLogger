package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

/**
 * Data model used displaying fund allocation amount.
 */
public class FundAllocationAmount implements Parcelable {

    private final String mFundName;
    private PhCurrency mFundAmount;

    /**
     * Create a new FundListItem
     * @param fundName name of the fund
     * @param fundAmount amount allocated in the fund
     */
    FundAllocationAmount(String fundName, PhCurrency fundAmount) {
        this.mFundName = fundName;
        this.mFundAmount = new PhCurrency(fundAmount);
    }

    protected FundAllocationAmount(@NonNull Parcel in) {
        mFundName = in.readString();
        mFundAmount = in.readParcelable(PhCurrency.class.getClassLoader());
    }

    public static final Creator<FundAllocationAmount> CREATOR = new Creator<FundAllocationAmount>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public FundAllocationAmount createFromParcel(Parcel in) {
            return new FundAllocationAmount(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public FundAllocationAmount[] newArray(int size) {
            return new FundAllocationAmount[size];
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
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mFundName);
        dest.writeParcelable(mFundAmount, flags);
    }
}
