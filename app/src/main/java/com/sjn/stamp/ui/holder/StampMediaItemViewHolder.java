/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sjn.stamp.ui.holder;

import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.controller.SongController;
import com.sjn.stamp.media.provider.ListProvider;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.ViewHelper;
import com.sjn.stamp.R;


public class StampMediaItemViewHolder extends MediaItemViewHolder {

    private ViewGroup mStampListLayout;
    private View.OnClickListener mOnNewStamp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StampEditStateObserver stampEditStateObserver = StampEditStateObserver.getInstance();
            final String mediaId = (String) v.getTag(R.id.text_view_new_stamp_media_id);
            SongController songController = new SongController(mActivity);
            songController.registerStampList(stampEditStateObserver.getSelectedStampList(), mediaId);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStampList(mediaId);
                }
            });
        }
    };

    private View.OnClickListener mOnRemoveStamp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String mediaId = (String) v.getTag(R.id.text_view_remove_stamp_media_id);
            final String stampName = (String) v.getTag(R.id.text_view_remove_stamp_stamp_name);
            SongController songController = new SongController(mActivity);
            songController.removeStamp(stampName, mediaId);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStampList(mediaId);
                }
            });
        }
    };
    private Activity mActivity;

    @Override
    protected View initializeConvertView(Activity activity, ViewGroup parent) {
        View convertView = LayoutInflater.from(activity)
                .inflate(R.layout.list_item_stamp_media, parent, false);
        this.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
        this.mTitleView = (TextView) convertView.findViewById(R.id.title);
        this.mAlbumView = (TextView) convertView.findViewById(R.id.album);
        this.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
        this.mAlbumArtView = (ImageView) convertView.findViewById(R.id.album_art);
        this.mStampListLayout = (ViewGroup) convertView.findViewById(R.id.stamp_info);
        this.mActivity = activity;
        return convertView;
    }

    @Override
    protected void setUpView(Activity activity, MediaBrowserCompat.MediaItem item) {
        MediaDescriptionCompat description = item.getDescription();
        if (description.getExtras() != null && description.getExtras().containsKey(ListProvider.CUSTOM_METADATA_TRACK_PREFIX)) {
            this.mTitleView.setText(String.valueOf(description.getExtras().getLong(ListProvider.CUSTOM_METADATA_TRACK_PREFIX)) + "回: " + description.getTitle());
        } else {
            this.mTitleView.setText(description.getTitle());
        }
        if (description.getDescription() == null || description.getDescription().length() == 0) {
            this.mAlbumView.setVisibility(View.GONE);
        } else {
            this.mAlbumView.setText(description.getDescription());
        }
        if (description.getSubtitle() == null || description.getSubtitle().length() == 0) {
            this.mDescriptionView.setVisibility(View.GONE);
        } else {
            this.mDescriptionView.setText(description.getSubtitle());
        }

        if (description.getIconUri() != null) {
            ViewHelper.updateAlbumArt(activity, this.mAlbumArtView, description.getIconUri().toString(), description.getTitle().toString());
        }
        updateStampList(item.getMediaId());
    }

    private void updateStampList(String mediaId) {
        if (mStampListLayout != null && isStampMedia(mediaId)) {
            mStampListLayout.removeAllViews();
            SongController songController = new SongController(mActivity);
            for (String stampName : songController.findStampsByMediaId(mediaId)) {
                TextView text = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_remove_stamp, null);
                text.setText("- " + stampName);
                text.setTag(R.id.text_view_remove_stamp_stamp_name, stampName);
                text.setTag(R.id.text_view_remove_stamp_media_id, mediaId);
                text.setOnClickListener(mOnRemoveStamp);
                mStampListLayout.addView(text);
            }
            TextView text = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_new_stamp, null);
            text.setTag(R.id.text_view_new_stamp_media_id, mediaId);
            text.setOnClickListener(mOnNewStamp);
            mStampListLayout.addView(text);
        }
    }

    private boolean isStampMedia(String mediaId) {
        return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
    }
}