package com.example.user.timecircle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityView = MainActivityView()
        mainActivityView.setContentView(this)

        supportFragmentManager.beginTransaction().apply {
            replace(mainActivityView.viewId, TimeCircleFragment())
            commit()
        }
    }
}

class MainActivityView : AnkoComponent<MainActivity> {
    val viewId = View.generateViewId()
    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            frameLayout {
                lparams(matchParent, matchParent)
                id = viewId
            }
        }
    }
}