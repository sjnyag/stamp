package com.sjn.stamp.ui.observer

import com.sjn.stamp.utils.LogHelper
import java.util.*


object StampEditStateObserver {

    private val TAG = LogHelper.makeLogTag(StampEditStateObserver::class.java)
    private var state = State.NO_EDIT

    var selectedStampList: List<String> = ArrayList()
        internal set

    private val mListenerList = Collections.synchronizedList(ArrayList<Listener>())

    val isStampMode: Boolean
        get() = state == State.EDITING || state == State.STAMPING

    enum class State {
        EDITING,
        NO_EDIT,
        STAMPING
    }

    fun notifyAllStampChange(stamp: String) {
        LogHelper.i(TAG, "notifyAllStampChange ", mListenerList!!.size)
        for (listener in ArrayList(mListenerList)) {
            listener.onNewStampCreated(stamp)
        }
    }

    fun notifySelectedStampListChange(stampList: List<String>) {
        LogHelper.i(TAG, "notifySelectedStampListChange ", mListenerList!!.size)
        selectedStampList = stampList
        for (listener in ArrayList(mListenerList)) {
            listener.onSelectedStampChange(selectedStampList)
        }
    }

    fun notifyStateChange(state: State) {
        LogHelper.i(TAG, "notifyStateChange ", state)
        this.state = state
        for (listener in ArrayList(mListenerList)) {
            listener.onStampStateChange(this.state)
        }
    }

    interface Listener {
        fun onSelectedStampChange(selectedStampList: List<String>)

        fun onNewStampCreated(stamp: String)

        fun onStampStateChange(state: State)
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
