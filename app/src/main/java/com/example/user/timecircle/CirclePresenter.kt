package com.example.user.timecircle

import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.user.timecircle.common.CommonUtil.dpToPx
import com.example.user.timecircle.common.CommonUtil.square
import com.example.user.timecircle.common.UNIT_ANGLE
import kotlinx.coroutines.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class CirclePresenter(lifeCycleOwner: LifecycleOwner, layout: FrameLayout, private val viewModel: TimeCircleViewModel) {
    private val context = layout.context

    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private var animationController = AnimationController(layout, lifeCycleOwner, viewModel)
    private val circleViewsController = CircleViewsController(layout)
    private var touchMode: TouchMode = TouchMode.None
    private var unconfirmedModePos: Pair<Float, Float>? = null
    private var unConfirmDetermineDeffer: Deferred<Unit>? = null
    private fun unconfirmedTimer() =
            GlobalScope.launch {
                unConfirmDetermineDeffer = async {
                    delay(1000)
                }
                unConfirmDetermineDeffer?.await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "longClick", Toast.LENGTH_SHORT).show()
                }
            }


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
                    is TouchMode.AdjustActivity -> circleViewsController.adjustActivityDone(touchMode as TouchMode.AdjustActivity)
                    is TouchMode.Rotating -> animationController.rotatingDone()
                    is TouchMode.Unconfirmed -> {
                        if (unConfirmDetermineDeffer?.isActive == true) {
                            Toast.makeText(context, "shortClick", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                unConfirmDetermineDeffer?.cancel()
                touchMode = TouchMode.None
                unconfirmedModePos = null
                animationController.initActivityTouchValues()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                touchMode = findTouchMode(x, y)
                when (touchMode) {
                    is TouchMode.Rotating -> animationController.downTouchedRotatePos = Pair(x, y)
                    is TouchMode.Unconfirmed -> {
                        unconfirmedModePos = Pair(x, y)
                        unconfirmedTimer()
                    }
                }
                return touchMode != TouchMode.None
            }
            MotionEvent.ACTION_MOVE -> {
                (touchMode as? TouchMode.Unconfirmed)?.let { unconfirmedMode ->
                    unconfirmedModePos?.let {
                        val distance = sqrt((it.first - x).pow(2) + (it.second - y).pow(2))
                        if (distance > 5f.dpToPx(context) && unconfirmedMode.adjustDirection != null) {
                            unConfirmDetermineDeffer?.cancel()
                            touchMode = TouchMode.AdjustActivity(unconfirmedMode)
                        } else if (distance > 5f.dpToPx(context)) {
                            unConfirmDetermineDeffer?.cancel()
                            touchMode = TouchMode.None
                        }
                    }
                }
                when (touchMode) {
                    is TouchMode.Rotating -> {
                        animationController.rotate(x, y)
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
            circleViewsController.returnActivityUnconfirmedMode(touchedIndex)
                    ?: TouchMode.Rotating(touchedIndex)
        } else TouchMode.None
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        // touchedIndex 현재 눌린 시간 인덱
        val touchedIndex = getCircleIndex(x, y)

        animationController.computeAndRotateState(touchedIndex)
    }

    private fun getCircleIndex(x: Float, y: Float, isOutSideRequest: Boolean = false): Int {
        var degree = atan2(x, -y) * (180 / Math.PI) + 720
        if (isOutSideRequest) {
            degree -= animationController.rotateAngle
        }
        return ((degree % 360) / UNIT_ANGLE).toInt()
    }

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