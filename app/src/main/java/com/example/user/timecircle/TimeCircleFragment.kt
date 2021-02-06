package com.example.user.timecircle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.time_circle_fragment.*
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

/**
 * Created by SungWoo on 2018-08-06.
 */

class TimeCircleFragment : Fragment() {

    lateinit var circlePresenter: CirclePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.time_circle_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        circlePresenter = CirclePresenter(view.time_circle_frame_layout)
        time_circle_root_layout.setOnClickListener { circlePresenter.zoomOut() }

        drag_view.onTouch = { x, y, touchFinish ->
            circlePresenter.onActivityTouch(x, y, touchFinish)
        }
    }
}