package com.sjn.stamp.db

import android.content.res.Resources

interface Shareable {
    fun share(resources: Resources): String
}
