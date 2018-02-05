package com.sjn.stamp.ui.observer

import com.sjn.stamp.utils.LogHelper
import java.util.*

object MusicListObserver {
    private val TAG = LogHelper.makeLogTag(MusicListObserver::class.java)

    private val listenerList = Collections.synchronizedList(ArrayList<Listener>())

    fun notifyMediaListUpdated() {
        LogHelper.i(TAG, "notifyMediaListUpdated:", listenerList!!.size)
        for (listener in ArrayList(listenerList)) {
            listener.onMediaListUpdated()
        }
    }

    interface Listener {
        fun onMediaListUpdated()
    }

    fun addListener(listener: Listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        if (listenerList.contains(listener)) {
            listenerList.remove(listener)
        }
    }

}
