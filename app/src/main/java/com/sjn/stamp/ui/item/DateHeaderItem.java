package com.sjn.stamp.ui.item;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.TimeHelper;

import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This is a header item with custom layout for section headers.
 * <p><b>Note:</b> THIS ITEM IS NOT A SCROLLABLE HEADER.</p>
 * A Section should not contain others Sections and headers are not Sectionable!
 */
@Getter
@Accessors(prefix = "m")
public class DateHeaderItem extends AbstractHeaderItem<DateHeaderItem.HeaderViewHolder> implements IFilterable {

    private Date mDate;
    private String mTitle;

    public DateHeaderItem(Date date) {
        super();
        mDate = TimeHelper.toDateOnly(date);
        mTitle = TimeHelper.toDateTime(date).toLocalDate().toString();
        setDraggable(false);
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof DateHeaderItem) {
            DateHeaderItem inItem = (DateHeaderItem) inObject;
            return this.getDate().equals(inItem.getDate());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mDate.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_header_item;
    }

    @Override
    public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() <= 0) {
            holder.mTitle.setText(getTitle());
        }
        List sectionableList = adapter.getSectionItems(this);
        String subTitle = (sectionableList.isEmpty() ? "Empty section" :
                sectionableList.size() + " section items");
        holder.mSubtitle.setText(subTitle);
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
    }

    public boolean isDateOf(Date recordedAt) {
        return TimeHelper.toDateOnly(recordedAt).compareTo(mDate) == 0;
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        TextView mTitle;
        TextView mSubtitle;

        HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = (TextView) view.findViewById(R.id.title);
            mSubtitle = (TextView) view.findViewById(R.id.subtitle);
        }
    }

}