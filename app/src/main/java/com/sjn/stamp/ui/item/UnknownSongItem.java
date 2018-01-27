package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.model.Song;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.TimeHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

public class UnknownSongItem extends AbstractItem<UnknownSongItem.SimpleViewHolder> implements IFilterable, Serializable {

    @SuppressWarnings(value = "unused")
    private static final String TAG = LogHelper.INSTANCE.makeLogTag(UnknownSongItem.class);

    @Override
    public String getTitle() {
        return mTitle;
    }

    private final int mPlayCount;
    private final Long mSongId;
    private final Date mLastPlayed;
    private final String mMediaId;
    private final String mTitle;
    private final String mSubTitle;
    private final String mAlbumArt;

    public UnknownSongItem(String id, Song song) {
        super(id);
        setDraggable(true);
        setSwipeable(true);
        mSongId = song.getId();
        mMediaId = song.getMediaId();
        mTitle = song.getTitle();
        mSubTitle = song.getAlbum();
        mAlbumArt = song.getAlbumArtUri();
        mPlayCount = song.getTotalSongHistory().getPlayCount();
        mLastPlayed = song.getSongHistoryList() != null && !song.getSongHistoryList().isEmpty() ? song.getSongHistoryList().last().getRecordedAt() : null;
    }

    public Long getSongId() {
        return mSongId;
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
        return R.layout.recycler_unknown_song_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new SimpleViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, final SimpleViewHolder holder, int position, List payloads) {
        final Context context = holder.itemView.getContext();
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            FlexibleUtils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(mTitle);
            holder.mSubtitle.setText(mSubTitle);
            holder.mDate.setText(TimeHelper.INSTANCE.getDateText(mLastPlayed, context.getResources()));
        }

        holder.mMediaId = mMediaId;
        ViewHelper.INSTANCE.updateAlbumArt((Activity) context, holder.mAlbumArtView, null, mTitle);
        if (mAlbumArt != null && !mAlbumArt.isEmpty()) {
            ViewHelper.INSTANCE.updateAlbumArt((Activity) context, holder.mAlbumArtView, mAlbumArt, mTitle);
        }
    }


    @Override
    public boolean filter(String constraint) {
        return mTitle != null && mTitle.toLowerCase().trim().contains(constraint) ||
                mSubTitle != null && mSubTitle.toLowerCase().trim().contains(constraint);
    }

    static final class SimpleViewHolder extends LongClickDisableViewHolder {

        String mMediaId;
        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        View mFrontView;
        TextView mDate;

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mAlbumArtView = view.findViewById(R.id.image);
            this.mFrontView = view.findViewById(R.id.front_view);
            this.mDate = view.findViewById(R.id.date);
        }

        @Override
        public float getActivationElevation() {
            return ViewHelper.INSTANCE.dpToPx(itemView.getContext(), 4f);
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
    }

    @Override
    public String toString() {
        return "SongHistoryItem[" + super.toString() + "]";
    }

}