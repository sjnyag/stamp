package com.sjn.stamp.ui.custom

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View

import com.gordonwong.materialsheetfab.AnimatedFab

class Fab : FloatingActionButton, AnimatedFab {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Shows the FAB.
     */
    override fun show() {
        show(0f, 0f)
    }

    /**
     * Shows the FAB and sets the FAB's translation.
     *
     * @param translationX translation X value
     * @param translationY translation Y value
     */
    override fun show(translationX: Float, translationY: Float) {
        // NOTE: Using the parameters is only needed if you want
        // to support moving the FAB around the screen.
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        visibility = View.VISIBLE
    }

    /**
     * Hides the FAB.
     */
    override fun hide() {
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        visibility = View.INVISIBLE
    }
}