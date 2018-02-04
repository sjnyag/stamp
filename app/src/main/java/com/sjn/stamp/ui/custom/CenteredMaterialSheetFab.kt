package com.sjn.stamp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import com.gordonwong.materialsheetfab.AnimatedFab
import com.gordonwong.materialsheetfab.MaterialSheetFab
import com.gordonwong.materialsheetfab.animations.AnimationListener
import com.gordonwong.materialsheetfab.animations.MaterialSheetAnimation
import io.codetail.animation.SupportAnimator
import java.lang.reflect.Method


class CenteredMaterialSheetFab<FAB>(fab: FAB, sheet: View, overlay: View, sheetColor: Int, fabColor: Int) : MaterialSheetFab<FAB>(fab, sheet, overlay, sheetColor, fabColor) where FAB : View, FAB : AnimatedFab {
    init {
        val interpolator = AnimationUtils.loadInterpolator(sheet.context,
                com.gordonwong.materialsheetfab.R.interpolator.msf_interpolator)
        sheetAnimation = CenteredMaterialSheetAnimation(sheet, sheetColor, fabColor, interpolator)
    }

    private class CenteredMaterialSheetAnimation(private val sheet: View, private val sheetColor: Int, private val fabColor: Int,
                                                 private val interpolator: Interpolator) : MaterialSheetAnimation(sheet, sheetColor, fabColor, interpolator) {

        // Default reveal direction is up and to the left (for FABs in the bottom right corner)
        private val revealXDirection: MaterialSheetFab.RevealXDirection = MaterialSheetFab.RevealXDirection.LEFT
        private var revealYDirection: MaterialSheetFab.RevealYDirection = MaterialSheetFab.RevealYDirection.UP
        private val isSupportCardView: Boolean = sheet.javaClass.name == SUPPORT_CARDVIEW_CLASSNAME
        private var setCardBackgroundColor: Method? = null

        init {
            // Get setCardBackgroundColor() method
            if (isSupportCardView) {
                setCardBackgroundColor = try {
                    sheet.javaClass.getDeclaredMethod("setCardBackgroundColor", Int::class.javaPrimitiveType)
                } catch (e: Exception) {
                    null
                }
            }
        }

        /**
         * Aligns the sheet's position with the FAB.
         *
         * @param fab Floating action button
         */
        override fun alignSheetWithFab(fab: View) {
            // NOTE: View.getLocationOnScreen() returns the view's coordinates on the screen
            // whereas other view methods like getRight() and getY() return coordinates relative
            // to the view's parent. Using those methods can lead to incorrect calculations when
            // the two views do not have the same parent.

            // Get FAB's coordinates
            val fabCoords = IntArray(2)
            fab.getLocationOnScreen(fabCoords)

            // Get sheet's coordinates
            val sheetCoords = IntArray(2)
            sheet.getLocationOnScreen(sheetCoords)

            // NOTE: Use the diffs between the positions of the FAB and sheet to align the sheet.
            // We have to use the diffs because the coordinates returned by getLocationOnScreen()
            // include the status bar and any other system UI elements, meaning the coordinates
            // aren't representative of the usable screen space.
            val leftDiff = sheetCoords[0] - fabCoords[0]
            val rightDiff = sheetCoords[0] + sheet.width - (fabCoords[0] + fab.width)
            val topDiff = sheetCoords[1] - fabCoords[1]
            val bottomDiff = sheetCoords[1] + sheet.height - (fabCoords[1] + fab.height)

            // NOTE: Preserve the sheet's margins to allow users to shift the sheet's position
            // to compensate for the fact that the design support library's FAB has extra
            // padding within the view
            val sheetLayoutParams = sheet
                    .layoutParams as ViewGroup.MarginLayoutParams

            // Set sheet's new coordinates (only if there is a change in coordinates because
            // setting the same coordinates can cause the view to "drift" - moving 0.5 to 1 pixels
            // around the screen)
            //            if (rightDiff != 0) {
            //                float sheetX = sheet.getX();
            //                // Align the right side of the sheet with the right side of the FAB if
            //                // doing so would not move the sheet off the screen
            //                if (rightDiff <= sheetX) {
            //                    sheet.setX(sheetX - rightDiff - sheetLayoutParams.rightMargin);
            //                    revealXDirection = RevealXDirection.LEFT;
            //                }
            //                // Otherwise, align the left side of the sheet with the left side of the FAB
            //                else if (leftDiff != 0 && leftDiff <= sheetX) {
            //                    sheet.setX(sheetX - leftDiff + sheetLayoutParams.leftMargin);
            //                    revealXDirection = RevealXDirection.RIGHT;
            //                }
            //            }

            if (bottomDiff != 0) {
                val sheetY = sheet.y
                // Align the bottom of the sheet with the bottom of the FAB
                if (bottomDiff <= sheetY) {
                    sheet.y = sheetY - bottomDiff.toFloat() - sheetLayoutParams.bottomMargin.toFloat()
                    revealYDirection = MaterialSheetFab.RevealYDirection.UP
                } else if (topDiff != 0 && topDiff <= sheetY) {
                    sheet.y = sheetY - topDiff + sheetLayoutParams.topMargin
                    revealYDirection = MaterialSheetFab.RevealYDirection.DOWN
                }// Otherwise, align the top of the sheet with the top of the FAB
            }
        }

        /**
         * Shows the sheet by morphing the FAB into the sheet.
         *
         * @param fab                    Floating action button
         * @param showSheetDuration      Duration of the sheet animation in milliseconds. Use 0 for no
         * animation.
         * @param showSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
         * animation.
         * @param listener               Listener for animation events.
         */
        override fun morphFromFab(fab: View, showSheetDuration: Long, showSheetColorDuration: Long,
                                  listener: AnimationListener) {
            sheet.visibility = View.VISIBLE
            revealSheetWithFab(fab, getFabRevealRadius(fab), sheetRevealRadius, showSheetDuration,
                    fabColor, sheetColor, showSheetColorDuration, listener)
        }

        /**
         * Hides the sheet by morphing the sheet into the FAB.
         *
         * @param fab                    Floating action button
         * @param hideSheetDuration      Duration of the sheet animation in milliseconds. Use 0 for no
         * animation.
         * @param hideSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
         * animation.
         * @param listener               Listener for animation events.
         */
        override fun morphIntoFab(fab: View?, hideSheetDuration: Long, hideSheetColorDuration: Long,
                                  listener: AnimationListener?) {
            fab ?: return
            revealSheetWithFab(fab, sheetRevealRadius, getFabRevealRadius(fab), hideSheetDuration,
                    sheetColor, fabColor, hideSheetColorDuration, listener)
        }

        override fun revealSheetWithFab(fab: View, startRadius: Float, endRadius: Float,
                                        sheetDuration: Long, startColor: Int, endColor: Int, sheetColorDuration: Long,
                                        listener: AnimationListener?) {
            listener?.onStart()
            // Pass listener to the animation that will be the last to finish
            val revealListener = if (sheetDuration >= sheetColorDuration) listener else null
            val colorListener = if (sheetColorDuration > sheetDuration) listener else null

            // Start animations
            startCircularRevealAnim(sheet, sheetRevealCenterX, getSheetRevealCenterY(fab),
                    startRadius, endRadius, sheetDuration, interpolator, revealListener)
            startColorAnim(sheet, startColor, endColor, sheetColorDuration, interpolator,
                    colorListener)
        }

        override fun startCircularRevealAnim(view: View, centerX: Int, centerY: Int, startRadius: Float,
                                             endRadius: Float, duration: Long, interpolator: Interpolator,
                                             listener: AnimationListener?) {
            // Use native circular reveal on Android 5.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Native circular reveal uses coordinates relative to the view
                val relativeCenterX = (centerX - view.x).toInt()
                val relativeCenterY = (centerY - view.y).toInt()
                // Setup animation
                val anim = ViewAnimationUtils.createCircularReveal(view, relativeCenterX,
                        relativeCenterY, startRadius, endRadius)
                anim.duration = duration
                anim.interpolator = interpolator
                // Add listener
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        listener?.onStart()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        listener?.onEnd()
                    }
                })
                // Start animation
                anim.start()
            } else {
                // Circular reveal library uses absolute coordinates
                // Setup animation
                val anim = io.codetail.animation.ViewAnimationUtils
                        .createCircularReveal(view, centerX, centerY, startRadius, endRadius)
                anim.setDuration(duration.toInt())
                anim.setInterpolator(interpolator)
                // Add listener
                anim.addListener(object : SupportAnimator.SimpleAnimatorListener() {
                    override fun onAnimationStart() {
                        listener?.onStart()
                    }

                    override fun onAnimationEnd() {
                        listener?.onEnd()
                    }
                })
                // Start animation
                anim.start()
            }
        }

        override fun startColorAnim(view: View, startColor: Int, endColor: Int,
                                    duration: Long, interpolator: Interpolator, listener: AnimationListener?) {
            // Setup animation
            val anim = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
            anim.duration = duration
            anim.interpolator = interpolator
            // Add listeners
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    listener?.onStart()
                }

                override fun onAnimationEnd(animation: Animator) {
                    listener?.onEnd()
                }
            })
            anim.addUpdateListener { animator ->
                // Update background color
                val color = animator.animatedValue as Int

                // Use CardView.setCardBackgroundColor() to avoid crashes on Android < 5.0 and to
                // properly set the card's background color without removing the card's other styles
                // See https://github.com/gowong/material-sheet-fab/pull/2 and
                // https://code.google.com/p/android/issues/detail?id=77843
                if (isSupportCardView) {
                    // Use setCardBackground() method if it is available
                    if (setCardBackgroundColor != null) {
                        try {
                            setCardBackgroundColor!!.invoke(sheet, color)
                        } catch (e: Exception) {
                            // Ignore exceptions since there's no other way set a support CardView's
                            // background color
                        }

                    }
                } else {
                    view.setBackgroundColor(color)
                }// Set background color for all other views
            }
            // Start animation
            anim.start()
        }

        override fun setSheetVisibility(visibility: Int) {
            sheet.visibility = visibility
        }

        override fun isSheetVisible(): Boolean {
            return sheet.visibility == View.VISIBLE
        }

        /**
         * @return Sheet reveal's center X coordinate
         */
        override fun getSheetRevealCenterX(): Int {
            return (sheet.x + sheet.width / 2).toInt()
        }

        /**
         * @return Sheet reveal's center Y coordinate
         */
        override fun getSheetRevealCenterY(fab: View): Int {
            return if (revealYDirection == MaterialSheetFab.RevealYDirection.UP) {
                (sheet.y + sheet.height * (SHEET_REVEAL_OFFSET_Y - 1) / SHEET_REVEAL_OFFSET_Y - fab.height / 2).toInt()
            } else (sheet.y + (sheet.height / SHEET_REVEAL_OFFSET_Y).toFloat()
                    + (fab.height / 2).toFloat()).toInt()
        }

        override fun getSheetRevealRadius(): Float {
            return Math.max(sheet.width, sheet.height).toFloat()
        }

        override fun getFabRevealRadius(fab: View?): Float {
            fab ?: return 0F
            return (Math.max(fab.width, fab.height) / 2).toFloat()
        }

        override fun getRevealXDirection(): MaterialSheetFab.RevealXDirection {
            return revealXDirection
        }

        override fun getRevealYDirection(): MaterialSheetFab.RevealYDirection? {
            return revealYDirection
        }

        companion object {

            private const val SUPPORT_CARDVIEW_CLASSNAME = "android.support.v7.widget.CardView"
            private const val SHEET_REVEAL_OFFSET_Y = 5
        }
    }

}
