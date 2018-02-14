package com.sjn.stamp.ui.fragment.media

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sjn.stamp.ui.MediaBrowsable
import com.sjn.stamp.ui.observer.MediaBrowserObserver
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper

abstract class MediaBrowserListFragment : ListFragment(), MediaBrowserObserver.Listener {

    var mediaBrowsable: MediaBrowsable? = null
    var mediaId: String?
        get() {
            return arguments?.getString(ARG_MEDIA_ID)
        }
        set(mediaId) {
            arguments = Bundle(1).apply { putString(MediaBrowserListFragment.ARG_MEDIA_ID, mediaId) }
        }

    private val mSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            LogHelper.d(TAG, "onChildrenLoaded START")
            LogHelper.d(TAG, "onChildrenLoaded parentId: " + parentId)
            LogHelper.d(TAG, "onChildrenLoaded children: " + children.size)
            onMediaBrowserChildrenLoaded(parentId, children)
            LogHelper.d(TAG, "onChildrenLoaded END")
        }

        override fun onError(id: String) {
            LogHelper.d(TAG, "onError START")
            onMediaBrowserError(id)
            LogHelper.d(TAG, "onError END")
        }
    }

    internal abstract fun onMediaBrowserChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>)

    internal abstract fun onMediaBrowserError(parentId: String)

    override fun onAttach(context: Context?) {
        LogHelper.d(TAG, "onAttach START")
        super.onAttach(context)
        if (context is MediaBrowsable) {
            mediaBrowsable = context
        }
        LogHelper.d(TAG, "fragment.onAttach, mediaId=", mediaId)
        if (mediaBrowsable?.mediaBrowser?.isConnected == true) {
            onMediaBrowserConnected()
        }
        LogHelper.d(TAG, "onAttach END")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogHelper.d(TAG, "onCreateView START")
        val view = super.onCreateView(inflater, container, savedInstanceState)
        LogHelper.d(TAG, "onCreateView END")
        return view
    }

    override fun onStart() {
        LogHelper.d(TAG, "onStart START")
        super.onStart()
        updateTitle()
        MediaBrowserObserver.addListener(this)
        LogHelper.d(TAG, "onStart END")
    }

    override fun onResume() {
        LogHelper.d(TAG, "onResume START")
        super.onResume()
        LogHelper.d(TAG, "onResume END")
    }

    override fun onPause() {
        LogHelper.d(TAG, "onPause START")
        super.onPause()
        LogHelper.d(TAG, "onPause END")
    }

    override fun onStop() {
        LogHelper.d(TAG, "onStop START")
        super.onStop()
        MediaBrowserObserver.removeListener(this)
        LogHelper.d(TAG, "onStop END")
    }

    override fun onDetach() {
        LogHelper.d(TAG, "onDetach START")
        super.onDetach()
        if (mediaBrowsable?.mediaBrowser?.isConnected == true) {
            mediaId?.let { mediaBrowsable?.mediaBrowser?.unsubscribe(it) }
        }
        mediaBrowsable = null
        LogHelper.d(TAG, "onDetach END")
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    override fun onMediaBrowserConnected() {
        LogHelper.d(TAG, "onMediaControllerConnected START")
        if (isDetached || mediaBrowsable == null) {
            LogHelper.d(TAG, "onMediaControllerConnected SKIP")
            return
        }
        LogHelper.d(TAG, "onMediaControllerConnected mediaId: " + mediaId)

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mediaId?.let {
            mediaBrowsable?.mediaBrowser?.unsubscribe(it)
            mediaBrowsable?.mediaBrowser?.subscribe(it, mSubscriptionCallback)
        }

        // Add MediaController callback so we can redraw the list when metadata changes:
        // MediaControllerObserver.getInstance().addListener(this);
        LogHelper.d(TAG, "onMediaControllerConnected END")
    }

    protected fun reloadList() {
        LogHelper.d(TAG, "reloadList START")
        mediaId?.let {
            mediaBrowsable?.mediaBrowser?.unsubscribe(it)
            mediaBrowsable?.mediaBrowser?.subscribe(it, mSubscriptionCallback)
        }
        LogHelper.d(TAG, "reloadList END")
    }

    private fun updateTitle() {
        LogHelper.d(TAG, "updateTitle START")
        mediaId?.let {
            mediaBrowsable?.mediaBrowser?.getItem(it, object : MediaBrowserCompat.ItemCallback() {
                override fun onItemLoaded(item: MediaBrowserCompat.MediaItem?) {
                    item?.description?.title?.let { listener?.setToolbarTitle(it) }
                }

                override fun onError(itemId: String) {
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(it)?.let { listener?.setToolbarTitle(it) }
                }
            })
        }
        LogHelper.d(TAG, "updateTitle END")
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MediaBrowserListFragment::class.java)
        private const val ARG_MEDIA_ID = "media_id"
    }
}
