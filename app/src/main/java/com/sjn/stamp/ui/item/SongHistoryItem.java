package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjn.stamp.R;
import com.sjn.stamp.controller.SongController;
import com.sjn.stamp.controller.SongHistoryController;
import com.sjn.stamp.db.SongHistory;
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.TimeHelper;
import com.sjn.stamp.utils.ViewHelper;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SongHistoryItem extends AbstractItem<SongHistoryItem.SimpleViewHolder>
        implements ISectionable<SongHistoryItem.SimpleViewHolder, DateHeaderItem>, IFilterable, Serializable {

    /* The header of this item */
    DateHeaderItem header;

    public String getMediaId() {
        return mMediaId;
    }

    String mMediaId;
    private String mArtistName;
    private Date mRecordedAt;
    private String mTitle;
    private String mAlbumArtUri;
    private String mLabel;
    private long mSongHistoryId;

    private SongHistoryItem(SongHistory songHistory, Resources resources) {
        super(String.valueOf(songHistory.getId()));
        setDraggable(true);
        setSwipeable(true);
        mSongHistoryId = songHistory.getId();
        mMediaId = songHistory.getSong().getMediaId();
        mArtistName = songHistory.getSong().getArtist().getName();
        mRecordedAt = songHistory.getRecordedAt();
        mTitle = songHistory.getSong().getTitle();
        mAlbumArtUri = songHistory.getSong().getAlbumArtUri();
        mLabel = songHistory.toLabel(resources);
    }

    public SongHistoryItem(SongHistory songHistory, DateHeaderItem header, Resources resources) {
        this(songHistory, resources);
        this.header = header;
    }

    @Override
    public void delete(Context context) {
        SongHistoryController controller = new SongHistoryController(context);
        controller.deleteSongHistory(mSongHistoryId);
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
    public SimpleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new SimpleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, SimpleViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();
        if (adapter.hasSearchText()) {
            Utils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            Utils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(getTitle());
            holder.mSubtitle.setText(getSubtitle());
        }
        holder.mDate.setText(getDateText(mRecordedAt, context.getResources()));
        ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, mAlbumArtUri, mTitle);
        holder.updateStampList(mMediaId);
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint) ||
                getSubtitle() != null && getSubtitle().toLowerCase().trim().contains(constraint);
    }

    private String getDateText(Date date, Resources resources) {
        DateTime dateTime = TimeHelper.toDateTime(date).minusSeconds(20);
        DateTime now = TimeHelper.getJapanNow();
        Minutes minutes = Minutes.minutesBetween(dateTime, now);
        if (minutes.isLessThan(Minutes.minutes(1))) {
            return resources.getString(R.string.item_song_history_seconds_ago, Seconds.secondsBetween(dateTime, now).getSeconds());
        } else if (minutes.isLessThan(Minutes.minutes(60))) {
            return resources.getString(R.string.item_song_history_minutes_ago, Minutes.minutesBetween(dateTime, now).getMinutes());
        } else {
            return TimeHelper.formatMMDDHHMM(dateTime);
        }
    }

    static final class SimpleViewHolder extends LongClickDisableViewHolder {

        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        Context mContext;
        View frontView;
        TextView mDate;
        ViewGroup mStampListLayout;
        View.OnClickListener mOnNewStamp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StampEditStateObserver stampEditStateObserver = StampEditStateObserver.getInstance();
                final String mediaId = (String) v.getTag(R.id.text_view_new_stamp_media_id);
                SongController songController = new SongController(mContext);
                songController.registerStampList(stampEditStateObserver.getSelectedStampList(), mediaId);
                ((Activity) mContext).runOnUiThread(new Runnable() {
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
                SongController songController = new SongController(mContext);
                songController.removeStamp(stampName, mediaId, false);

                if (mContext instanceof Activity) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateStampList(mediaId);
                        }
                    });
                }
            }
        };

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();
            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mAlbumArtView = (ImageView) view.findViewById(R.id.image);
            this.frontView = view.findViewById(R.id.front_view);
            this.mDate = (TextView) view.findViewById(R.id.date);
            this.mStampListLayout = (ViewGroup) view.findViewById(R.id.stamp_info);
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

        void updateStampList(String mediaId) {
            if (!StampEditStateObserver.getInstance().isStampMode()) {
                mStampListLayout.setVisibility(View.GONE);
                return;
            }
            mStampListLayout.setVisibility(View.VISIBLE);
            if (mStampListLayout != null) {
                mStampListLayout.removeAllViews();
                TextView addView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_new_stamp, null);
                addView.setTag(R.id.text_view_new_stamp_media_id, mediaId);
                addView.setOnClickListener(mOnNewStamp);
                mStampListLayout.addView(addView);
                SongController songController = new SongController(mContext);
                for (String stampName : songController.findStampsByMusicId(mediaId)) {
                    TextView textView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_remove_stamp, null);
                    textView.setText(mTitle.getContext().getString(R.string.stamp_delete, stampName));
                    textView.setTag(R.id.text_view_remove_stamp_stamp_name, stampName);
                    textView.setTag(R.id.text_view_remove_stamp_media_id, mediaId);
                    textView.setOnClickListener(mOnRemoveStamp);
                    mStampListLayout.addView(textView);
                }
            }
        }
    }

    @Override
    public String toString() {
        return mLabel;
    }

}