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
private const val rotateBaseRightUnit = -4
private const val rotateBaseLeftUnit = 3
private const val MAX_ROTATE_INDEX = 3

class TimeCircleFragment : Fragment() {
    private val centerX by lazy { dimen(R.dimen.timeCircle_Length) / 2 }
    private val centerY by lazy { dimen(R.dimen.timeCircle_Length) / 2 }
    private val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    private lateinit var circleFrameLayout: FrameLayout
    private lateinit var circleImageView: ImageView
    private var isSelectionMode = false
    private var rotateAngle = INIT_ROTATION_ANGLE
    private var isZoomed = false
    private var rotateBaseIndex = CIRCLE_NUM / 2

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
            initValues()
            circleFrameLayout.animate().scaleX(SCALE1).scaleY(SCALE1).y(position1).setDuration(duration).rotation(rotateAngle).start()
            circleImageView.animate().scaleX(CIRCLE_IMAGE_SCALE1).scaleY(CIRCLE_IMAGE_SCALE1).setDuration(duration).start()
            isZoomed = false
        }
    }

    private fun initValues() {
        rotateAngle = INIT_ROTATION_ANGLE
        rotateBaseIndex = CIRCLE_NUM / 2
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
            changeColorAndRotate(x, y)
        }
        return true
    }

    private fun isTouchedInCircle(length: Float): Boolean {
        return length < square(dimen(R.dimen.timeCircle_Length) / 2) && length > square(dimen(R.dimen.timeImage_Length) / 2)
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        val touchedIndex = getCircleIndex(x, y)
        circleViews[touchedIndex]?.apply {
            color = Color.GREEN
            invalidate()
        }

        i("coco", "rotatebseindex $rotateBaseIndex circleindex $touchedIndex")
        var rotateIndex = calculateRotateIndex(touchedIndex)
        if (rotateIndex == 0) return

        i("COCO", "rotationIndex $rotateIndex")
        rotateIndex = when {
            rotateIndex > MAX_ROTATE_INDEX -> MAX_ROTATE_INDEX
            rotateIndex < -MAX_ROTATE_INDEX -> -MAX_ROTATE_INDEX
            else -> rotateIndex
        }
        rotateBaseIndex += rotateIndex
        if (rotateBaseIndex > CIRCLE_NUM - 1) rotateBaseIndex -= CIRCLE_NUM
        else if (rotateBaseIndex < 0) rotateBaseIndex += CIRCLE_NUM
        rotateAngle += rotateIndex * UNIT_ANGLE
        circleFrameLayout.animate().rotation(-rotateAngle).setDuration(300).start()
    }

    private fun calculateRotateIndex(touchedIndex: Int): Int {
        return when {
            touchedIndex < CIRCLE_NUM / 4 && CIRCLE_NUM * 3 / 4 < rotateBaseIndex ->
                if (CIRCLE_NUM + touchedIndex - rotateBaseIndex > rotateBaseLeftUnit) {
                    CIRCLE_NUM + touchedIndex - rotateBaseIndex - rotateBaseLeftUnit
                } else {
                    0
                }
            rotateBaseIndex < CIRCLE_NUM / 4 && CIRCLE_NUM * 3 / 4 < touchedIndex ->
                if (-CIRCLE_NUM + touchedIndex - rotateBaseIndex < rotateBaseRightUnit) {
                    -CIRCLE_NUM + touchedIndex - rotateBaseIndex - rotateBaseRightUnit
                } else {
                    0
                }
            touchedIndex - rotateBaseIndex > rotateBaseLeftUnit -> touchedIndex - rotateBaseIndex - rotateBaseLeftUnit
            touchedIndex - rotateBaseIndex < rotateBaseRightUnit -> touchedIndex - rotateBaseIndex - rotateBaseRightUnit
            else -> 0
        }
    }

    private fun getCircleIndex(x: Float, y: Float): Int {
        return (((-atan2(x, y) * (180 / Math.PI) + 360) % 360) / UNIT_ANGLE).toInt()
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()
}