package com.example.user.timecircle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityView = MainActivityView()
        mainActivityView.setContentView(this)

        supportFragmentManager.beginTransaction().apply {
            TimeCircleFragment().apply {
                add(mainActivityView.viewId, this)
                commit()
            }
        }
    }
}

class MainActivityView : AnkoComponent<MainActivity> {
    val viewId = View.generateViewId()
    override fun createView(ui: AnkoContext<MainActivity>): View {
        return ui.apply {
            frameLayout {
                lparams(matchParent, matchParent)
                id = viewId
            }
        }.view
    }
}