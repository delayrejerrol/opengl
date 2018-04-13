package com.project.jerrol.coinfallingcpp.opengl

import android.content.Context
import android.content.res.AssetManager
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by jerro on 3/6/2018.
 */
class CoinRenderer(private val context: Context) : GLSurfaceView.Renderer {

    override fun onDrawFrame(gl: GL10?) {
        nativeDrawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        nativeSurfaceChange(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val assetManager = context.assets
        nativeSurfaceCreate(assetManager)
    }

    external fun nativeSurfaceCreate(assetManager: AssetManager)
    external fun nativeSurfaceChange(width: Int, height: Int)
    external fun nativeDrawFrame()
    external fun drawNewCoin(coinSize: Int)
    external fun nativeClearSurface(isClearSurface: Boolean)

    companion object {
        init {
            System.loadLibrary("coin-lib")
        }
    }
}