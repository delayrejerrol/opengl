package com.example.jerro.coinfallingkotlinv2

/**
 * Created by jerro on 3/6/2018.
 */
interface ICoinAnimationListener {
    /*fun onRedrawRemainingCoin(remainingCoin: Int)
    fun onTranslateYComplete(isComplete: Boolean)*/
    fun onDrawCoin()
    fun onDrawNewCoin(totalCoin: Int)
    fun onTranslateYCompleted(isCompleted: Boolean)
    fun isClearSurface(): Boolean
    fun setClearSurface(isClearSurface: Boolean)
}