package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.ui.activity.MediaBrowsable;
import com.sjn.stamp.utils.LogHelper;
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

    @SuppressWarnings(value = "unused")
    private static final String TAG = LogHelper.makeLogTag(SongItem.class);

    public String getMediaId() {
        return mMediaId;
    }

    @Override
    public String getTitle() {
        return mTitle;
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

    public SongItem(MediaMetadataCompat metadataCompat, Activity activity) {
        super(metadataCompat.getDescription().getMediaId());
        setDraggable(true);
        setSwipeable(true);
        mMediaId = metadataCompat.getDescription().getMediaId();
        mTitle = metadataCompat.getDescription().getTitle() == null ? "" : metadataCompat.getDescription().getTitle().toString();
        mSubTitle = metadataCompat.getDescription().getSubtitle() == null ? "" : metadataCompat.getDescription().getSubtitle().toString();
        mAlbumArt = metadataCompat.getDescription().getIconUri() == null ? "" : metadataCompat.getDescription().getIconUri().toString();
        mIsPlayable = false;
        mIsBrowsable = false;
        mMediaBrowsable = null;
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

    public static final class SimpleViewHolder extends StampContainsViewHolder {

        String mMediaId;
        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        View mFrontView;
        TextView mDate;
        ImageView mImageView;

        SimpleViewHolder(View view, FlexibleAdapter adapter, Activity activity) {
            super(view, adapter, activity);
            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mAlbumArtView = view.findViewById(R.id.image);
            this.mImageView = view.findViewById(R.id.play_eq);
            this.mFrontView = view.findViewById(R.id.front_view);
            this.mDate = view.findViewById(R.id.date);
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
    }

    @Override
    public String toString() {
        return "SongItem[" + super.toString() + "]";
    }

}