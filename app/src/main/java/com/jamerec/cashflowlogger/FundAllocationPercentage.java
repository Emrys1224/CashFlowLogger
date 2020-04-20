package com.jamerec.cashflowlogger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

/**
 * Data model used displaying fund allocation percentage.
 */
public class FundAllocationPercentage implements Parcelable {

    private final String mFundName;
    private int mPercentAllocation;

    /**
     * Create a new FundListItem
     *
     * @param fundName          name of the fund
     * @param percentAllocation percentage allocated for the fund.
     *                          If the given value exceeds 100(%) then the value will be set
     *                          to -1... because I don't want to deal with exceptions XD
     */
    FundAllocationPercentage(String fundName, int percentAllocation) {
        this.mFundName = fundName;
        updatePercentAllocation(percentAllocation);
    }

    protected FundAllocationPercentage(@NonNull Parcel in) {
        mFundName = in.readString();
        mPercentAllocation = in.readInt();
    }

    public static final Creator<FundAllocationPercentage> CREATOR = new Creator<FundAllocationPercentage>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public FundAllocationPercentage createFromParcel(Parcel in) {
            return new FundAllocationPercentage(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public FundAllocationPercentage[] newArray(int size) {
            return new FundAllocationPercentage[size];
        }
    };

    /**
     * Get the fund name
     *
     * @return name of the fund
     */
    String getFundName() {
        return this.mFundName;
    }

    /**
     * Get the percentage allocated for this fund
     *
     * @return amount in the fund
     */
    int getPercentAllocation() {
        return this.mPercentAllocation;
    }

    /**
     * Update the percent allocation for this fund
     * @param newAllocation new percent allocation for this fund.
     *                      If the given value exceeds 100(%) then the value will be set
     *                      to -1... because I don't want to deal with exceptions XD
     */
    void updatePercentAllocation(int newAllocation) {
        if (newAllocation > 100)
            this.mPercentAllocation = -1;
        else
            this.mPercentAllocation = newAllocation;
    }

    /**
     * Get the name and the percent allocation of the fund for debugging purposes.
     *
     * @return string showing the name and percent allocation in this fund item
     */
    @NonNull
    @Override
    public String toString() {
        return "Fund name: " + mFundName + "\t\tPercent Allocation: " + mPercentAllocation + "%";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mFundName);
        dest.writeInt(mPercentAllocation);
    }
}
