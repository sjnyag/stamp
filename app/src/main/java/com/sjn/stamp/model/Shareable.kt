package com.sjn.stamp.model

import android.content.res.Resources

interface Shareable {
    fun share(resources: Resources): String
}
