package com.sjn.stamp.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
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
import com.sjn.stamp.ui.observer.StampEditStateObserver;
import com.sjn.stamp.utils.MediaIDHelper;
import com.sjn.stamp.utils.ViewHelper;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(prefix = "m")
public class RankedArtistItem extends AbstractItem<RankedArtistItem.SimpleViewHolder> implements IFilterable, Serializable {

    private int mOrder;
    private int mPlayCount;
    private String mMostPlayedSongTitle;
    private String mArtistName;
    private String mArtUrl;

    public RankedArtistItem(String mostPlayedSongTitle, String artistName, String artUrl, int playCount, int order) {
        super(artistName);
        setDraggable(true);
        setSwipeable(true);
        mMostPlayedSongTitle = mostPlayedSongTitle;
        mArtistName = artistName;
        mArtUrl = artUrl;
        mOrder = order;
        mPlayCount = playCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_ranked_item;
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
            holder.mTitle.setText(mArtistName);
            holder.mSubtitle.setText(String.format("最も聴いた曲 %s", mMostPlayedSongTitle));
            holder.mCountView.setText(String.valueOf(mPlayCount));
            holder.mOrderView.setText(String.valueOf(mOrder));
        }
        if (mArtUrl != null) {
            ViewHelper.updateAlbumArt((Activity) context, holder.mAlbumArtView, mArtUrl, mArtistName);
        }
        holder.updateStampList(mArtistName);
    }

    @Override
    public boolean filter(String constraint) {
        return mArtistName.toLowerCase().trim().contains(constraint);
    }

    static final class SimpleViewHolder extends FlexibleViewHolder {

        ImageView mAlbumArtView;
        TextView mTitle;
        TextView mSubtitle;
        Context mContext;
        View mFrontView;
        TextView mCountView;
        TextView mOrderView;
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
                songController.removeStamp(stampName, mediaId);

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

        public void updateStampList(String artistName) {
            /*
            if (!StampEditStateObserver.getInstance().isOpen()) {
                mStampListLayout.setVisibility(View.GONE);
                return;
            }
            mStampListLayout.setVisibility(View.VISIBLE);
            if (mStampListLayout != null && isStampMedia(mediaId)) {
                mStampListLayout.removeAllViews();
                TextView addView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_new_stamp, null);
                addView.setTag(R.id.text_view_new_stamp_media_id, mediaId);
                addView.setOnClickListener(mOnNewStamp);
                mStampListLayout.addView(addView);
                SongController songController = new SongController(mContext);
                for (String stampName : songController.findStampsByMediaId(mediaId)) {
                    TextView textView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_remove_stamp, null);
                    textView.setText("- " + stampName);
                    textView.setTag(R.id.text_view_remove_stamp_stamp_name, stampName);
                    textView.setTag(R.id.text_view_remove_stamp_media_id, mediaId);
                    textView.setOnClickListener(mOnRemoveStamp);
                    mStampListLayout.addView(textView);
                }
            }
            */
        }

        private boolean isStampMedia(String mediaId) {
            return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
        }

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();

            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mAlbumArtView = (ImageView) view.findViewById(R.id.image);
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
            this.mCountView = (TextView) view.findViewById(R.id.count);
            this.mOrderView = (TextView) view.findViewById(R.id.order);
            this.mStampListLayout = (ViewGroup) view.findViewById(R.id.stamp_info);
        }

        @Override
        public void toggleActivation() {
            super.toggleActivation();
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