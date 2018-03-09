package com.project.jerrol.coinfallingcpp.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by jerro on 3/6/2018.
 */
class CoinRenderer(val context: Context) : GLSurfaceView.Renderer {

    override fun onDrawFrame(gl: GL10?) {
        nativeDrawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        nativeSurfaceChange(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        nativeSurfaceCreate()
    }

    external fun nativeSurfaceCreate()
    external fun nativeSurfaceChange(width: Int, height: Int)
    external fun nativeDrawFrame()

    companion object {
        init {
            System.loadLibrary("coin-lib")
        }
    }
}