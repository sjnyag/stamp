package com.sjn.stamp.ui.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.db.RankedSong;
import com.sjn.stamp.utils.ViewHelper;

import java.util.Locale;

public class RankedSongItemViewHolder {

    private ImageView mAlbumArtView;
    private TextView mTitleView;
    private TextView mArtistView;
    private TextView mCountView;

    public static View setupView(final Activity activity, View convertView, ViewGroup parent, final RankedSong rankedSong, int position) {
        RankedSongItemViewHolder holder;

        if (convertView == null) {
            convertView = createConvertView(activity, parent);
            holder = new RankedSongItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (RankedSongItemViewHolder) convertView.getTag();
        }
        holder.dynamicUpdate(activity, rankedSong, position);

        return convertView;
    }

    private static View createConvertView(final Activity activity, ViewGroup parent) {
        return LayoutInflater.from(activity).inflate(R.layout.list_item_ranked_song, parent, false);
    }

    private RankedSongItemViewHolder(View convertView) {
        mAlbumArtView = (ImageView) convertView.findViewById(R.id.album_art);
        mTitleView = (TextView) convertView.findViewById(R.id.title);
        mArtistView = (TextView) convertView.findViewById(R.id.artist);
        mCountView = (TextView) convertView.findViewById(R.id.count);
    }

    private void dynamicUpdate(Activity activity, final RankedSong rankedSong, int position) {
        mTitleView.setText(String.format(Locale.JAPANESE, "%d. %s", position + 1, rankedSong.getSong().getTitle()));
        mArtistView.setText(rankedSong.getSong().getArtist());
        mCountView.setText(String.format(Locale.JAPANESE, "%d", rankedSong.getPlayCount()));
        ViewHelper.updateAlbumArt(activity, mAlbumArtView, rankedSong.getSong().getAlbumArtUri(), rankedSong.getSong().getTitle());
    }

}
