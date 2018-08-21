package com.example.user.timecircle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.setContentView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityView = MainActivityView()
        mainActivityView.setContentView(this)

        supportFragmentManager.beginTransaction().apply {
            val timeCircleFragment = TimeCircleFragment()
            add(mainActivityView.viewId, timeCircleFragment)
            commit()
        }
    }
}

class MainActivityView : AnkoComponent<MainActivity> {
    val viewId = View.generateViewId()
    override fun createView(ui: AnkoContext<MainActivity>): View {
        return ui.apply {
            frameLayout {
                id = viewId
            }
        }.view
    }
}