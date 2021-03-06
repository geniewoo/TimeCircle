package com.example.user.timecircle

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.example.user.timecircle.common.UNIT_ANGLE
import com.example.user.timecircle.common.cocoLog
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.atan2
import kotlin.text.Typography.degree

class CirclePresenter(lifeCycleOwner: LifecycleOwner, layout: FrameLayout, private val viewModel: TimeCircleViewModel) {
    private val context = layout.context

    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private var downTouchedRotatePos = Pair(0f, 0f)
    private var animationController = AnimationController(layout, lifeCycleOwner, viewModel)
    private val circleViewsController = CircleViewsController(layout)
    private var touchMode: TouchMode = TouchMode.None


    init {
        layout.onClick { viewModel.isZoom.value = true }
        layout.onTouch { _, event ->
            onTimeCircleTouched(event)
        }

        animationController.initValues()
    }

    private fun onTimeCircleTouched(motionEvent: MotionEvent): Boolean {
        if (viewModel.isZoom.value == false) {
            return false
        }

        val x = motionEvent.x - centerX
        val y = motionEvent.y - centerY

        when (motionEvent.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (touchMode) {
                    is TouchMode.AdjustActivity -> {
                        circleViewsController.adjustActivityDone(touchMode as TouchMode.AdjustActivity)
                    }
                    is TouchMode.Rotating -> {
                        animationController.rotatingDone()
                    }
                }
                touchMode = TouchMode.None
                animationController.initActivityTouchValues()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                touchMode = findTouchMode(x, y)
                when (touchMode) {
                    is TouchMode.Rotating -> downTouchedRotatePos = Pair(x, y)
                }
                return touchMode != TouchMode.None
            }
            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    is TouchMode.Rotating -> {
                        rotate(x, y)
                    }
                    is TouchMode.AdjustActivity -> {
                        val touchedIndex = getCircleIndex(x, y)
                        (touchMode as? TouchMode.AdjustActivity)?.let {
                            circleViewsController.adjustActivity(touchedIndex, it)
                        }
                        changeColorAndRotate(x, y)
                    }
                    else -> return false
                }
            }
        }
        return false
    }

    private fun findTouchMode(x: Float, y: Float): TouchMode {
        val length = square(x) + square(y)
        return if (length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)) {
            val touchedIndex = getCircleIndex(x, y)
            circleViewsController.returnIndexEdgeOfActivitySet(touchedIndex)
                    ?: TouchMode.Rotating(touchedIndex)
        } else TouchMode.None
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        // touchedIndex 현재 눌린 시간 인덱
        val touchedIndex = getCircleIndex(x, y)

        animationController.computeAndRotateState(touchedIndex)
    }

    private fun rotate(x: Float, y: Float) {
        val x1 = (x - (animationController.zoomInX + centerX)) / 2
        val y1 = (y - (animationController.zoomInY + centerY)) / 2

        val x2 = (downTouchedRotatePos.first - (animationController.zoomInX + centerX)) / 2
        val y2 = (downTouchedRotatePos.second - (animationController.zoomInY + centerY)) / 2

//        val degree1 = atan2(x1.toDouble(), y1.toDouble())
//        val degree2 = atan2(x2.toDouble(), y2.toDouble())

        val degree1 = (atan2(x, -y) * (180 / Math.PI) + 720) % 360
        val degree2 = (atan2(downTouchedRotatePos.first, -downTouchedRotatePos.second) * (180 / Math.PI) + 720) % 360
        //fixme 0-> 360 혹은 360-> 0 될 수 있어서 조정해준다. 더 좋은 방법있으면 수정바람
        val degree = (degree2 - degree1).let { if (it < -180) it + 360 else if (it > 180) it - 360 else it }

        cocoLog("degree : " + (degree) + "x : $x y : $y", 22)
        animationController.rotate((degree).toFloat())
//        var rotateIndex = animationController.calculateRotateIndex(touchedIndex)
//        //회전 시킬 일 없으면 return
//        if (rotateIndex == 0) return
//
//        rotateIndex = rotateIndex.coerceIn(-MAX_ROTATE_INDEX, MAX_ROTATE_INDEX)
//
//        rotateBaseIndex += rotateIndex
//        if (rotateBaseIndex > CIRCLE_NUM - 1) rotateBaseIndex -= CIRCLE_NUM
//        else if (rotateBaseIndex < 0) rotateBaseIndex += CIRCLE_NUM
//
//        rotateAngle += rotateIndex * UNIT_ANGLE
//        layout.animate().rotation(-rotateAngle).setDuration(100).start()
//        layout.animation
    }

    private fun getCircleIndex(x: Float, y: Float, isOutSideRequest: Boolean = false): Int {
        var degree = atan2(x, -y) * (180 / Math.PI) + 720
        if (isOutSideRequest) {
            degree -= animationController.rotateAngle
        }
        return ((degree % 360) / UNIT_ANGLE).toInt()
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()

    fun onActivityTouch(x: Float, y: Float, activityComponent: ActivityComponent, touchFinish: Boolean): Boolean {
        // 줌상태 고려해주지 않기 때문에 2로 나누어준다.
        val activityX = (x - (animationController.zoomInX + centerX)) / 2
        val activityY = (y - (animationController.zoomInY + centerY)) / 2

        val isValid = findIsInCircle(activityX, activityY)
        if (isValid) {
            val touchedIndex = getCircleIndex(activityX, activityY, true)
            // 이미 다른 엑티비티가 있는 경우 return
            return circleViewsController.changeColorForActivityDrop(activityComponent, touchedIndex, touchFinish)
        } else {
            circleViewsController.removeColorForActivityDrop()
        }
        return isValid
    }

    private fun findIsInCircle(x: Float, y: Float): Boolean {
        val length = square(x) + square(y)
        return length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)
    }
}