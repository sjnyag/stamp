package com.sjn.stamp.utils

import android.app.Application
import android.graphics.Color
import io.multimoon.colorful.*

object ColorfulHelper {
    fun init(application: Application) {
        initColorful(application, Defaults(
                primaryColor = ThemeColor.CYAN,
                accentColor = ThemeColor.GREEN,
                useDarkTheme = false,
                translucent = true))

    }
}

fun ColorfulDelegate.getTextColor(): Int {
    return if (Colorful().getDarkTheme()) Color.WHITE else Color.BLACK
}

fun ColorfulDelegate.getCurrent(primary: Boolean = true, dark: Boolean = false): ColorfulColor {
    return if (dark) {
        if (primary) getPrimaryDark() else getAccentDark()
    } else {
        if (primary) getPrimary() else getAccent()
    }
}

fun ColorfulDelegate.getPrimary(): ColorfulColor {
    return Colorful().getPrimaryColor().getColorPack().normal()
}

fun ColorfulDelegate.getAccent(): ColorfulColor {
    return Colorful().getAccentColor().getColorPack().normal()
}

fun ColorfulDelegate.getPrimaryDark(): ColorfulColor {
    return Colorful().getPrimaryColor().getColorPack().dark()
}

fun ColorfulDelegate.getAccentDark(): ColorfulColor {
    return Colorful().getAccentColor().getColorPack().dark()
}