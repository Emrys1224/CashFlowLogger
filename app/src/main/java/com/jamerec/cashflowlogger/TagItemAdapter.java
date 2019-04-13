package com.jamerec.cashflowlogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class TagItemAdapter extends RecyclerView.Adapter<TagItemAdapter.TagItemHolder> {

    private final String TAG = getClass().getSimpleName();

    private List<String> mTags;
    private LayoutInflater mInflater;

    TagItemAdapter(Context context, List<String> tags) {
        this.mTags = tags;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TagItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View tagItemView = mInflater.inflate(R.layout.flex_item_tag, viewGroup, false);
        return new TagItemHolder(tagItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TagItemHolder tagItemHolder, int tagIndex) {
        final int tagPosition = tagItemHolder.getAdapterPosition();
        Button btnTagItem = tagItemHolder.mBtnTagItem;
        btnTagItem.setText(mTags.get(tagIndex));

        btnTagItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove this tag.
                mTags.remove(tagPosition);
                notifyItemRemoved(tagPosition);
                notifyItemRangeChanged(tagPosition, mTags.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    class TagItemHolder extends RecyclerView.ViewHolder {

        private Button mBtnTagItem;

        TagItemHolder(@NonNull View itemView) {
            super(itemView);

            mBtnTagItem = itemView.findViewById(R.id.flex_item);
        }
    }
}
