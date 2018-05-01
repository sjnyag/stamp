package com.sjn.stamp.ui.fragment.media

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.DecelerateInterpolator

import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener
import com.sjn.stamp.R
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.custom.CenteredMaterialSheetFab
import com.sjn.stamp.ui.custom.Fab
import com.sjn.stamp.ui.item.holder.SongViewHolder
import com.sjn.stamp.ui.observer.StampEditStateObserver
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.SpotlightHelper
import com.sjn.stamp.utils.ViewHelper
import com.takusemba.spotlight.SimpleTarget
import com.takusemba.spotlight.Spotlight

abstract class FabFragment : Fragment(), StampEditStateObserver.Listener {

    var isShowing = true
    protected var recyclerView: RecyclerView? = null
    protected var adapter: SongAdapter? = null
    private var fab: Fab? = null
    private var centeredMaterialSheetFab: CenteredMaterialSheetFab<*>? = null
    private var startStampEdit: View.OnClickListener = View.OnClickListener { StampEditStateObserver.notifyStateChange(StampEditStateObserver.State.EDITING) }
    private var stopStampEdit: View.OnClickListener = View.OnClickListener { StampEditStateObserver.notifyStateChange(StampEditStateObserver.State.NO_EDIT) }

    private fun openStampEdit() {
        if (centeredMaterialSheetFab == null || fab == null) return
        if (centeredMaterialSheetFab?.isSheetVisible() == false) centeredMaterialSheetFab?.showSheet()
        fab?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.RED)
            setImageResource(R.drawable.ic_dialog_close_light)
            setTag(R.id.fab_type, R.drawable.ic_dialog_close_light)
            setOnClickListener(stopStampEdit)
        }
    }

    private fun closeStampEdit() {
        if (centeredMaterialSheetFab == null || fab == null) return
        if (centeredMaterialSheetFab?.isSheetVisible() == true) centeredMaterialSheetFab?.hideSheet()
        fab?.apply {
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            setImageResource(R.drawable.ic_stamp)
            setTag(R.id.fab_type, R.drawable.ic_stamp)
            setOnClickListener(startStampEdit)
        }
    }

    protected fun hideFab() {
        fab ?: return
        ViewCompat.animate(fab)
                .scaleX(0f).scaleY(0f)
                .alpha(0f).setDuration(100)
                .start()
    }

    protected fun showFab() {
        fab ?: return
        ViewCompat.animate(fab)
                .scaleX(1f).scaleY(1f)
                .alpha(1f).setDuration(200)
                .setStartDelay(300L)
                .start()
    }

    protected fun initializeFab(resourceId: Int, color: ColorStateList, onClickListener: View.OnClickListener?) {
        fab = activity?.findViewById<Fab>(R.id.fab)?.apply {
            visibility = View.VISIBLE
            if (Integer.valueOf(resourceId) != getTag(R.id.fab_type)) {
                StampEditStateObserver.notifyStateChange(StampEditStateObserver.State.NO_EDIT)
                setTag(R.id.fab_type, resourceId)
                setImageResource(resourceId)
                backgroundTintList = color
                onClickListener?.let {
                    setOnClickListener(it)
                }
                ViewCompat.animate(this)
                        .scaleX(1f).scaleY(1f)
                        .alpha(1f).setDuration(100)
                        .setStartDelay(300L)
                        .start()
            }
        }
    }

    protected fun initializeFabWithStamp() {
        initializeFab(R.drawable.ic_stamp, ColorStateList.valueOf(Color.WHITE), startStampEdit)
        activity?.let { activity ->
            centeredMaterialSheetFab = CenteredMaterialSheetFab(fab!!,
                    activity.findViewById<View>(R.id.fab_sheet).apply {
                        visibility = View.VISIBLE
                    },
                    activity.findViewById<View>(R.id.overlay).apply {
                        visibility = View.VISIBLE
                    },
                    ViewHelper.getThemeColor(activity, R.attr.colorPrimaryDark, Color.DKGRAY),
                    ViewHelper.getThemeColor(activity, R.attr.colorAccent, Color.DKGRAY))
            centeredMaterialSheetFab?.setEventListener(object : MaterialSheetFabEventListener() {
                override fun onShowSheet() {}

                override fun onSheetShown() {
                    StampEditStateObserver.notifyStateChange(StampEditStateObserver.State.EDITING)
                }

                override fun onHideSheet() {}

                override fun onSheetHidden() {
                    StampEditStateObserver.notifyStateChange(if (StampEditStateObserver.selectedStampList.isEmpty()) StampEditStateObserver.State.NO_EDIT else StampEditStateObserver.State.STAMPING)
                }
            })
        }
    }

    fun performFabAction() {
        //default implementation does nothing
    }

    override fun onStart() {
        LogHelper.d(TAG, "onStart START")
        super.onStart()
        StampEditStateObserver.addListener(this)
        LogHelper.d(TAG, "onStart END")
    }

    override fun onStop() {
        LogHelper.d(TAG, "onStop START")
        super.onStop()
        if (centeredMaterialSheetFab?.isSheetVisible() == true) centeredMaterialSheetFab?.hideSheet()
        StampEditStateObserver.removeListener(this)
        LogHelper.d(TAG, "onStop END")
    }

    override fun onSelectedStampChange(selectedStampList: List<String>) {}

    override fun onNewStampCreated(stamp: String) {}

    /**
     * [StampEditStateObserver.Listener]
     */
    override fun onStampStateChange(state: StampEditStateObserver.State) {
        LogHelper.d(TAG, "onStampStateChange: ", state)
        if (centeredMaterialSheetFab == null || fab == null) {
            return
        }
        when (state) {
            StampEditStateObserver.State.EDITING -> openStampEdit()
            StampEditStateObserver.State.NO_EDIT -> closeStampEdit()
            StampEditStateObserver.State.STAMPING -> {
                if (centeredMaterialSheetFab?.isSheetVisible() == true) {
                    centeredMaterialSheetFab?.hideSheet()
                }
                if (isShowing && !SpotlightHelper.isShown(activity, SpotlightHelper.KEY_STAMP_ADD)) {
                    showSpotlight()
                }
            }
        }
        adapter?.notifyDataSetChanged()
    }

    private fun showSpotlight() {
        if (recyclerView == null) {
            return
        }
        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        val view = recyclerView?.findViewHolderForAdapterPosition(layoutManager.findFirstVisibleItemPosition())
        if (view is SongViewHolder) {
            view.showTapTargetView?.let { textView ->
                activity?.let { activity ->
                    Spotlight.with(activity)
                            .setDuration(200L)
                            .setAnimation(DecelerateInterpolator(2f))
                            .setTargets(SimpleTarget.Builder(activity)
                                    .setPoint(textView)
                                    .setRadius(120f)
                                    .setTitle(getString(R.string.spotlight_stamp_add_title))
                                    .setDescription(getString(R.string.spotlight_stamp_add_description))
                                    .build())
                            .start()
                    SpotlightHelper.setShown(activity, SpotlightHelper.KEY_STAMP_ADD)
                }
            }
        }
    }

    companion object {

        private val TAG = LogHelper.makeLogTag(FabFragment::class.java)
    }
}
