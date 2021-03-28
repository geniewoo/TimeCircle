package com.example.user.timecircle.common

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.example.user.timecircle.TimeCircleViewModel
import com.example.user.timecircle.common.views.DragActivityView
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

class FirstTouchInterpreter(viewModel: TimeCircleViewModel, private val firstLayout: ConstraintLayout): SecondTouchInterpreter(viewModel, firstLayout.time_circle_second_layer) {
    init {
        firstLayout.setOnClickListener { animationController.isZoomed = false }
        firstLayout.activity_list.children.forEach {
            (it as? DragActivityView)?.onTouch = { x, y, activityComponent, touchFinish ->
                onActivityTouch(x, y, activityComponent, touchFinish)
            }
        }
        firstLayout.setOnTouchListener { v, event ->
            false
        }
    }

    override fun onZoom(isZoom: Boolean) {
        firstLayout.activity_list.isVisible = isZoom
    }
}