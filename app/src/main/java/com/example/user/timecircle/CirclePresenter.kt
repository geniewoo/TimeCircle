package com.example.user.timecircle

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.example.user.timecircle.common.FirstTouchInterpreter

class CirclePresenter(lifeCycleOwner: LifecycleOwner, layout: ConstraintLayout, private val viewModel: TimeCircleViewModel) {
    private val context = layout.context
    private val touchInterpreter = FirstTouchInterpreter(viewModel, layout)
}