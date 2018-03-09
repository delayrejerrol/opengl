package com.project.jerrol.coinfallingcpp.opengl

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView

/**
 * Created by jerro on 3/6/2018.
 */
class CoinGL(context: Context) : GLSurfaceView(context) {

    private var mRenderer : CoinRenderer? = null

    init {
        // Set openGL version
        setEGLContextClientVersion(2)

        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)

        mRenderer = CoinRenderer(context)
        setRenderer(mRenderer)

        renderMode = RENDERMODE_CONTINUOUSLY
    }
}