package com.sjn.stamp.ui.item;

import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;


class LongClickDisableViewHolder extends FlexibleViewHolder {

    LongClickDisableViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter);
    }

    LongClickDisableViewHolder(View view, FlexibleAdapter adapter, boolean stickyHeader) {
        super(view, adapter, stickyHeader);
    }

    @Override
    public boolean onLongClick(View view) {
        // hack to workaround bug in FlexibleAdapter - selectable = false
        FlexibleAdapter.OnItemLongClickListener oldL = mAdapter.mItemLongClickListener;
        mAdapter.mItemLongClickListener = null;
        super.onLongClick(view);
        mAdapter.mItemLongClickListener = oldL;
        // ignore
        return false;
    }
}
