package com.example.user.timecircle

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.user.timecircle.common.CommonUtil.dpToPx
import com.example.user.timecircle.common.CommonUtil.square
import com.example.user.timecircle.common.UNIT_ANGLE
import com.example.user.timecircle.common.cocoLog
import kotlinx.coroutines.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class CirclePresenter(lifeCycleOwner: LifecycleOwner, layout: FrameLayout, private val viewModel: TimeCircleViewModel) {
    private val context = layout.context

    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val animationController = AnimationController(layout, lifeCycleOwner, viewModel)
    private val circleViewsController = CircleViewsController(layout)
    private var touchMode: TouchMode = TouchMode.None
    private var unconfirmedModePos: Pair<Float, Float>? = null
    private var unConfirmDetermineDeffer: Deferred<Unit>? = null

    private fun unconfirmedTimer(x: Float, y: Float) =
            GlobalScope.launch {
                unConfirmDetermineDeffer = async {
                    delay(1000)
                }
                unConfirmDetermineDeffer?.await()
                (touchMode as? TouchMode.UnconfirmedRotating)?.let {
                    touchMode = TouchMode.MoveActivity(getCircleIndex(x, y, true), it.activitySet)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "longClick", Toast.LENGTH_SHORT).show()
                }
            }


    init {
        layout.onClick { viewModel.isZoom.value = true }
        layout.setOnTouchListener { _, event ->
            onTimeCircleTouched(event)
            gestureDetector.onTouchEvent(event)
        }

        animationController.initValues()
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return true
        }
    })

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
                    is TouchMode.UnconfirmedRotating -> {
                        animationController.rotatingDone()
                        if (unConfirmDetermineDeffer?.isActive == true) {
                            Toast.makeText(context, "shortClick", Toast.LENGTH_SHORT).show()
                            // todo shortclick dialog 추가
                        }
                    }
                    is TouchMode.MoveActivity -> {
                        (touchMode as? TouchMode.MoveActivity)?.let {
                            circleViewsController.removeColorForActivityEdit()
                            circleViewsController.removeColorForActivityMove(it.activitySet)
                            val touchedIndex = getCircleIndex(x, y)
                            if (findIsInCircle(x, y)) {
                                if (touchedIndex in it.activitySet.fromIndex..it.activitySet.toIndex) {
                                    circleViewsController.recoverColorForActivityMoveCancel(it.activitySet)
                                } else {
                                    circleViewsController.removeActivityForMove(it.activitySet)
                                    circleViewsController.changeColorForActivityEdit(it.activitySet.component, touchedIndex, true)
                                }
                            } else {
                                circleViewsController.removeActivityForMove(it.activitySet)
                            }
                        }
                        return true
                    }
                }
                unConfirmDetermineDeffer?.cancel()
                touchMode = TouchMode.None
                unconfirmedModePos = null
                animationController.initActivityTouchValues()
                return false
            }
            MotionEvent.ACTION_DOWN -> {
                touchMode = findTouchMode(x, y)
                when (touchMode) {
                    is TouchMode.Rotating -> animationController.downTouchedRotatePos = Pair(x, y)
                    is TouchMode.UnconfirmedRotating -> {
                        animationController.downTouchedRotatePos = Pair(x, y)
                        unconfirmedModePos = Pair(x, y)
                        unconfirmedTimer(x, y)
                    }
                }
                return touchMode != TouchMode.None
            }
            MotionEvent.ACTION_MOVE -> {
                cocoLog("coco77 move")
                (touchMode as? TouchMode.UnconfirmedRotating)?.let { unconfirmedMode ->
                    unconfirmedModePos?.let {
                        val distance = sqrt((it.first - x).pow(2) + (it.second - y).pow(2))
                        if (distance > 5f.dpToPx(context) && unconfirmedMode.adjustDirection != null) {
                            unConfirmDetermineDeffer?.cancel()
                            touchMode = TouchMode.AdjustActivity(unconfirmedMode)
                        } else if (distance > 5f.dpToPx(context)) {
                            unConfirmDetermineDeffer?.cancel()
                            touchMode = TouchMode.Rotating(getCircleIndex(it.first, it.second))
                        }
                    }
                }
                when (touchMode) {
                    is TouchMode.Rotating -> animationController.rotate(x, y)
                    is TouchMode.UnconfirmedRotating -> animationController.rotate(x, y)
                    is TouchMode.AdjustActivity -> {
                        val touchedIndex = getCircleIndex(x, y)
                        (touchMode as? TouchMode.AdjustActivity)?.let {
                            circleViewsController.adjustActivity(touchedIndex, it)
                        }
                        changeColorAndRotate(x, y)
                    }
                    is TouchMode.MoveActivity -> {
                        (touchMode as? TouchMode.MoveActivity)?.let {
                            circleViewsController.changeColorForActivityMoveReady(it.activitySet)

                            val touchedIndex = getCircleIndex(x, y)
                            if (findIsInCircle(x, y)) {
                                if (touchedIndex in it.activitySet.fromIndex..it.activitySet.toIndex) {
                                    circleViewsController.removeColorForActivityEdit()
                                } else {
                                    circleViewsController.changeColorForActivityEdit(it.activitySet.component, touchedIndex, false)
                                }
                            } else {
                                circleViewsController.removeColorForActivityEdit()
                            }
                            return true
                        }
                    }
                    else -> return false
                }
            }
        }
        return false
    }

    private fun findTouchMode(x: Float, y: Float): TouchMode = if (findIsInCircle(x, y)) {
        val touchedIndex = getCircleIndex(x, y)
        circleViewsController.returnActivityUnconfirmedMode(touchedIndex)
                ?: TouchMode.Rotating(touchedIndex)
    } else TouchMode.None

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
            return circleViewsController.changeColorForActivityEdit(activityComponent, touchedIndex, touchFinish)
        } else {
            circleViewsController.removeColorForActivityEdit()
        }
        return isValid
    }

    private fun findIsInCircle(x: Float, y: Float): Boolean {
        val length = square(x) + square(y)
        return length in square(context.dimen(R.dimen.time_image_length) / 2)..square(context.dimen(R.dimen.time_circle_length) / 2)
    }
}