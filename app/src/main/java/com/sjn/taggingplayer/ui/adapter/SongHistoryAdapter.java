package com.sjn.taggingplayer.ui.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.ui.holder.SongHistoryItemViewHolder;
import com.sjn.taggingplayer.ui.holder.SongHistoryStickyViewHolder;
import com.sjn.taggingplayer.utils.LogHelper;
import com.sjn.taggingplayer.utils.TimeHelper;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class SongHistoryAdapter extends GenericAdapter<SongHistory> implements StickyListHeadersAdapter {
    private static final String TAG = LogHelper.makeLogTag(SongHistoryAdapter.class);

    public SongHistoryAdapter(Activity activity, List<SongHistory> songHistoryList) {
        super(activity, R.layout.list_item_song_history, songHistoryList);
    }

    @Override
    public View getDataRow(int position, View convertView, ViewGroup parent) {
        SongHistory item = getItem(position);
        return SongHistoryItemViewHolder.setupView((Activity) getContext(), convertView, parent, item);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        SongHistory item = getItem(position);
        return SongHistoryStickyViewHolder.setupView((Activity) getContext(), convertView, parent, item);
    }

    @Override
    public long getHeaderId(int position) {
        SongHistory item = getItem(position);
        if (item == null) {
            return 0;
        }
        return TimeHelper.toDateTime(item.getRecordedAt()).toLocalDate().toString().hashCode();
    }

}