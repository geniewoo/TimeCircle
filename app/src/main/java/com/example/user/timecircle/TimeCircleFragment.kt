package com.example.user.timecircle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.user.timecircle.common.views.DragActivityView
import kotlinx.android.synthetic.main.time_circle_fragment.*
import kotlinx.android.synthetic.main.time_circle_fragment.view.*

/**
 * Created by SungWoo on 2018-08-06.
 */

class TimeCircleFragment : Fragment() {

    lateinit var circlePresenter: CirclePresenter
    private val viewModel: TimeCircleViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.time_circle_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        circlePresenter = CirclePresenter(viewLifecycleOwner, view.time_circle_frame_layout, viewModel)
        time_circle_root_layout.setOnClickListener { viewModel.isZoom.value = false }
        activity_list.children.forEach {
            (it as? DragActivityView)?.onTouch = { x, y, activityComponent, touchFinish ->
                circlePresenter.onActivityTouch(x, y, activityComponent, touchFinish)
            }
        }

        viewModel.isZoom.observe(viewLifecycleOwner) {
            activity_list.isVisible = it
        }
    }
}