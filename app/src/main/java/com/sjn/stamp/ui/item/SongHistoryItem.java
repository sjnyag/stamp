package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.model.SongHistory;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.TimeHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SongHistoryItem extends AbstractItem<SongHistoryItem.SimpleViewHolder>
        implements ISectionable<SongHistoryItem.SimpleViewHolder, DateHeaderItem>, IFilterable, Serializable {

    /* The header of this item */
    private DateHeaderItem header;

    public String getMediaId() {
        return mMediaId;
    }

    private String mMediaId;
    private String mArtistName;
    private Date mRecordedAt;
    private String mTitle;
    private String mAlbumArtUri;
    private String mLabel;
    private long mSongHistoryId;
    private Activity mActivity;

    private SongHistoryItem(SongHistory songHistory, Resources resources) {
        super(String.valueOf(songHistory.getId()));
        setDraggable(true);
        setSwipeable(true);
        mSongHistoryId = songHistory.getId();
        mMediaId = MediaIDHelper.createMediaID(songHistory.getSong().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_TIMELINE);
        mArtistName = songHistory.getSong().getArtist().getName();
        mRecordedAt = songHistory.getRecordedAt();
        mTitle = songHistory.getSong().getTitle();
        mAlbumArtUri = songHistory.getSong().getAlbumArtUri();
        mLabel = songHistory.toLabel(resources);
    }

    public SongHistoryItem(SongHistory songHistory, DateHeaderItem header, Resources resources, Activity activity) {
        this(songHistory, resources);
        this.header = header;
        this.mActivity = activity;
    }

    @Override
    public void delete(Context context) {
        if (context != null) {
            SongHistoryController controller = new SongHistoryController(context);
            controller.delete(mSongHistoryId);
        }
    }

    @Override
    public String getSubtitle() {
        return mArtistName;
    }

    @Override
    public DateHeaderItem getHeader() {
        return header;
    }

    @Override
    public void setHeader(DateHeaderItem header) {
        this.header = header;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_song_history_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new SimpleViewHolder(view, adapter, mActivity);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, SimpleViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();
        if (adapter.hasSearchText()) {
            FlexibleUtils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            FlexibleUtils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(getTitle());
            holder.mSubtitle.setText(getSubtitle());
        }
        holder.mDate.setText(TimeHelper.getDateText(mRecordedAt, context.getResources()));
        ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, mAlbumArtUri, mTitle);
        holder.updateStampList(mMediaId);
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint) ||
                getSubtitle() != null && getSubtitle().toLowerCase().trim().contains(constraint);
    }

    static final class SimpleViewHolder extends StampContainsViewHolder {

        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        View frontView;
        TextView mDate;


        SimpleViewHolder(View view, FlexibleAdapter adapter, Activity activity) {
            super(view, adapter, activity);
            this.mTitle = view.findViewById(R.id.title);
            this.mSubtitle = view.findViewById(R.id.subtitle);
            this.mAlbumArtView = view.findViewById(R.id.image);
            this.frontView = view.findViewById(R.id.front_view);
            this.mDate = view.findViewById(R.id.date);
        }

        @Override
        public float getActivationElevation() {
            return ViewHelper.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        protected boolean shouldActivateViewWhileSwiping() {
            return false;//default=false
        }

        @Override
        protected boolean shouldAddSelectionInActionMode() {
            return false;//default=false
        }

        @Override
        public View getFrontView() {
            return frontView;
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

        @Override
        protected boolean isStampMedia(String mediaId) {
            return true;
        }
    }

    @Override
    public String toString() {
        return mLabel;
    }

}