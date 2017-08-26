package com.sjn.stamp.ui.item;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongController;
import com.sjn.stamp.db.Stamp;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.MediaIDHelper;

import eu.davidea.flexibleadapter.FlexibleAdapter;

class StampContainsViewHolder extends LongClickDisableViewHolder {

    Activity mActivity;
    private ViewGroup mStampListLayout;

    StampContainsViewHolder(View view, FlexibleAdapter adapter, Activity activity) {
        super(view, adapter);
        this.mActivity = activity;
        this.mStampListLayout = view.findViewById(R.id.stamp_info);
    }

    public TextView getShowTapTargetView() {
        return (TextView) mStampListLayout.getChildAt(0);
    }

    void updateStampList(String mediaId) {
        if (!StampEditStateObserver.getInstance().isStampMode()) {
            mStampListLayout.setVisibility(View.GONE);
            return;
        }
        mStampListLayout.setVisibility(View.VISIBLE);
        if (mStampListLayout != null && isStampMedia(mediaId)) {
            mStampListLayout.removeAllViews();
            TextView addView = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_new_stamp, null);
            addView.setTag(R.id.text_view_new_stamp_media_id, mediaId);
            addView.setOnClickListener(mOnNewStamp);
            mStampListLayout.addView(addView);
            SongController songController = new SongController(mActivity);
            for (Stamp stamp : songController.findStampsByMediaId(mediaId)) {
                int stampResource = stamp.isSystem() ? R.layout.text_view_remove_smart_stamp : R.layout.text_view_remove_stamp;
                TextView textView = (TextView) LayoutInflater.from(mActivity).inflate(stampResource, null);
                textView.setText(mActivity.getString(R.string.stamp_delete, stamp.getName()));
                textView.setTag(R.id.text_view_remove_stamp_stamp_name, stamp.getName());
                textView.setTag(R.id.text_view_remove_stamp_media_id, mediaId);
                textView.setTag(R.id.text_view_remove_stamp_is_system, stamp.isSystem());
                textView.setOnClickListener(mOnRemoveStamp);
                mStampListLayout.addView(textView);
            }
        }
    }

    protected boolean isStampMedia(String mediaId) {
        return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
    }

    private View.OnClickListener mOnNewStamp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StampEditStateObserver stampEditStateObserver = StampEditStateObserver.getInstance();
            final String mediaId = (String) v.getTag(R.id.text_view_new_stamp_media_id);
            SongController songController = new SongController(mActivity);
            songController.registerStampList(stampEditStateObserver.getSelectedStampList(), mediaId, false);
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
            final boolean isSystem = (Boolean) v.getTag(R.id.text_view_remove_stamp_is_system);
            SongController songController = new SongController(mActivity);
            songController.removeStamp(stampName, mediaId, isSystem);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStampList(mediaId);
                }
            });
        }
    };
}
