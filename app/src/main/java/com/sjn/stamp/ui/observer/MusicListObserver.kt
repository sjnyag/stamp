package com.sjn.stamp.ui.observer

import com.sjn.stamp.utils.LogHelper
import java.util.*

object MusicListObserver {
    private val TAG = LogHelper.makeLogTag(MusicListObserver::class.java)

    private val mListenerList = Collections.synchronizedList(ArrayList<Listener>())

    fun notifyMediaListUpdated() {
        LogHelper.i(TAG, "notifyMediaListUpdated:", mListenerList!!.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onMediaListUpdated()
        }
    }

    interface Listener {
        fun onMediaListUpdated()
    }

    fun addListener(listener: Listener) {
        if (!mListenerList.contains(listener)) {
            mListenerList.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        if (mListenerList.contains(listener)) {
            mListenerList.remove(listener)
        }
    }

}
