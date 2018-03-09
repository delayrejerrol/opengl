package com.project.jerrol.coinfallingkotlin

/**
 * Created by jerro on 3/6/2018.
 */
interface ICoinAnimationListener {
    fun onRedrawRemainingCoin(remainingCoin: Int)
    fun onTranslateYComplete(isComplete: Boolean)
}