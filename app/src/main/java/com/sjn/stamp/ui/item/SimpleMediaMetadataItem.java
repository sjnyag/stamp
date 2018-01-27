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
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaItemHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

public class SimpleMediaMetadataItem extends AbstractItem<SimpleMediaMetadataItem.SimpleViewHolder> implements IFilterable, Serializable {

    @SuppressWarnings(value = "unused")
    private static final String TAG = LogHelper.INSTANCE.makeLogTag(SimpleMediaMetadataItem.class);

    @Override
    public String getTitle() {
        return mTitle;
    }

    private final String mMediaId;
    private final String mTitle;
    private final String mSubTitle;
    private final String mAlbumArt;

    public SimpleMediaMetadataItem(MediaMetadataCompat metadata) {
        super(metadata.getDescription().getMediaId());
        setDraggable(true);
        setSwipeable(true);
        mMediaId = metadata.getDescription().getMediaId();
        mTitle = MediaItemHelper.INSTANCE.getTitle(metadata);
        mSubTitle = MediaItemHelper.INSTANCE.getArtist(metadata);
        mAlbumArt = MediaItemHelper.INSTANCE.getAlbumArtUri(metadata);
    }

    public String getMediaId() {
        return mMediaId;
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
        return R.layout.recycler_simple_media_metadata_item;
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

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mAlbumArtView = view.findViewById(R.id.image);
            this.mFrontView = view.findViewById(R.id.front_view);
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