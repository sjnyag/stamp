package com.sjn.stamp.ui.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.ui.custom.LabelCardView;
import com.sjn.stamp.utils.ViewHelper;
import com.sjn.stamp.R;
import com.sjn.stamp.utils.TimeHelper;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.util.Date;
import java.util.Locale;

public class SongHistoryItemViewHolder {

    private ImageView mAlbumArtView;
    private TextView mTitleView;
    private TextView mArtistView;
    private TextView mDateView;
    private TextView mCountView;
    private LabelCardView mConvertView;

    public static View setupView(final Activity activity, View convertView, ViewGroup parent, final SongHistory songHistory) {
        SongHistoryItemViewHolder holder;

        if (convertView == null) {
            convertView = createConvertView(activity, parent);
            holder = new SongHistoryItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SongHistoryItemViewHolder) convertView.getTag();
        }
        holder.dynamicUpdate(activity, songHistory);

        return convertView;
    }

    private static View createConvertView(final Activity activity, ViewGroup parent) {
        return LayoutInflater.from(activity).inflate(R.layout.list_item_song_history, parent, false);
    }

    private SongHistoryItemViewHolder(View convertView) {
        mConvertView = (LabelCardView) convertView;
        mAlbumArtView = (ImageView) convertView.findViewById(R.id.album_art);
        mTitleView = (TextView) convertView.findViewById(R.id.title);
        mArtistView = (TextView) convertView.findViewById(R.id.artist);
        mDateView = (TextView) convertView.findViewById(R.id.date);
        mCountView = (TextView) convertView.findViewById(R.id.count);
    }

    private void dynamicUpdate(Activity activity, SongHistory songHistory) {
        if (songHistory == null) {
            return;
        }
        mTitleView.setText(songHistory.getSong().getTitle());
        mArtistView.setText(songHistory.getSong().getArtist());
        mDateView.setText(getDateText(songHistory.getRecordedAt()));
        mCountView.setText(String.format(Locale.JAPANESE, "%d", songHistory.getCount()));
        mConvertView.setLabelText(String.format(Locale.JAPANESE, "%d", songHistory.getCount()));
        ViewHelper.updateAlbumArt(activity, mAlbumArtView, songHistory.getSong().getAlbumArtUri(), songHistory.getSong().getTitle());
        //ViewHelper.setDrawableLayerColor(activity, mConvertView, songHistory.getColor(), R.drawable.big_card, R.id.card_white);
    }

    private String getDateText(Date date) {
        DateTime dateTime = TimeHelper.toDateTime(date).minusSeconds(20);
        DateTime now = TimeHelper.getJapanNow();
        Minutes minutes = Minutes.minutesBetween(dateTime, now);
        if (minutes.isLessThan(Minutes.minutes(1))) {
            return String.format(Locale.JAPANESE, "%d 秒前", Seconds.secondsBetween(dateTime, now).getSeconds());
        } else if (minutes.isLessThan(Minutes.minutes(60))) {
            return String.format(Locale.JAPANESE, "%d 分前", Minutes.minutesBetween(dateTime, now).getMinutes());
        } else {
            return dateTime.toString("MM/dd HH:mm", Locale.JAPAN);
        }
    }

}
