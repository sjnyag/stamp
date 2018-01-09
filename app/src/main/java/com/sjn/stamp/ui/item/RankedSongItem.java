package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

public class RankedSongItem extends AbstractItem<RankedSongItem.SimpleViewHolder> implements IFilterable, Serializable {

    private int mOrder;
    private int mPlayCount;
    private MediaMetadataCompat mTrack;

    public RankedSongItem(MediaMetadataCompat track, int playCount, int order) {
        super(track.getDescription().getMediaId());
        setDraggable(true);
        setSwipeable(true);
        mTrack = track;
        mOrder = order;
        mPlayCount = playCount;
    }

    public MediaMetadataCompat getTrack() {
        return mTrack;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_ranked_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new SimpleViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, SimpleViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

        String title = mTrack.getDescription().getTitle() != null ? mTrack.getDescription().getTitle().toString() : null;
        String artist = mTrack.getDescription().getSubtitle() != null ? mTrack.getDescription().getSubtitle().toString() : null;
        String artUrl = mTrack.getDescription().getIconUri() != null ? mTrack.getDescription().getIconUri().toString() : null;
        // In case of searchText matches with Title or with a field this will be highlighted
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            FlexibleUtils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(title);
            holder.mSubtitle.setText(artist);
            holder.mCountView.setText(String.valueOf(mPlayCount));
            holder.mOrderView.setText(String.valueOf(mOrder));
        }
        if (artUrl != null) {
            ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, artUrl, title);
        }
    }

    @Override
    public boolean filter(String constraint) {
        return mTrack != null && mTrack.getDescription().getTitle() != null && mTrack.getDescription().getTitle().toString().toLowerCase().trim().contains(constraint) ||
                mTrack != null && mTrack.getDescription().getSubtitle() != null && mTrack.getDescription().getSubtitle().toString().toLowerCase().trim().contains(constraint);
    }

    public String getMediaId() {
        return mTrack.getDescription().getMediaId();
    }

    static final class SimpleViewHolder extends LongClickDisableViewHolder {

        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        Context mContext;
        View mFrontView;
        TextView mCountView;
        TextView mOrderView;

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();

            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mAlbumArtView = view.findViewById(R.id.image);
            this.mAlbumArtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.mItemLongClickListener != null) {
                        mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
                        toggleActivation();
                    }
                }
            });

            this.mFrontView = view.findViewById(R.id.front_view);
            this.mCountView = view.findViewById(R.id.count);
            this.mOrderView = view.findViewById(R.id.order);
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
    }

    @Override
    public String toString() {
        return "SongHistoryItem[" + super.toString() + "]";
    }

}