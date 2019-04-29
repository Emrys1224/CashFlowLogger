package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TagItemAdapter extends RecyclerView.Adapter<TagItemAdapter.TagItemHolder> {

    private final String TAG = getClass().getSimpleName();

    // Tag item layout option.
    static final boolean DISPLAY_ONLY = false;
    static final boolean BUTTON_TAG = true;

    private ExpenseItem mExpenseItem;
    private LayoutInflater mInflater;
    private boolean mTagStyle;

    TagItemAdapter(Context context, ExpenseItem expenseItem, boolean tagStyle) {
        this.mExpenseItem = expenseItem;
        this.mInflater = LayoutInflater.from(context);
        this.mTagStyle = tagStyle;
    }

    @NonNull
    @Override
    public TagItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View tagItemView = mInflater.inflate(R.layout.flex_item_tag, viewGroup, false);
        return new TagItemHolder(tagItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TagItemHolder tagItemHolder, int tagIndex) {
        tagItemHolder.setTagName(
                mExpenseItem.getTags().get(tagIndex));

        // Add ClickListener only if the item layout is BUTTON_TAG.
        if (mTagStyle == BUTTON_TAG) {
            Button btnTag = tagItemHolder.mBtnTagItem;
            final int tagPosition = tagItemHolder.getAdapterPosition();
            final String tag = tagItemHolder.mTag;

            btnTag.setEnabled(true);
            btnTag.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_remove, 0);   // Remove icon

            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove this tag.
                    mExpenseItem.removeTag(tag);
                    notifyItemRemoved(tagPosition);
                    notifyItemRangeChanged(tagPosition, getItemCount());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mExpenseItem.getTags().size();
    }

    class TagItemHolder extends RecyclerView.ViewHolder {

        private Button mBtnTagItem;
        private String mTag;

        TagItemHolder(@NonNull View itemView) {
            super(itemView);

            mBtnTagItem = itemView.findViewById(R.id.flex_item);
        }

        private void setTagName(String tag) {
            this.mTag = tag;
            mBtnTagItem.setText(tag);
        }
    }
}
