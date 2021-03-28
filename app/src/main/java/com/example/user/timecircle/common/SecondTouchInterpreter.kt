package com.example.user.timecircle.common

import android.widget.FrameLayout
import com.example.user.timecircle.ThirdTouchInterpreter
import com.example.user.timecircle.TimeCircleViewModel
import kotlinx.android.synthetic.main.time_circle_fragment.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick

open class SecondTouchInterpreter(viewModel: TimeCircleViewModel, layout: FrameLayout): ThirdTouchInterpreter(viewModel, layout.time_circle_third_layer) {
    init {
        layout.onClick { viewModel.isZoom.value = true }
    }
}