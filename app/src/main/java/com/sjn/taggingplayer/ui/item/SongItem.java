package com.sjn.taggingplayer.ui.item;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjn.taggingplayer.R;
import com.sjn.taggingplayer.controller.SongController;
import com.sjn.taggingplayer.ui.observer.TagEditStateObserver;
import com.sjn.taggingplayer.utils.MediaIDHelper;
import com.sjn.taggingplayer.utils.ViewHelper;
import com.squareup.picasso.Target;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.flipview.FlipView;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SongItem extends AbstractItem<SongItem.SimpleViewHolder>
        implements ISectionable<SongItem.SimpleViewHolder, DateHeaderItem>, IFilterable, Serializable {

    /* The header of this item */
    DateHeaderItem header;

    public MediaBrowserCompat.MediaItem getMediaItem() {
        return mMediaItem;
    }

    MediaBrowserCompat.MediaItem mMediaItem;
    //to avoid GC
    private Target mTarget;

    private SongItem(MediaBrowserCompat.MediaItem mediaItem) {
        super(String.valueOf(mediaItem.getMediaId()));
        setDraggable(true);
        setSwipeable(true);
        mMediaItem = mediaItem;
    }

    public SongItem(MediaBrowserCompat.MediaItem mediaItem, DateHeaderItem header) {
        this(mediaItem);
        this.header = header;
    }

    @Override
    public String getSubtitle() {
        if (mMediaItem.getDescription().getSubtitle() == null) {
            return "";
        }
        return mMediaItem.getDescription().getSubtitle().toString();
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
        return R.layout.recycler_simple_item;
    }

    @Override
    public SimpleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new SimpleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void bindViewHolder(final FlexibleAdapter adapter, SimpleViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

//        // Background, when bound the first time
//        if (payloads.size() == 0) {
//            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
//                    Color.WHITE, Color.parseColor("#dddddd"), //Same color of divider
//                    DrawableUtils.getColorControlHighlight(context));
//            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
//            DrawableUtils.setBackgroundCompat(holder.frontView, drawable);
//        }

        // DemoApp: INNER ANIMATION EXAMPLE! ImageView - Handle Flip Animation
//		if (adapter.isSelectAll() || adapter.isLastItemInActionMode()) {
//			// Consume the Animation
//			holder.mFlipView.flip(adapter.isSelected(position), 200L);
//		} else {
        // Display the current flip status
        holder.mFlipView.flipSilently(adapter.isSelected(position));
//		}

        // In case of searchText matches with Title or with a field this will be highlighted
        if (adapter.hasSearchText()) {
            Utils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
            Utils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
        } else {
            holder.mTitle.setText(mMediaItem.getDescription().getTitle());
            holder.mSubtitle.setText(mMediaItem.getDescription().getSubtitle());
        }
        if (mMediaItem.getDescription().getIconUri() != null) {
            ViewHelper.updateAlbumArt((Activity) context, holder.mFlipView, mMediaItem.getDescription().getIconUri().toString(), mMediaItem.getDescription().getTitle().toString());
        }
        holder.update(holder.mImageView, mMediaItem);

        holder.updateTagList(mMediaItem.getMediaId());
    }

    @Override
    public boolean filter(String constraint) {
        return mMediaItem != null && mMediaItem.getDescription().getTitle() != null && mMediaItem.getDescription().getTitle().toString().toLowerCase().trim().contains(constraint) ||
                mMediaItem != null && mMediaItem.getDescription().getSubtitle() != null && mMediaItem.getDescription().getSubtitle().toString().toLowerCase().trim().contains(constraint);
    }

    static final class SimpleViewHolder extends FlexibleViewHolder {

        public static final int STATE_INVALID = -1;
        public static final int STATE_NONE = 0;
        public static final int STATE_PLAYABLE = 1;
        public static final int STATE_PAUSED = 2;
        public static final int STATE_PLAYING = 3;

        private static ColorStateList sColorStatePlaying;
        private static ColorStateList sColorStateNotPlaying;

        FlipView mFlipView;
        TextView mTitle;
        TextView mSubtitle;
        ImageView mHandleView;
        Context mContext;
        View frontView;
        TextView mDate;
        ImageView mImageView;
        ViewGroup mTagListLayout;
        View.OnClickListener mOnNewTag = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagEditStateObserver tagEditStateObserver = TagEditStateObserver.getInstance();
                final String mediaId = (String) v.getTag(R.id.text_view_new_tag_media_id);
                SongController songController = new SongController(mContext);
                songController.registerTagList(tagEditStateObserver.getSelectedTagList(), mediaId);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTagList(mediaId);
                    }
                });
            }
        };

        View.OnClickListener mOnRemoveTag = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mediaId = (String) v.getTag(R.id.text_view_remove_tag_media_id);
                final String tagName = (String) v.getTag(R.id.text_view_remove_tag_tag_name);
                SongController songController = new SongController(mContext);
                songController.removeTag(tagName, mediaId);

                if (mContext instanceof Activity) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTagList(mediaId);
                        }
                    });
                }
            }
        };

        public boolean swiped = false;

        public void updateTagList(String mediaId) {
            if (mTagListLayout != null && isTagMedia(mediaId)) {
                mTagListLayout.removeAllViews();
                SongController songController = new SongController(mContext);
                for (String tagName : songController.findTagsByMediaId(mediaId)) {
                    TextView text = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_remove_tag, null);
                    text.setText("- " + tagName);
                    text.setTag(R.id.text_view_remove_tag_tag_name, tagName);
                    text.setTag(R.id.text_view_remove_tag_media_id, mediaId);
                    text.setOnClickListener(mOnRemoveTag);
                    mTagListLayout.addView(text);
                }
                TextView text = (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view_new_tag, null);
                text.setTag(R.id.text_view_new_tag_media_id, mediaId);
                text.setOnClickListener(mOnNewTag);
                mTagListLayout.addView(text);
            }
        }

        private boolean isTagMedia(String mediaId) {
            return MediaIDHelper.getCategoryType(mediaId) != null || MediaIDHelper.isTrack(mediaId);
        }

        SimpleViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mContext = view.getContext();
            if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
                initializeColorStateLists(this.mContext);
            }

            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mFlipView = (FlipView) view.findViewById(R.id.image);
            this.mFlipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.mItemLongClickListener != null) {
                        mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
                        Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                        toggleActivation();
                    }
                }
            });
            this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
            this.mImageView = (ImageView) view.findViewById(R.id.play_eq);
            setDragHandleView(mHandleView);

            this.frontView = view.findViewById(R.id.front_view);
            this.mDate = (TextView) view.findViewById(R.id.date);
            this.mTagListLayout = (ViewGroup) view.findViewById(R.id.tag_info);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            super.onClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            return super.onLongClick(view);
        }

        @Override
        public void toggleActivation() {
            super.toggleActivation();
            // Here we use a custom Animation inside the ItemView
            mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
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
        public void onItemReleased(int position) {
            swiped = (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE);
            super.onItemReleased(position);
        }

        public void update(View view, MediaBrowserCompat.MediaItem mediaItem) {
            Integer cachedState = STATE_INVALID;
            cachedState = (Integer) view.getTag(R.id.tag_mediaitem_state_cache);
            int state = getMediaItemState(this.mContext, mediaItem);
            if (cachedState == null || cachedState != state) {
                Drawable drawable = getDrawableByState(this.mContext, state);
                if (drawable != null) {
                    this.mImageView.setImageDrawable(drawable);
                    this.mImageView.setVisibility(View.VISIBLE);
                } else {
                    this.mImageView.setVisibility(View.GONE);
                }
                view.setTag(R.id.tag_mediaitem_state_cache, state);
            }
        }

        private static void initializeColorStateLists(Context ctx) {
            sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                    R.color.media_item_icon_not_playing));
            sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                    R.color.media_item_icon_playing));
        }

        public static Drawable getDrawableByState(Context context, int state) {
            if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
                initializeColorStateLists(context);
            }

            switch (state) {
                case STATE_PLAYABLE:
                    Drawable pauseDrawable = ContextCompat.getDrawable(context,
                            R.drawable.ic_play_arrow_black_36dp);
                    DrawableCompat.setTintList(pauseDrawable, sColorStateNotPlaying);
                    return pauseDrawable;
                case STATE_PLAYING:
                    AnimationDrawable animation = (AnimationDrawable)
                            ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp);
                    DrawableCompat.setTintList(animation, sColorStatePlaying);
                    animation.start();
                    return animation;
                case STATE_PAUSED:
                    Drawable playDrawable = ContextCompat.getDrawable(context,
                            R.drawable.ic_equalizer1_white_36dp);
                    DrawableCompat.setTintList(playDrawable, sColorStatePlaying);
                    return playDrawable;
                default:
                    return null;
            }
        }

        public static int getMediaItemState(Context context, MediaBrowserCompat.MediaItem mediaItem) {
            int state = STATE_NONE;
            // Set state to playable first, then override to playing or paused state if needed
            if (mediaItem.isPlayable()) {
                state = STATE_PLAYABLE;
                if (MediaIDHelper.isMediaItemPlaying(context, mediaItem)) {
                    state = getStateFromController(context);
                }
            }

            return state;
        }

        public static int getStateFromController(Context context) {
            MediaControllerCompat controller = ((FragmentActivity) context)
                    .getSupportMediaController();
            PlaybackStateCompat pbState = controller.getPlaybackState();
            if (pbState == null ||
                    pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
                return STATE_NONE;
            } else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                return STATE_PLAYING;
            } else {
                return STATE_PAUSED;
            }
        }
    }

    @Override
    public String toString() {
        return "SongHistoryItem[" + super.toString() + "]";
    }

}