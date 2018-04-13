package com.delayre.jerrol.sampleopengl

import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

    val renderer = object : GLSurfaceView.Renderer {
        override fun onDrawFrame(gl: GL10?) {
            nativeOnDrawFrame()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            nativeOnChanged(width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            nativeOnCreate()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView.setEGLContextClientVersion(2)

        glSurfaceView.setRenderer(renderer)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun nativeOnCreate()
    external fun nativeOnChanged(width: Int, height: Int)
    external fun nativeOnDrawFrame()

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
