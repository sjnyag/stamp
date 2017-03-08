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
package com.sjn.taggingplayer.ui.holder;

import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongController;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.MediaIDHelper;


public class TaggingMediaItemViewHolder extends MediaItemViewHolder {

    private ViewGroup mTagListLayout;
    private View.OnClickListener mOnNewTag = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TagEditStateObserver tagEditStateObserver = TagEditStateObserver.getInstance();
            if (!tagEditStateObserver.isTagEditMode()) {
                return;
            }
            final String mediaId = (String) v.getTag(R.id.text_view_new_tag_media_id);
            SongController songController = new SongController(mActivity);
            songController.registerTagList(tagEditStateObserver.getSelectedTagList(), mediaId);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTagList(mediaId);
                }
            });
        }
    };

    private View.OnClickListener mOnRemoveTag = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String mediaId = (String) v.getTag(R.id.text_view_remove_tag_media_id);
            final String tagName = (String) v.getTag(R.id.text_view_remove_tag_tag_name);
            SongController songController = new SongController(mActivity);
            songController.removeTag(tagName, mediaId);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTagList(mediaId);
                }
            });
        }
    };
    private Activity mActivity;

    @Override
    protected View initializeConvertView(Activity activity, ViewGroup parent) {
        View convertView = LayoutInflater.from(activity)
                .inflate(R.layout.list_item_tagging_media, parent, false);
        this.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
        this.mTitleView = (TextView) convertView.findViewById(R.id.title);
        this.mAlbumView = (TextView) convertView.findViewById(R.id.album);
        this.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
        this.mAlbumArtView = (ImageView) convertView.findViewById(R.id.album_art);
        this.mTagListLayout = (ViewGroup) convertView.findViewById(R.id.tag_info);
        this.mActivity = activity;
        return convertView;
    }

    @Override
    protected void setUpView(Activity activity, MediaBrowserCompat.MediaItem item) {
        super.setUpView(activity, item);
        updateTagList(item.getMediaId());
    }

    private void updateTagList(String mediaId) {
        if (mTagListLayout != null && isTagMedia(mediaId)) {
            mTagListLayout.removeAllViews();
            SongController songController = new SongController(mActivity);
            for (String tagName : songController.findTagsByMediaId(mediaId)) {
                TextView text = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_remove_tag, null);
                text.setText("- " + tagName);
                text.setTag(R.id.text_view_remove_tag_tag_name, tagName);
                text.setTag(R.id.text_view_remove_tag_media_id, mediaId);
                text.setOnClickListener(mOnRemoveTag);
                mTagListLayout.addView(text);
            }
            TextView text = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_new_tag, null);
            text.setTag(R.id.text_view_new_tag_media_id, mediaId);
            text.setOnClickListener(mOnNewTag);
            mTagListLayout.addView(text);
        }
    }

    private boolean isTagMedia(String mediaId) {
        return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
    }
}