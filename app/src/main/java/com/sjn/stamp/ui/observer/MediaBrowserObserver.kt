package com.sjn.stamp.ui.observer

import com.sjn.stamp.utils.LogHelper
import java.util.*

object MediaBrowserObserver {

    private val TAG = LogHelper.makeLogTag(MediaBrowserObserver::class.java)
    private val listenerList = Collections.synchronizedList(ArrayList<Listener>())

    interface Listener {

        fun onMediaBrowserConnected()
    }

    fun notifyConnected() {
        LogHelper.i(TAG, "notifyConnected ", listenerList?.size)
        for (listener in ArrayList(listenerList)) {
            listener.onMediaBrowserConnected()
        }
    }

    fun addListener(listener: Listener) {
        LogHelper.i(TAG, "addListener")
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        LogHelper.i(TAG, "removeListener")
        if (listenerList.contains(listener)) {
            listenerList.remove(listener)
        }
    }
}
