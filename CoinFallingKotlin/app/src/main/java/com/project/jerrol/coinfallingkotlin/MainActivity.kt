package com.project.jerrol.coinfallingkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import com.project.jerrol.coinfallingkotlin.opengl.CoinGL
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ICoinAnimationListener  {

    private var mCoinGL: CoinGL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCoinGL = CoinGL(this)
        setContentView(R.layout.activity_main)

        // Retrieve our Relative layout from our main layout we just set to our view
        val relativeLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        main_layout.addView(mCoinGL, relativeLayoutParams)

        button.setOnClickListener({
            mCoinGL?.dirtSurface(editText.text.toString().toInt())
        })
    }

    override fun onRedrawRemainingCoin(remainingCoin: Int) {
        // Redraw remaining coin every 20 each
        mCoinGL?.dirtSurface(remainingCoin)
    }

    override fun onTranslateYComplete(isComplete: Boolean) {
        if (isComplete) {
            Log.i("MainActivity", "onTranslateYComplete called")
            mCoinGL?.clearSurface()
        }
    }
}
