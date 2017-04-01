package com.sjn.taggingplayer.ui.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.db.SongHistory;
import com.sjn.taggingplayer.utils.TimeHelper;

public class SongHistoryStickyViewHolder {
    private TextView mTitleView;
    private View mConvertView;

    public static View setupView(final Activity activity, View convertView, ViewGroup parent, final SongHistory songHistory) {
        SongHistoryStickyViewHolder holder;

        if (convertView == null) {
            convertView = createConvertView(activity, parent);
            holder = new SongHistoryStickyViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SongHistoryStickyViewHolder) convertView.getTag();
        }
        holder.dynamicUpdate(songHistory);
        return convertView;
    }

    private static View createConvertView(final Activity activity, ViewGroup parent) {
        return LayoutInflater.from(activity).inflate(R.layout.list_sticky_song_history, parent, false);
    }

    private SongHistoryStickyViewHolder(View convertView) {
        mConvertView = convertView;
        mTitleView = (TextView) convertView.findViewById(R.id.title);
    }

    private void dynamicUpdate(final SongHistory songHistory) {
        if (songHistory == null) {
            ViewGroup.LayoutParams params = mConvertView.getLayoutParams();
            params.height = 1;
            mConvertView.setLayoutParams(params);
            return;
        }
        mTitleView.setText(TimeHelper.toDateTime(songHistory.getRecordedAt()).toLocalDate().toString());
    }

}
