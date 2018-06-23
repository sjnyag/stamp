package com.sjn.stamp.ui.fragment.media

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.sjn.stamp.R
import com.sjn.stamp.ui.item.ProgressItem
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.tintByTheme
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.util.*


abstract class ListFragment : FabFragment(), SwipeRefreshLayout.OnRefreshListener, FastScroller.OnScrollStateChangeListener, FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemLongClickListener, FlexibleAdapter.EndlessScrollListener, FlexibleAdapter.OnUpdateListener, StampEditStateObserver.Listener {

    abstract val menuResourceId: Int
    var currentItems: List<AbstractFlexibleItem<*>> = ArrayList()
    protected var loading: ProgressBar? = null
    protected var emptyView: View? = null
    protected var emptyTextView: TextView? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null
    protected var fastScroller: FastScroller? = null
    protected var progressItem = ProgressItem()
    protected var listener: FragmentInteractionListener? = null
    protected var listState: Parcelable? = null


    private val refreshHandler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
        when (message.what) {
            0 // Stop
            -> {
                swipeRefreshLayout?.isRefreshing = false
                true
            }
            1 // Start
            -> {
                swipeRefreshLayout?.isRefreshing = true
                true
            }
            2 // Show empty view
            -> {
                emptyView?.let {
                    ViewCompat.animate(it).alpha(1f)
                }
                true
            }
            else -> false
        }
    })

    interface FragmentInteractionListener {

        fun onFragmentChange(swipeRefreshLayout: SwipeRefreshLayout?, recyclerView: RecyclerView?,
                             @SelectableAdapter.Mode mode: Int)

        fun initSearchView(menu: Menu?)

        fun startActionModeByLongClick(position: Int)

        fun destroyActionModeIfCan()

        fun setToolbarTitle(title: CharSequence?)

        fun updateContextTitle(selectedItemCount: Int)

        fun restoreSelection()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        LogHelper.d(TAG, "setUserVisibleHint START")
        super.setUserVisibleHint(isVisibleToUser)
        isShowing = isVisibleToUser
        if (isShowing && view != null) {
            notifyFragmentChange()
        }
        LogHelper.d(TAG, "setUserVisibleHint END")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogHelper.d(TAG, "onCreate START")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        LogHelper.d(TAG, "onCreate END")
    }

    override fun onAttach(context: Context?) {
        LogHelper.d(TAG, "onAttach START")
        super.onAttach(context)
        if (context is FragmentInteractionListener) {
            listener = context
        }
        LogHelper.d(TAG, "onAttach END")
    }

    override fun onDetach() {
        LogHelper.d(TAG, "onDetach START")
        super.onDetach()
        listener = null
        LogHelper.d(TAG, "onDetach END")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        LogHelper.d(TAG, "onCreateOptionsMenu START")
        //menu.clear();
        inflater?.inflate(menuResourceId, menu)
        menu?.let { it ->
            for (i in 0 until it.size()) {
                context?.let { context ->
                    it.getItem(i).tintByTheme(context)
                }
            }
        }
        listener?.initSearchView(menu)
        LogHelper.d(TAG, "onCreateOptionsMenu END")
    }

    fun notifyFragmentChange() {
        LogHelper.d(TAG, "notifyFragmentChange START")
        listener?.onFragmentChange(swipeRefreshLayout, recyclerView, SelectableAdapter.Mode.IDLE)
        LogHelper.d(TAG, "notifyFragmentChange END")
    }


    open fun emptyMessage(): String {
        return getString(R.string.no_items)
    }

    override fun onUpdateEmptyView(size: Int) {
        LogHelper.d(TAG, "onUpdateEmptyView START ", size)
        emptyTextView?.text = emptyMessage()
        fastScroller?.let {
            it.visibility = if (size > 0 || loading?.visibility == View.VISIBLE) View.VISIBLE else View.GONE
        }
        emptyView?.let {
            it.alpha = 0f
            it.visibility = if (size > 0 || loading?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        if (size > 0 || loading?.visibility == View.VISIBLE) {
            refreshHandler.removeMessages(2)
        } else {
            refreshHandler.sendEmptyMessage(2)
        }
        //        if (adapter != null) {
        //            String message = (adapter.hasSearchText() ? "Filtered " : "Refreshed ");
        //            message += size + " items in " + adapter.getTime() + "ms";
        //            Snackbar.make(getActivity().findViewById(R.id.main_view), message, Snackbar.LENGTH_SHORT).show();
        //        }
        LogHelper.d(TAG, "onUpdateEmptyView END")
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        LogHelper.d(TAG, "onSaveInstanceState START")
        listState = recyclerView?.layoutManager?.onSaveInstanceState()
        state.putParcelable(LIST_STATE_KEY, listState)
        LogHelper.d(TAG, "onSaveInstanceState END")
    }

    /**
     * [FastScroller.OnScrollStateChangeListener]
     */
    override fun onFastScrollerStateChange(scrolling: Boolean) {
        if (scrolling) hideFab() else showFab()
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(ListFragment::class.java)
        const val LIST_STATE_KEY = "LIST_STATE_KEY"
    }
}
