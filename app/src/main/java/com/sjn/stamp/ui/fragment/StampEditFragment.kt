package com.sjn.stamp.ui.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.gc.materialdesign.views.ButtonFloatSmall
import com.github.sjnyag.animationwraplayout.AnimationWrapLayout
import com.sjn.stamp.R
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.custom.ToggleTextView
import com.sjn.stamp.ui.observer.StampEditStateObserver

class StampEditFragment : Fragment(), StampEditStateObserver.Listener {
    private var stampListLayout: AnimationWrapLayout? = null
    private var registerButton: ButtonFloatSmall? = null
    private var emptyString: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_stamp_edit, container, false)
        stampListLayout = rootView.findViewById(R.id.stamp_list_layout)
        emptyString = rootView.findViewById(R.id.stamp_list_empty)
        registerButton = (View.inflate(context, R.layout.add_stamp_button, null) as ButtonFloatSmall).apply {
            setOnClickListener {
                context.let {
                    DialogFacade.createRegisterStampDialog(it)
                }
            }
        }
        rootView.findViewById<Button>(R.id.button_ok).apply {
            setOnClickListener {
                val state = if (StampEditStateObserver.selectedStampList.isEmpty()) StampEditStateObserver.State.NO_EDIT else StampEditStateObserver.State.STAMPING
                StampEditStateObserver.notifyStateChange(state)
            }
        }
        context?.let {
            drawStampList(it, StampController(it).findAllMyStamps())
        }
        return rootView
    }

    private fun drawStampList(context: Context, stampList: List<String>?) {
        stampListLayout?.let {
            it.removeAllViews()
            it.addView(registerButton)
            if (stampList == null || stampList.isEmpty()) {
                emptyString?.visibility = View.VISIBLE
                return
            }
            emptyString?.visibility = View.GONE
            for (stampName in stampList) {
                it.addView(inflateStampView(context, stampName))
            }
        }
    }

    private fun updateEmptyString(stampList: List<String>?) {
        emptyString?.visibility = if (stampList?.isEmpty() == true) View.VISIBLE else View.GONE
    }

    private fun inflateStampView(context: Context, stampName: String): ToggleTextView {
        return (View.inflate(context, R.layout.text_view_select_stamp, null) as ToggleTextView).apply {
            setOnClickListener { notifySelectedStampListChange() }
            setOnLongClickListener(View.OnLongClickListener { view ->
                val stamp = (view as ToggleTextView).text.toString()
                if (stamp.isEmpty()) {
                    return@OnLongClickListener false
                }
                DialogFacade.createRemoveStampSongDialog(context, stamp, { _, _ ->
                    getContext().let {
                        StampController(it).delete(stamp, false)
                        stampListLayout?.removeViewWithAnimation(view)
                        updateEmptyString(StampController(it).findAll())
                    }
                }).show()
                true
            })
            text = stampName
        }
    }

    private fun notifySelectedStampListChange() {
        stampListLayout?.let { list ->
            val stampList = (0 until list.childCount)
                    .map { list.getChildAt(it) }
                    .filterIsInstance<ToggleTextView>()
                    .filter { it.value }
                    .map { it.text.toString() }
            StampEditStateObserver.notifySelectedStampListChange(stampList)
        }
    }

    override fun onStart() {
        super.onStart()
        StampEditStateObserver.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        StampEditStateObserver.removeListener(this)
    }

    override fun onSelectedStampChange(selectedStampList: List<String>) {}

    override fun onNewStampCreated(stamp: String) {
        context?.let {
            stampListLayout?.addViewWithAnimation(inflateStampView(it, stamp), 1)
            updateEmptyString(StampController(it).findAll())
        }
    }

    override fun onStampStateChange(state: StampEditStateObserver.State) {}
}