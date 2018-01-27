package com.sjn.stamp.utils

import android.content.Context
import android.content.Intent
import com.sjn.stamp.R
import java.util.*


object ShareHelper {

    fun share(context: Context?, text: String?, hashTagList: ArrayList<String>?) {
        if (context == null || text == null) {
            return
        }
        var hashTags = ""
        hashTagList?.let {
            if (!it.isEmpty()) {
                for (hashTag in it) {
                    hashTags += "#$hashTag "
                }
                hashTags += "\n"
            }
        }
        context.startActivity(Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text + "\n"
                        + hashTags
                        + context.resources.getString(R.string.hash_tag) + "\n"
                        + context.resources.getString(R.string.base_url)))
    }
}
