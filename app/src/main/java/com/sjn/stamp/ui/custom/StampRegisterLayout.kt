package com.sjn.stamp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.sjn.stamp.R
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.ui.observer.StampEditStateObserver

class StampRegisterLayout : RelativeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val root = LayoutInflater.from(context).inflate(R.layout.layout_stamp_register, this)
        val stampInput = root.findViewById<View>(R.id.stamp_input) as EditText
        val stampRegisterButton = root.findViewById<View>(R.id.stamp_register) as Button
        stampRegisterButton.setOnClickListener({
            val stampName = stampInput.text.toString()
            getContext()?.let {
                if (StampController(it).register(stampName, false)) {
                    StampEditStateObserver.notifyAllStampChange(stampName)
                    stampInput.setText("")
                }
            }
        })
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

}