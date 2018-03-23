package com.project.jerrol.coinfallingkotlin.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import com.project.jerrol.coinfallingkotlin.ICoinAnimationListener
import com.project.jerrol.coinfallingkotlin.R
import com.project.jerrol.coinfallingkotlin.model.Coin
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList

/**
 * Created by jerro on 3/6/2018.
 */
class CoinRenderer(val context: Context) : GLSurfaceView.Renderer {

    companion object {
        // Scaling value
        private var sScaleValue = 1.0f
        private var sScaleX = 1.0f
        private var sScaleY = 1.0f
        private var sWidthPixel = 320.0f
        private var sHeightPixel = 480.0f

        @JvmStatic
        fun compileShader(shaderType: Int, shaderSource: String): Int {
            var shaderHandle = GLES20.glCreateShader(shaderType)

            if (shaderHandle != 0) {
                // Pass in the shader source
                GLES20.glShaderSource(shaderHandle, shaderSource)

                // Compile the shader
                GLES20.glCompileShader(shaderHandle)

                // Get the compilation status
                val compileStatus = IntArray(1)
                GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

                // If the compilation failed, delete the shader
                if (compileStatus[0] == 0) {
                    Log.e("CoinRenderer", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
                    GLES20.glDeleteShader(shaderHandle)
                    shaderHandle = 0
                }
            }

            if (shaderHandle == 0) {
                throw RuntimeException("Error creating shader")
            }

            return shaderHandle
        }

        @JvmStatic
        fun createAndLinkProgram(vertexShader: Int, fragmentShader: Int, attributes: ArrayList<String>?): Int {
            var programHandle = GLES20.glCreateProgram()

            if (programHandle != 0) {
                // Bind the vertex shader to the program
                GLES20.glAttachShader(programHandle, vertexShader)

                // Bind the fragment shader to the program
                GLES20.glAttachShader(programHandle, fragmentShader)

                // Bind attributes
                if (attributes != null) {
                    for (i in 0..attributes.size) {
                        GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                    }
                }

                // Link the two shaders together in a program
                GLES20.glLinkProgram(programHandle)

                // Get the link status
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

                // if the link failed, delete the program
                if (linkStatus[0] == 0) {
                    Log.e("CoinRenderer", "Error compiling program: " + GLES20.glGetShaderInfoLog(programHandle))
                    GLES20.glDeleteProgram(programHandle)
                    programHandle = 0
                }
            }

            if (programHandle == 0) {
                throw RuntimeException("Error creating a program")
            }

            return programHandle
        }


        @JvmStatic
        fun readTextFileFromRawResources(context: Context, resourceId: Int): String {
            val inputStream = context.resources.openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var nextLine: String? = null
            val body = StringBuilder()

            try {
                nextLine = bufferedReader.readLine()
                while ((nextLine) != null) {

                    body.appendln(nextLine)
                    //body.append(System.getProperty("line.separator"))
                    nextLine = bufferedReader.readLine()
                }
            } catch (e: IOException) {
                return null.toString()
            }

            return body.toString()
        }
    }

    // Our matrices
    private val mMatrixProjection = FloatArray(64)
    private val mMatrixView = FloatArray(64)
    private val mMatrixProjectionView = FloatArray(64)

    // Our Screen Resolution
    private var mScreenWidth = 1280f
    private var mScreenHeight = 768f

    // The coin collection
    private var mCoinCollection: ArrayList<Coin> = ArrayList()

    // The coin faces
    private val mCoinFaces = intArrayOf(
            R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    )

    // The coin faces holder
    private var mCoinBitmaps = arrayOfNulls<Bitmap>(8)

    // The coin textures
    private var mTextures = IntArray(8)
    private var mTextureId: Int = 0

    private var mProgramHandle = 0
    private var mImageProgramHandle = 0
    private var mCurrentTime = 0

    private var mClearSurface = false

    // GL Handle
    private var mPositionHandle = 0
    private var mTexCoord = 0
    private var mMatrixHandle = 0
    private var mSamplerLoc = 0

    init {
        // createCoinBitmaps()
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear screen and depth buffer,
        // we have set the clear color as black transparent
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        if (!isClearSurface()) drawCoin()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mScreenWidth = width.toFloat()
        mScreenHeight = height.toFloat()

        //Redo the Viewport, making it fullscreen
        GLES20.glViewport(0, 0, mScreenWidth.toInt(), mScreenHeight.toInt())

        // Clear our matrices
        for (i in 0 until 64) {
            mMatrixProjection[i] = 0.0f
            mMatrixView[i] = 0.0f
            mMatrixProjectionView[i] = 0.0f
        }

        // Setup our screen width and height for normal coin translation
        Matrix.orthoM(mMatrixProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0f, 50f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mMatrixView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMatrixProjectionView, 0, mMatrixProjection, 0, mMatrixView, 0)

        // Set camera to the upper top of the screen
        // Matrix.translateM(mMatrixProjectionView, 0, 0.0f, mScreenHeight, 0.0f)

        setupScaling()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Setup scaling
        setupScaling()

        // Set the clear color to transparent black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        var vertexShader = readTextFileFromRawResources(context, R.raw.color_vertex_shader)
        var fragmentShader = readTextFileFromRawResources(context, R.raw.color_fragment_shader)

        var vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        var fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        mProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, null)

        vertexShader = readTextFileFromRawResources(context, R.raw.image_vertex_shader)
        fragmentShader = readTextFileFromRawResources(context, R.raw.image_fragment_shader)

        vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        mImageProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, null)

        // Get handle to vertex shader's vPosition number
        mPositionHandle = GLES20.glGetAttribLocation(mImageProgramHandle, "vPosition")
        // Get handle to texture coordinates location
        mTexCoord = GLES20.glGetAttribLocation(mImageProgramHandle, "a_texCoord")
        // Get handle to shape's transformation matrix
        mMatrixHandle = GLES20.glGetUniformLocation(mImageProgramHandle, "uMVPMatrix")
        // Get handle to textures locations
        mSamplerLoc = GLES20.glGetUniformLocation(mImageProgramHandle, "s_texture")

        // Generate textures
        for (i in mCoinFaces.indices) {
            mTextures[i] = loadGLTexture(i)
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        }
    }

    fun createCoinBitmaps() {
        val options = BitmapFactory.Options()
        options.inScaled = false

        for (i in mCoinFaces.indices) {
            mCoinBitmaps[i] = BitmapFactory.decodeResource(context.resources, mCoinFaces[i], options)
        }
    }

    fun drawCoin() {
        //GLES20.glUseProgram(mProgramHandle)
        GLES20.glUseProgram(mImageProgramHandle)

        val elapseRealtime = SystemClock.elapsedRealtime() % 100000L
        Log.i("CoinRenderer", "elapseRealtime: $elapseRealtime")
        //val slowTime = SystemClock.uptimeMillis() % 100000L
        //Log.i("CoinRenderer", "slowTime: $slowTime")
        // translate each coin every 1 sec
        val translateCoinPositionY = (0.01f) * (elapseRealtime.toInt())
        val elapse = translateCoinPositionY.toInt()

        Log.i("CoinRenderer", "Elapse: $elapse")
        for (coin in mCoinCollection) {
            if (mCurrentTime < elapse) {
                val nextCoinFace = coin.getNextCoinFace(coin.getCurrentCoinFace())
                //coin.setTextureId(updateGLTexture(coin.getTextureId(), nextCoinFace))
                //coin.setTextureId(updateGLTexture(coin.getTextureId(), mTextures[nextCoinFace]))
                coin.setTextureId(coin.getTextureId())
                coin.translate(0.0f, -(25.0f * sScaleValue))
            }
            /*coin.Render(mMatrixProjectionView, coin.getTextureId(),
                    mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc)*/

            coin.Render(mMatrixProjectionView, coin.getCurrentTextureId(),
                    mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc)
        }

        if (!hasCoinVisible()) {
            Log.i("Coins", "All coins y < 0")
            //GLES20.glDeleteProgram(mProgramHandle)
            GLES20.glDeleteProgram(mImageProgramHandle)
            val listener = context as ICoinAnimationListener
            listener.onTranslateYComplete(true)
        }

        mCurrentTime = elapse
    }

    fun drawNewCoin(totalCoin: Int) {
        // Initiate our coins with random bitmaps and increase distance
        val random = Random()
        for (i in 0 until totalCoin) {
            val currentCoinFace = i.rem(mCoinFaces.size)

            val pointX = random.nextInt(mScreenWidth.toInt()).toFloat()
            var pointY = random.nextInt(mScreenHeight.toInt()).toFloat()

            val coin = Coin(currentCoinFace, pointX, pointY + mScreenHeight, sScaleValue)
            // coin.setTextureId(loadGLTexture(i, currentCoinFace))
            // coin.setTextureId(mTextures[currentCoinFace])
            coin.setTextures(mTextures)

            mCoinCollection.add(coin)
        }
    }

    fun getTextureId(): Int {
        if (mTextureId == 7) {
            mTextureId = 0
        } else {
            mTextureId++
        }
        return mTextureId
    }

    fun isClearSurface(): Boolean {
        return mClearSurface
    }

    fun loadGLTexture(coinFaceIndex: Int): Int {
        val textureHandle = IntArray(1)

        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture name.")
        }

        val options = BitmapFactory.Options()
        options.inScaled = false

        val bitmap = BitmapFactory.decodeResource(context.resources, mCoinFaces[coinFaceIndex], options)

        // Bind texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        // Set wrapping mode
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        /*for (i in mCoinFaces.indices) {
            mCoinBitmaps[i] = BitmapFactory.decodeResource(context.resources, mCoinFaces[i], options)
        }*/

        // Load the bitmap to the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        return textureHandle[0]
    }

    fun loadGLTexture(textureIndex: Int, coinFaceIndex: Int): Int {
        val textureHandle = IntArray(1)

        GLES20.glGenTextures(1, textureHandle, 0)

        // Bind texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex)

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Load the bitmap to the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mCoinBitmaps[coinFaceIndex], 0)

        return textureIndex
    }

    fun setClearSurface(clearSurface: Boolean) {
        if (clearSurface) {
            mCoinCollection.clear()
        }
        this.mClearSurface = clearSurface
    }

    fun setupScaling() {
        // The screen resolution
        sWidthPixel = context.resources.displayMetrics.widthPixels.toFloat()
        sHeightPixel = context.resources.displayMetrics.heightPixels.toFloat()

        // Orientation is assumed portrait
        sScaleX = sWidthPixel / 320.0f
        sScaleY = sHeightPixel / 480.0f

        // Get our uniform scalier
        sScaleValue = if (sScaleX > sScaleY) sScaleY else sScaleX
    }

    fun updateGLTexture(textureIndex: Int, coinFaceIndex: Int): Int {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[textureIndex])

        // Set filtering
        // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // Set wrapping mode
        // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mCoinBitmaps[coinFaceIndex], 0)

        return textureIndex
    }

    private fun hasCoinVisible(): Boolean {
        var hasCoinVisible = false
        for (coin in mCoinCollection) {
            if (coin.getY() > 0) {
                hasCoinVisible = true
                break
            }
        }

        return hasCoinVisible
    }
}