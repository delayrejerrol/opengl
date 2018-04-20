package com.project.jerrol.coinfallingcpp

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream

class Utils {

    companion object {
        private const val TAG = "Utils"

        @JvmStatic
        fun loadTexture(manager: AssetManager, path: String): Int {
            Log.i(TAG, "loadTexture called")
            val `in`: InputStream?
            try {
                `in` = manager.open(path)
            } catch (e: IOException) {
                e.printStackTrace()
                return -1
            }

            val op = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bmp = BitmapFactory.decodeStream(`in`, null, op)

            // Generate texture ID
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            val textureID = textures[0]

            // create texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            // Set wrapping mode
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)

            // clean up
            try {
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bmp.recycle()
            }
            return textureID
        }

        @JvmStatic
        fun getElapseRealtime(): Int {
            val elapseRealtime = SystemClock.elapsedRealtime() % 100000L
            val translateCoinPositionY = (0.01f) * (elapseRealtime.toInt())

            return translateCoinPositionY.toInt()
        }
    }
}