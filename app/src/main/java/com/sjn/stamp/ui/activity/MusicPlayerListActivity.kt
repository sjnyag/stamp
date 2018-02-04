/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sjn.stamp.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.text.TextUtils
import com.sjn.stamp.MusicService
import com.sjn.stamp.R
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.DrawerMenu
import com.sjn.stamp.ui.SongListFragmentFactory
import com.sjn.stamp.ui.fragment.FullScreenPlayerFragment
import com.sjn.stamp.ui.fragment.media_list.MediaBrowserListFragment
import com.sjn.stamp.ui.fragment.media_list.PagerFragment
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.MediaRetrieveHelper
import com.sjn.stamp.utils.PermissionHelper
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.util.*

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
open class MusicPlayerListActivity : MediaBrowserListActivity() {

    private val REQUEST_PERMISSION = 1

    private var voiceSearchParams: Bundle? = null
    private var savedInstanceState: Bundle? = null
    private var newIntent: Intent? = null
    private var reservedUri: Uri? = null

    private val mediaId: String?
        get() {
            return mediaBrowserListFragment?.mediaId ?: return null
        }

    private val mediaBrowserListFragment: MediaBrowserListFragment?
        get() {
            val fragment = supportFragmentManager.findFragmentByTag(DrawerActivity.FRAGMENT_TAG)
            if (fragment is MediaBrowserListFragment) {
                return fragment
            } else if (fragment is PagerFragment) {
                val page = fragment.current
                if (page is MediaBrowserListFragment) {
                    return page
                }
            }
            return null
        }

    override val currentMediaItems: List<AbstractFlexibleItem<*>>
        get() {
            return mediaBrowserListFragment?.currentItems ?: emptyList()
        }

    override val menuResourceId: Int
        get() {
            return mediaBrowserListFragment?.menuResourceId ?: 0
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.d(TAG, "Activity onCreate")

        setContentView(R.layout.activity_player)
        initializeToolbar()
        if (savedInstanceState == null) {
            navigateToBrowser(DrawerMenu.first().fragment, false)
        }

        if (!PermissionHelper.hasPermission(this, MediaRetrieveHelper.PERMISSIONS)) {
            LogHelper.d(TAG, "has no Permission")
            PermissionHelper.requestPermissions(this, MediaRetrieveHelper.PERMISSIONS, REQUEST_PERMISSION)
        }
        this.savedInstanceState = savedInstanceState
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        LogHelper.d(TAG, "onRequestPermissionsResult")
        LogHelper.d(TAG, "onRequestPermissionsResult: requestCode " + requestCode)
        LogHelper.d(TAG, "onRequestPermissionsResult: permissions " + Arrays.toString(permissions))
        LogHelper.d(TAG, "onRequestPermissionsResult: grantResults " + Arrays.toString(grantResults))
        if (!PermissionHelper.hasPermission(this, MediaRetrieveHelper.PERMISSIONS)) {
            DialogFacade.createPermissionNecessaryDialog(this) { _, _ -> finish() }.show()
        }
        if (Arrays.asList(*permissions).contains(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            sendCustomAction(MusicService.CUSTOM_ACTION_RELOAD_MUSIC_PROVIDER, null, null)
        }
    }

    public override fun onResume() {
        super.onResume()
        LogHelper.d(TAG, "Activity onResume")
        (when (newIntent) {
            null -> intent
            else -> newIntent
        })?.let {
            initializeFromParams(savedInstanceState, it)
        }
        newIntent = null
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        val mediaId = mediaId
        if (mediaId != null) {
            outState!!.putString(SAVED_MEDIA_ID, mediaId)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(itemId: Int): Boolean {
        return false
    }

    override fun onNewIntent(intent: Intent) {
        LogHelper.d(TAG, "onNewIntent, intent=" + intent)
        newIntent = intent
    }

    override fun search(query: String, extras: Bundle?, callback: MediaBrowserCompat.SearchCallback) {
        mediaBrowser?.search(query, extras, callback)
    }

    override fun playByCategory(mediaId: String) {
        LogHelper.d(TAG, "playByCategory, mediaId=" + mediaId)
        MediaControllerCompat.getMediaController(this)?.transportControls?.playFromMediaId(mediaId, null)

    }

    override fun onMediaItemSelected(musicId: String) {
        LogHelper.d(TAG, "onMediaItemSelected, musicId=" + musicId)
        MediaControllerCompat.getMediaController(this)?.transportControls?.playFromMediaId(MediaIDHelper.createDirectMediaId(musicId), null)
    }

    override fun onMediaItemSelected(item: MediaBrowserCompat.MediaItem) {
        onMediaItemSelected(item.mediaId, item.isPlayable, item.isBrowsable)
    }

    override fun onMediaItemSelected(mediaId: String?, isPlayable: Boolean, isBrowsable: Boolean) {
        LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + mediaId!!)
        when {
            isPlayable -> {
                MediaControllerCompat.getMediaController(this)?.transportControls?.playFromMediaId(mediaId, null)
            }
            isBrowsable -> navigateToBrowser(mediaId)
            else -> LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ", "mediaId=", mediaId)
        }
    }

    override fun setToolbarTitle(title: CharSequence?) {
        LogHelper.d(TAG, "Setting toolbar title to ", title)
        this.title = title ?: getString(R.string.app_name)
    }

    override fun onMediaControllerConnected() {
        super.onMediaControllerConnected()
        voiceSearchParams?.let {
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            MediaControllerCompat.getMediaController(this)?.transportControls?.playFromSearch(it.getString(SearchManager.QUERY), voiceSearchParams)
            voiceSearchParams = null
        }
        mediaBrowserListFragment?.onMediaControllerConnected()
        reservedUri?.let { uri ->
            MediaControllerCompat.getMediaController(this)?.let { controller ->
                controller.transportControls.playFromUri(uri, null)
            }
            reservedUri = null
        }
    }

    override fun onSessionDestroyed() {}

    private fun startFullScreenIfNeeded(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_START_FULLSCREEN, false) == true) {
            val slidingUpPanelLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout) ?: return
            slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
    }

    private fun initializeFromParams(savedInstanceState: Bundle?, intent: Intent) {
        LogHelper.d(TAG, "initializeFromParams ", intent)
        var mediaId: String? = null
        // check if we were started from a "Play XYZ" voice search. If so, we create the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.action != null && intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            voiceSearchParams = intent.extras
            LogHelper.d(TAG, "Starting from voice search query=", voiceSearchParams!!.getString(SearchManager.QUERY))
        } else if (intent.data != null) {
            LogHelper.d(TAG, "Play from Intent: " + intent.data!!)
            val controller = MediaControllerCompat.getMediaController(this)
            if (controller != null) {
                reservedUri = null
                controller.transportControls.playFromUri(intent.data, null)
            } else {
                reservedUri = intent.data
            }
        } else {
            // If there is a saved media ID, use it
            savedInstanceState?.let { mediaId = it.getString(SAVED_MEDIA_ID) }
        }
        navigateToBrowser(mediaId)
        startFullScreenIfNeeded(intent)
    }

    private fun navigateToBrowser(mediaId: String?) {
        mediaId ?: return
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId)
        mediaBrowserListFragment?.let {
            if (!TextUtils.equals(it.mediaId, mediaId)) {
                navigateToBrowser(SongListFragmentFactory.create(mediaId), true)
            }

        }
    }

    override fun onBackPressed() {
        val slidingUpPanelLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)
        if (slidingUpPanelLayout != null && slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        super.onBackPressed()
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(MusicPlayerListActivity::class.java)
        private val SAVED_MEDIA_ID = "com.sjn.stamp.MEDIA_ID"

        val EXTRA_START_FULLSCREEN = "com.sjn.stamp.EXTRA_START_FULLSCREEN"

        /**
         * Optionally used with [.EXTRA_START_FULLSCREEN] to carry a MediaDescription to
         * the [FullScreenPlayerFragment], speeding up the screen rendering
         * while the [android.support.v4.media.session.MediaControllerCompat] is connecting.
         */
        val EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.sjn.stamp.CURRENT_MEDIA_DESCRIPTION"
    }
}