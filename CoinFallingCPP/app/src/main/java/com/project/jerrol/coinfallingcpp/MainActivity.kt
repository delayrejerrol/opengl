package com.project.jerrol.coinfallingcpp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import com.project.jerrol.coinfallingcpp.opengl.CoinGL
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mCoinGL: CoinGL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCoinGL = CoinGL(this)
        setContentView(R.layout.activity_main)

        // Retrieve our Relative layout from our main layout we just set to our view
        val relativeLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        main_layout.addView(mCoinGL, relativeLayoutParams)

        button.setOnClickListener({
            mCoinGL?.drawNewCoin(editText.text.toString().toInt())
        })
    }
}
