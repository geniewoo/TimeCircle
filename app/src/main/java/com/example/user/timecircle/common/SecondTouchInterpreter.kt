package com.example.user.timecircle.common

import android.widget.FrameLayout
import com.example.user.timecircle.ThirdTouchInterpreter
import com.example.user.timecircle.TimeCircleViewModel
import org.jetbrains.anko.sdk25.coroutines.onClick

open class SecondTouchInterpreter(viewModel: TimeCircleViewModel, layout: FrameLayout): ThirdTouchInterpreter(viewModel, layout) {
    init {
        layout.onClick { animationController.isZoomed = true }
    }
}