package com.example.user.timecircle

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log.i
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dimen
import kotlin.math.atan2

/**
 * Created by SungWoo on 2018-08-06.
 */

private const val INIT_ROTATION_ANGLE = 0.0f
private const val SCALE1 = 1.0f
private const val scale2 = 4.0f
private const val CIRCLE_IMAGE_SCALE1 = 1.0f
private const val CIRCLE_IMAGE_SCALE2 = 1.6f
private const val position1 = 0.0f
private const val position2 = -1500.0f
private const val duration: Long = 500

class TimeCircleFragment : Fragment() {
    private val centerX by lazy { dimen(R.dimen.timeCircle_Length) / 2 }
    private val centerY by lazy { dimen(R.dimen.timeCircle_Length) / 2 }
    private val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    private lateinit var circleFrameLayout: FrameLayout
    private lateinit var circleImageView: ImageView
    private var isSelectionMode = false
    private var rotateAngle = INIT_ROTATION_ANGLE
    private var isZoomed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ui = UI {
            verticalLayout {
                onClick { zoomOut() }
                circleFrameLayout = frameLayout {
                    onClick { zoomIn() }
                    onTouch { _, event -> onTimeCircleTouched(event) }
                    onLongClick { changeToSelectionMode() }
                    circleImageView = imageView {
                        imageResource = R.drawable.circle_image
                        z = 2.0f
                    }.lparams(wrapContent, wrapContent) {
                        gravity = Gravity.CENTER
                    }
                    imageView {
                        imageResource = R.drawable.circle_stroke_image
                        z = 2.0f
                    }.lparams(wrapContent, wrapContent) {
                        gravity = Gravity.CENTER
                    }
                }.lparams(dimen(R.dimen.timeCircle_Length), dimen(R.dimen.timeCircle_Length)) {
                    gravity = Gravity.CENTER
                }
                for (i in 0 until CIRCLE_NUM) {
                    val circleView = CircleView(context)
                    circleView.z = 1.0f
                    circleFrameLayout.addView(circleView)
                    circleViews[i] = circleView
                }
            }
        }
        return ui.view
    }

    private fun zoomIn() {
        if (!isZoomed) {
            circleFrameLayout.animate().scaleX(scale2).scaleY(scale2).y(position2).setDuration(duration).start()
            circleImageView.animate().scaleX(CIRCLE_IMAGE_SCALE2).scaleY(CIRCLE_IMAGE_SCALE2).setDuration(duration).start()
            isZoomed = true
        }
    }

    private fun zoomOut() {
        if (isZoomed) {
            rotateAngle = INIT_ROTATION_ANGLE
            circleFrameLayout.animate().scaleX(SCALE1).scaleY(SCALE1).y(position1).setDuration(duration).rotation(rotateAngle).start()
            circleImageView.animate().scaleX(CIRCLE_IMAGE_SCALE1).scaleY(CIRCLE_IMAGE_SCALE1).setDuration(duration).start()
            isZoomed = false
        }
    }

    private fun changeToSelectionMode() {
        if (isZoomed) {
            isSelectionMode = true
        }
    }

    private fun onTimeCircleTouched(motionEvent: MotionEvent): Boolean {
        if (!isZoomed) {
            return false
        }

        if (motionEvent.action == MotionEvent.ACTION_UP) {
            isSelectionMode = false
            return true
        }

        if (!isSelectionMode) {
            return false
        }

        val x = centerX - motionEvent.x
        val y = centerY - motionEvent.y

        val length = square(x) + square(y)

        if (isTouchedInCircle(length)) {
            changeColor(x, y)
        }
        return true
    }

    private fun isTouchedInCircle(length: Float): Boolean {
        return length < square(dimen(R.dimen.timeCircle_Length) / 2) && length > square(dimen(R.dimen.timeImage_Length) / 2)
    }

    private fun changeColor(x: Float, y: Float) {
        val circleIndex = (((-atan2(x, y) * (180 / Math.PI) + 360) % 360) / UNIT_ANGLE).toInt()
        circleViews[circleIndex]?.apply {
            color = Color.GREEN
            invalidate()
        }
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()
}