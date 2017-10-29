package com.sjn.stamp.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.MediaMetadataCompat
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.sjn.stamp.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

object DrawerHelper {
    class AccountHeaderLoader(private val accountHeader: AccountHeader, private val metadata: MediaMetadataCompat) : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val profileDrawerItem = createProfileItem(metadata)
            profileDrawerItem.withIcon(bitmap)
            accountHeader.clear()
            accountHeader.addProfiles(profileDrawerItem)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            createDefaultHeader(metadata, accountHeader)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            createDefaultHeader(metadata, accountHeader)
        }
    }

    fun updateHeader(activity: Activity, metadata: MediaMetadataCompat?, accountHeader: AccountHeader?) {
        if (metadata == null || accountHeader == null) {
            return
        }
        activity.runOnUiThread({
            if (metadata.description == null || metadata.description.iconUri == null) {
                createDefaultHeader(metadata, accountHeader)
            } else {
                ViewHelper.readBitmapAsync(activity, metadata.description.iconUri.toString(), AccountHeaderLoader(accountHeader, metadata))
            }
        })
    }

    private fun createProfileItem(metadata: MediaMetadataCompat): ProfileDrawerItem {
        val profileDrawerItem = ProfileDrawerItem()
        metadata.description?.let {
            it.title?.let {
                profileDrawerItem.withName(it.toString())
            }
            it.subtitle?.let {
                profileDrawerItem.withEmail(it.toString())
            }
        }
        return profileDrawerItem
    }

    private fun createDefaultHeader(metadata: MediaMetadataCompat, accountHeader: AccountHeader) {
        val profileDrawerItem = createProfileItem(metadata)
        profileDrawerItem.withIcon(R.mipmap.ic_launcher)
        accountHeader.clear()
        accountHeader.addProfiles(profileDrawerItem)
    }

}