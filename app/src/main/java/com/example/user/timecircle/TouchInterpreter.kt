package com.example.user.timecircle

import android.content.Context
import android.widget.FrameLayout
import android.widget.Toast
import com.example.user.timecircle.common.CommonUtil
import com.example.user.timecircle.common.UNIT_ANGLE
import kotlinx.android.synthetic.main.time_circle_fragment.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.dimen
import kotlin.math.atan2

open class TouchInterpreter(val viewModel: TimeCircleViewModel, layout: FrameLayout) {
    protected val context: Context = layout.context
    protected val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    protected val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    protected var touchMode: TouchMode = TouchMode.None
    protected var unConfirmDetermineDeffer: Deferred<Unit>? = null

    protected val animationController = AnimationController(layout.time_circle_second_layer, object : AnimationController.AnimationChangeListener {
        override fun onZoom(isZoom: Boolean) {
            this@TouchInterpreter.onZoom(isZoom)
        }
    })
    protected val circleViewsController = CircleViewsController(layout.time_circle_third_layer)

    fun unconfirmedTimer(x: Float, y: Float) =
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

    fun getCircleIndex(x: Float, y: Float, isOutSideRequest: Boolean = false): Int {
        var degree = atan2(x, -y) * (180 / Math.PI) + 720
        if (isOutSideRequest) {
            degree -= animationController.rotateAngle
        }
        return ((degree % 360) / UNIT_ANGLE).toInt()
    }

    fun findIsInCircle(x: Float, y: Float): Boolean {
        val length = CommonUtil.square(x) + CommonUtil.square(y)
        return length in CommonUtil.square(context.dimen(R.dimen.time_image_length) / 2)..CommonUtil.square(context.dimen(R.dimen.time_circle_length) / 2)
    }

    open fun onZoom(isZoom: Boolean) {}
}
