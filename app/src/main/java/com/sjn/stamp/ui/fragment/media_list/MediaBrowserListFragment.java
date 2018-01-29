package com.sjn.stamp.ui.fragment.media_list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjn.stamp.ui.MediaBrowsable;
import com.sjn.stamp.ui.observer.MediaControllerObserver;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.MediaIDHelper;

import java.util.List;

public abstract class MediaBrowserListFragment extends ListFragment implements MediaControllerObserver.Listener {

    private static final String TAG = LogHelper.INSTANCE.makeLogTag(MediaBrowserListFragment.class);
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
                    LogHelper.INSTANCE.d(TAG, "onChildrenLoaded START");
                    LogHelper.INSTANCE.d(TAG, "onChildrenLoaded parentId: " + parentId);
                    LogHelper.INSTANCE.d(TAG, "onChildrenLoaded children: " + children.size());
                    onMediaBrowserChildrenLoaded(parentId, children);
                    LogHelper.INSTANCE.d(TAG, "onChildrenLoaded END");
                }

                @Override
                public void onError(@NonNull String id) {
                    LogHelper.INSTANCE.d(TAG, "onError START");
                    onMediaBrowserError(id);
                    LogHelper.INSTANCE.d(TAG, "onError END");
                }
            };

    @Override
    public void onAttach(Context context) {
        LogHelper.INSTANCE.d(TAG, "onAttach START");
        super.onAttach(context);
        if (context instanceof MediaBrowsable) {
            mMediaBrowsable = (MediaBrowsable) context;
        }
        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        if (mediaBrowser == null) {
            return;
        }
        LogHelper.INSTANCE.d(TAG, "fragment.onAttach, mediaId=", mMediaId,
                "  onMediaControllerConnected=" + mediaBrowser.isConnected());
        if (mediaBrowser.isConnected()) {
            onMediaControllerConnected();
        }
        LogHelper.INSTANCE.d(TAG, "onAttach END");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.INSTANCE.d(TAG, "onCreateView START");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        LogHelper.INSTANCE.d(TAG, "onCreateView END");
        return view;
    }

    @Override
    public void onStart() {
        LogHelper.INSTANCE.d(TAG, "onStart START");
        super.onStart();
        updateTitle();
        MediaControllerObserver.INSTANCE.addListener(this);
        LogHelper.INSTANCE.d(TAG, "onStart END");
    }

    @Override
    public void onResume() {
        LogHelper.INSTANCE.d(TAG, "onResume START");
        super.onResume();
        LogHelper.INSTANCE.d(TAG, "onResume END");
    }

    @Override
    public void onPause() {
        LogHelper.INSTANCE.d(TAG, "onPause START");
        super.onPause();
        LogHelper.INSTANCE.d(TAG, "onPause END");
    }

    @Override
    public void onStop() {
        LogHelper.INSTANCE.d(TAG, "onStop START");
        super.onStop();
        MediaControllerObserver.INSTANCE.removeListener(this);
        LogHelper.INSTANCE.d(TAG, "onStop END");
    }

    @Override
    public void onDetach() {
        LogHelper.INSTANCE.d(TAG, "onDetach START");
        super.onDetach();
        MediaBrowserCompat mediaBrowser = mMediaBrowsable.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        mMediaBrowsable = null;
        LogHelper.INSTANCE.d(TAG, "onDetach END");
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    @Override
    public void onMediaControllerConnected() {
        LogHelper.INSTANCE.d(TAG, "onMediaControllerConnected START");
        if (isDetached() || mMediaBrowsable == null) {
            LogHelper.INSTANCE.d(TAG, "onMediaControllerConnected SKIP");
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMediaBrowsable.getMediaBrowser().getRoot();
        }
        LogHelper.INSTANCE.d(TAG, "onMediaControllerConnected mediaId: " + mMediaId);

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
        LogHelper.INSTANCE.d(TAG, "onMediaControllerConnected END");
    }

    @Override
    public void onSessionDestroyed() {

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
        LogHelper.INSTANCE.d(TAG, "reloadList START");
        mMediaBrowsable.getMediaBrowser().unsubscribe(mMediaId);
        mMediaBrowsable.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);
        LogHelper.INSTANCE.d(TAG, "reloadList END");
    }

    protected void updateTitle() {
        LogHelper.INSTANCE.d(TAG, "updateTitle START");
        if (mListener == null) {
            return;
        }
        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mMediaId)) {
            mListener.setToolbarTitle(null);
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
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
                mListener.setToolbarTitle(MediaIDHelper.INSTANCE.extractBrowseCategoryValueFromMediaID(mMediaId));
            }
        });
        LogHelper.INSTANCE.d(TAG, "updateTitle END");
    }
}
