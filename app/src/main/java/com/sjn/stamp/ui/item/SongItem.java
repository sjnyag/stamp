package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongController;
import com.sjn.stamp.ui.activity.MediaBrowsable;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.SongStateHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.Utils;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SongItem extends AbstractItem<SongItem.SimpleViewHolder> implements IFilterable, Serializable {

    private static final String TAG = LogHelper.makeLogTag(SongItem.class);

    public String getMediaId() {
        return mMediaId;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }

    public boolean isPlayable() {
        return mIsPlayable;
    }

    public boolean isBrowsable() {
        return mIsBrowsable;
    }

    private String mMediaId;
    private String mTitle;
    private String mSubTitle;
    private String mAlbumArt;
    private boolean mIsPlayable;
    private boolean mIsBrowsable;
    private MediaBrowsable mMediaBrowsable;
    private Activity mActivity;

    public SongItem(MediaBrowserCompat.MediaItem mediaItem, MediaBrowsable mediaBrowsable, Activity activity) {
        super(mediaItem.getMediaId());
        setDraggable(true);
        setSwipeable(true);
        mMediaId = mediaItem.getMediaId();
        mTitle = mediaItem.getDescription().getTitle() == null ? "" : mediaItem.getDescription().getTitle().toString();
        mSubTitle = mediaItem.getDescription().getSubtitle() == null ? "" : mediaItem.getDescription().getSubtitle().toString();
        mAlbumArt = mediaItem.getDescription().getIconUri() == null ? "" : mediaItem.getDescription().getIconUri().toString();
        mIsPlayable = mediaItem.isPlayable();
        mIsBrowsable = mediaItem.isBrowsable();
        mMediaBrowsable = mediaBrowsable;
        mActivity = activity;
    }

    @Override
    public String getSubtitle() {
        if (mSubTitle == null) {
            return "";
        }
        return mSubTitle;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_song_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new SimpleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter, mActivity);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, final SimpleViewHolder holder, int position, List payloads) {
        final Context context = holder.itemView.getContext();
        if (adapter.hasSearchText()) {
            Utils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            Utils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(mTitle);
            holder.mSubtitle.setText(mSubTitle);
        }

        holder.mMediaId = mMediaId;
        ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, null, mTitle);
        if (mAlbumArt != null && !mAlbumArt.isEmpty()) {
            ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, mAlbumArt, mTitle);
        } else if (mMediaBrowsable != null) {
            mMediaBrowsable.search(mMediaId, null, new MediaBrowserCompat.SearchCallback() {
                @Override
                public void onSearchResult(@NonNull String query, Bundle extras, @NonNull List<MediaBrowserCompat.MediaItem> items) {
                    for (final MediaBrowserCompat.MediaItem metadata : items) {
                        if (metadata.getDescription().getIconUri() == null) {
                            continue;
                        }
                        if (query.equals(holder.mMediaId)) {
                            mAlbumArt = metadata.getDescription().getIconUri().toString();
                            ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, mAlbumArt, mTitle);
                        }
                        break;
                    }
                }

                @Override
                public void onError(@NonNull String query, Bundle extras) {
                    super.onError(query, extras);
                }
            });
        }
        holder.update(holder.mImageView, mMediaId, mIsPlayable);
        holder.updateStampList(mMediaId);
    }


    @Override
    public boolean filter(String constraint) {
        return mTitle != null && mTitle.toLowerCase().trim().contains(constraint) ||
                mSubTitle != null && mSubTitle.toLowerCase().trim().contains(constraint);
    }

    public static final class SimpleViewHolder extends LongClickDisableViewHolder {

        String mMediaId;
        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        Activity mActivity;
        View mFrontView;
        TextView mDate;
        ImageView mImageView;
        ViewGroup mStampListLayout;
        View.OnClickListener mOnNewStamp = new View.OnClickListener() {
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

        View.OnClickListener mOnRemoveStamp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mediaId = (String) v.getTag(R.id.text_view_remove_stamp_media_id);
                final String stampName = (String) v.getTag(R.id.text_view_remove_stamp_stamp_name);
                SongController songController = new SongController(mActivity);
                songController.removeStamp(stampName, mediaId, false);

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStampList(mediaId);
                    }
                });
            }
        };

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
                for (String stampName : songController.findStampsByMediaId(mediaId)) {
                    TextView textView = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.text_view_remove_stamp, null);
                    textView.setText(mTitle.getContext().getString(R.string.stamp_delete, stampName));
                    textView.setTag(R.id.text_view_remove_stamp_stamp_name, stampName);
                    textView.setTag(R.id.text_view_remove_stamp_media_id, mediaId);
                    textView.setOnClickListener(mOnRemoveStamp);
                    mStampListLayout.addView(textView);
                }
            }
        }

        private boolean isStampMedia(String mediaId) {
            return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
        }

        SimpleViewHolder(View view, FlexibleAdapter adapter, Activity activity) {
            super(view, adapter);
            this.mActivity = activity;
            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mAlbumArtView = (ImageView) view.findViewById(R.id.image);
            this.mImageView = (ImageView) view.findViewById(R.id.play_eq);
            this.mFrontView = view.findViewById(R.id.front_view);
            this.mDate = (TextView) view.findViewById(R.id.date);
            this.mStampListLayout = (ViewGroup) view.findViewById(R.id.stamp_info);
            this.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.mItemLongClickListener != null) {
                        mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
                    }
                }
            });
        }

        @Override
        public float getActivationElevation() {
            return ViewHelper.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        public View getFrontView() {
            return mFrontView;
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager ||
                    mAdapter.getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
                if (position % 2 != 0)
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            } else {
                //Linear layout
                if (mAdapter.isSelected(position))
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            }
        }

        public void update(View view, String mediaId, boolean isPlayable) {
            Integer cachedState = (Integer) view.getTag(R.id.tag_mediaitem_state_cache);
            int state = SongStateHelper.getMediaItemState(this.mActivity, mediaId, isPlayable);
            if (cachedState == null || cachedState != state) {
                Drawable drawable = SongStateHelper.getDrawableByState(this.mActivity, state);
                if (drawable != null) {
                    this.mImageView.setImageDrawable(drawable);
                    this.mImageView.setVisibility(View.VISIBLE);
                } else {
                    this.mImageView.setVisibility(View.GONE);
                }
                view.setTag(R.id.tag_mediaitem_state_cache, state);
            }
        }

        public TextView getShowTapTargetView() {
            return (TextView) mStampListLayout.getChildAt(0);
        }
    }

    @Override
    public String toString() {
        return "SongHistoryItem[" + super.toString() + "]";
    }

}