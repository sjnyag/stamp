package com.sjn.stamp.ui.fragment.media_list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.ui.activity.MediaBrowsable;
import com.sjn.stamp.ui.observer.MediaControllerObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.List;

public abstract class MediaBrowserListFragment extends ListFragment implements MediaControllerObserver.Listener {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserListFragment.class);
    private static final String ARG_MEDIA_ID = "media_id";

    protected String mMediaId;
    protected MediaBrowsable mMediaBrowsable;

    abstract void onMediaBrowserChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children);

    abstract void onMediaBrowserError(@NonNull String parentId);

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    onMediaBrowserChildrenLoaded(parentId, children);
                }

                @Override
                public void onError(@NonNull String id) {
                    onMediaBrowserError(id);
                }
            };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MediaBrowsable) {
            mMediaBrowsable = (MediaBrowsable) context;
        }
        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        if (mediaBrowser == null) {
            return;
        }
        LogHelper.d(TAG, "fragment.onAttach, mediaId=", mMediaId,
                "  onConnected=" + mediaBrowser.isConnected());
        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "fragment.onCreateView");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogHelper.d(TAG, "fragment.onStart");
        MediaControllerObserver.getInstance().addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.d(TAG, "fragment.onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogHelper.d(TAG, "fragment.onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.d(TAG, "fragment.onStop");
        MediaControllerObserver.getInstance().removeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogHelper.d(TAG, "fragment.onDetach");
        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        mMediaBrowsable = null;
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    @Override
    public void onConnected() {
        LogHelper.d(TAG, "fragment.onConnected");
        if (isDetached() || mMediaBrowsable == null) {
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMediaBrowsable.getMediaBrowser().getRoot();
        }
        updateTitle();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mMediaBrowsable.getMediaBrowser().unsubscribe(mMediaId);

        mMediaBrowsable.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        // MediaControllerObserver.getInstance().addListener(this);
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(MediaBrowserListFragment.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    protected void reloadList() {
        LogHelper.d(TAG, "fragment.reloadList");
        mMediaBrowsable.getMediaBrowser().unsubscribe(mMediaId);
        mMediaBrowsable.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

    }

    protected void updateTitle() {
        if (mListener == null) {
            return;
        }
        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mMediaId)) {
            mListener.setToolbarTitle(null);
            return;
        }

        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        mediaBrowser.getItem(mMediaId, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
                if (mListener == null) {
                    return;
                }
                mListener.setToolbarTitle(
                        item.getDescription().getTitle());
            }

            @Override
            public void onError(@NonNull String itemId) {
                if (mListener == null) {
                    return;
                }
                mListener.setToolbarTitle(MediaIDHelper.extractBrowseCategoryValueFromMediaID(mMediaId));
            }
        });
    }
}
