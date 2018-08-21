package com.example.user.timecircle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log.i
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.editText
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout

/**
 * Created by SungWoo on 2018-08-06.
 */
class TimeCircleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val ui = UI {
            verticalLayout {
                var circleLayout = frameLayout {
//                    var cicleView = ankoView(::CircleView, 0) {
//                    }.lparams(dimen(R.dimen.timeCircle_Length),
//                            dimen(R.dimen.timeCircle_Length)) {
//                        gravity = Gravity.CENTER
//                    }
                }
                for (i in 0 until CIRCLE_NUM) {
                    i("coco","i : $i")
                    val circleView = CircleView(context, i.toFloat())
                    circleLayout.addView(circleView)
                    circleView.setCenter()
                }
                var title = editText {
                    hint = "test!!!!"
                }
            }
        }
        return ui.view
    }
}