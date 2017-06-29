package com.sjn.stamp.ui.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.db.RankedArtist;
import com.sjn.stamp.utils.ViewHelper;
import com.sjn.stamp.R;

import java.util.Locale;

public class RankedArtistItemViewHolder {

    private ImageView mAlbumArtView;
    private TextView mArtistView;
    private TextView mCountView;

    public static View setupView(final Activity activity, View convertView, ViewGroup parent, final RankedArtist rankedArtist, int position) {
        RankedArtistItemViewHolder holder;

        if (convertView == null) {
            convertView = createConvertView(activity, parent);
            holder = new RankedArtistItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (RankedArtistItemViewHolder) convertView.getTag();
        }
        holder.dynamicUpdate(activity, rankedArtist, position);

        return convertView;
    }

    private static View createConvertView(final Activity activity, ViewGroup parent) {
        return LayoutInflater.from(activity).inflate(R.layout.list_item_ranked_artist, parent, false);
    }

    private RankedArtistItemViewHolder(View convertView) {
        mAlbumArtView = (ImageView) convertView.findViewById(R.id.album_art);
        mArtistView = (TextView) convertView.findViewById(R.id.artist);
        mCountView = (TextView) convertView.findViewById(R.id.count);
    }

    private void dynamicUpdate(Activity activity, RankedArtist rankedArtist, int position) {
        mArtistView.setText(String.format(Locale.JAPANESE, "%d. %s", position + 1, rankedArtist.getArtist().getName()));
        mCountView.setText(String.format(Locale.JAPANESE, "%d", rankedArtist.getPlayCount()));
        ViewHelper.updateAlbumArt(activity, mAlbumArtView, rankedArtist.getArtist().getAlbumArtUri(), rankedArtist.getArtist().getName());
    }

}
