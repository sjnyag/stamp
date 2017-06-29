package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjn.stamp.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Scrollable Header and Footer Item that can be expanded too. When visible, all the subItems
 * will be Headers or Footers as well, depending where the parent has been initially added!
 */
public class ScrollableExpandableItem extends AbstractItem<ScrollableExpandableItem.ScrollableExpandableViewHolder>
        implements IExpandable<ScrollableExpandableItem.ScrollableExpandableViewHolder, ScrollableSubItem> {

    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<ScrollableSubItem> mSubItems;


    public ScrollableExpandableItem(String id) {
        super(id);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_scrollable_expandable_item;
    }

    @Override
    public ScrollableExpandableViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ScrollableExpandableViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ScrollableExpandableViewHolder holder, int position, List payloads) {
        holder.mTitle.setSelected(true);//For marquee!!
        holder.mTitle.setText(getTitle());
        holder.mSubtitle.setText(getSubtitle());

        //Support for StaggeredGridLayoutManager
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
            Log.d("ScrollableExpandable", "ScrollableExpandableItem configured fullSpan for StaggeredGridLayout");
        }
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<ScrollableSubItem> getSubItems() {
        return mSubItems;
    }

    public void addSubItem(ScrollableSubItem subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<>();
        mSubItems.add(subItem);
    }

    static class ScrollableExpandableViewHolder extends ExpandableViewHolder {

        public TextView mTitle;
        public TextView mSubtitle;

        ScrollableExpandableViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);
            mTitle = (TextView) view.findViewById(R.id.title);
            mSubtitle = (TextView) view.findViewById(R.id.subtitle);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
        }
    }

    @Override
    public String toString() {
        return "ScrollableExpandableItem[" + super.toString() + "]";
    }
}