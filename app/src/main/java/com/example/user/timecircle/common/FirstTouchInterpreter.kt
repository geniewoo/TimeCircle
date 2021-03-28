package com.example.user.timecircle.common

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.user.timecircle.TimeCircleViewModel
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

class FirstTouchInterpreter(viewModel: TimeCircleViewModel, layout: ConstraintLayout): SecondTouchInterpreter(viewModel, layout.time_circle_second_layer) {
    init {
        layout.setOnClickListener { viewModel.isZoom.value = false }
    }
}