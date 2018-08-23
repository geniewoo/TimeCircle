package com.example.user.timecircle

import android.content.Context
import android.view.View
import android.widget.LinearLayout

/**
 * Created by SungWoo on 2018-08-23.
 */
class TimeCircleLayout1(context: Context): LinearLayout(context) {
    init {
        id = View.generateViewId()
        addView(getChildView())
    }

    private fun getChildView(): View {
        return UI {

        }.view
    }
}