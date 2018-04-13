package com.example.jerro.coinfallingkotlinv2

import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.example.jerro.coinfallingkotlinv2.tool.GLTool.Companion.compileShader
import com.example.jerro.coinfallingkotlinv2.tool.GLTool.Companion.createAndLinkProgram
import com.project.jerrol.coinfallingkotlin.model.Coin
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

    // Scaling value
    private var sScaleValue = 1.0f
    private var sScaleX = 1.0f
    private var sScaleY = 1.0f
    private var sWidthPixel = 320.0f
    private var sHeightPixel = 480.0f

    // Our Screen Resolution
    private var mScreenWidth = 1280f
    private var mScreenHeight = 768f

    // Our matrices
    private val mMatrixProjection = FloatArray(64)
    private val mMatrixView = FloatArray(64)
    private val mMatrixProjectionView = FloatArray(64)

    // GL Handle
    private var mPositionHandle = 0
    private var mTexCoord = 0
    private var mMatrixHandle = 0
    private var mSamplerLoc = 0

    // The coin faces
    private val mCoinFaces = intArrayOf(
            R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    )

    // The coin textures
    private var mTextures = IntArray(8)
    private var mTextureId: Int = 0

    // GL Program handler
    private var mProgramHandle = 0
    private var mImageProgramHandle = 0

    // The coin collection
    private var mCoinCollection: ArrayList<Coin> = ArrayList()

    private var mCurrentTime = 0

    var renderer = object : GLSurfaceView.Renderer {

        private val COLOR_FRAGMENT_SHADER = "precision mediump float; \n" +
                                            "void main() { \n" +
                                            "   gl_FragColor = vec4(0.5,0,0,1); \n" +
                                            "}"

        private val COLOR_VERTEX_SHADER =   "uniform mat4 uMVPMatrix; \n" +
                                            "attribute vec4 vPosition; \n" +
                                            "void main() { \n" +
                                            "    gl_Position = uMVPMatrix * vPosition; \n" +
                                            "}"

        private val IMAGE_FRAGMENT_SHADER = "precision mediump float; \n" +
                                            "varying vec2 v_texCoord; \n" +
                                            "uniform sampler2D s_texture; \n" +
                                            "void main() { \n" +
                                            "   gl_FragColor = texture2D( s_texture, v_texCoord ); \n" +
                                            "}"

        private val IMAGE_VERTEX_SHADER =   "uniform mat4 uMVPMatrix; \n" +
                                            "attribute vec4 vPosition; \n" +
                                            "attribute vec2 a_texCoord; \n" +
                                            "varying vec2 v_texCoord; \n" +
                                            "void main() { \n" +
                                            "   gl_Position = uMVPMatrix * vPosition; \n" +
                                            "   v_texCoord = a_texCoord; \n" +
                                            "}"

        override fun onDrawFrame(gl: GL10?) {
            // Clear screen and depth buffer,
            // we have set the clear color as black transparent
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            if (!coinListener.isClearSurface()) coinListener.onDrawCoin()
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

            var vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, COLOR_VERTEX_SHADER)
            var fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, COLOR_FRAGMENT_SHADER)
            mProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, null)

            vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, IMAGE_VERTEX_SHADER)
            fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, IMAGE_FRAGMENT_SHADER)
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

        fun setupScaling() {
            // The screen resolution
            sWidthPixel = resources.displayMetrics.widthPixels.toFloat()
            sHeightPixel = resources.displayMetrics.heightPixels.toFloat()

            // Orientation is assumed portrait
            sScaleX = sWidthPixel / 320.0f
            sScaleY = sHeightPixel / 480.0f

            // Get our uniform scalier
            sScaleValue = if (sScaleX > sScaleY) sScaleY else sScaleX
        }

        fun loadGLTexture(coinFaceIndex: Int): Int {
            val textureHandle = IntArray(1)

            GLES20.glGenTextures(1, textureHandle, 0)

            if (textureHandle[0] == 0) {
                throw RuntimeException("Error generating texture name.")
            }

            val options = BitmapFactory.Options()
            options.inScaled = false

            val bitmap = BitmapFactory.decodeResource(resources, mCoinFaces[coinFaceIndex], options)

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
    }

    var coinListener = object : ICoinAnimationListener {

        private var mClearSurface = false

        override fun onDrawCoin() {
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
                    coin.translate(0.0f, -(15.0f * sScaleValue))
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
                /*val listener = this as ICoinAnimationListener
                listener.onTranslateYComplete(true)*/
                onTranslateYCompleted(true)
            }

            mCurrentTime = elapse
        }

        override fun onDrawNewCoin(totalCoin: Int) {
            // Initiate our coins with random bitmaps and increase distance
            val random = Random()
            for (i in 0 until totalCoin) {
                val currentCoinFace = i.rem(mCoinFaces.size)

                val min = 20
                val max = mScreenWidth.toInt()
                val pointX = (random.nextInt(max - min) + min).toFloat()
                var pointY = random.nextInt(mScreenHeight.toInt()).toFloat()

                val coin = Coin(currentCoinFace, pointX, pointY + mScreenHeight, sScaleValue)
                // coin.setTextureId(loadGLTexture(i, currentCoinFace))
                // coin.setTextureId(mTextures[currentCoinFace])
                coin.setTextures(mTextures)

                mCoinCollection.add(coin)
            }
        }

        override fun onTranslateYCompleted(isCompleted: Boolean) {
            if (isCompleted) {
                Log.i("MainActivity", "onTranslateYCompleted called")
                clearSurface()
            }
        }

        override fun isClearSurface(): Boolean {
            return mClearSurface
        }

        override fun setClearSurface(isClearSurface: Boolean) {
            if (isClearSurface) {
                mCoinCollection.clear()
            }
            mClearSurface = isClearSurface
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_layout.setEGLContextClientVersion(2)
        main_layout.setZOrderOnTop(true)
        main_layout.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        main_layout.holder.setFormat(PixelFormat.RGBA_8888)
        main_layout.setRenderer(renderer)

        main_layout.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        button.setOnClickListener({
            dirtSurface(editText.text.toString().toInt())
        })
    }

    fun clearSurface() {
        main_layout.queueEvent { coinListener.setClearSurface(true) }
    }

    fun dirtSurface(totalCoin: Int) {
        main_layout.queueEvent({
            /*renderer.drawNewCoin(totalCoin)
            renderer.setClearSurface(false)*/
            //renderer
            coinListener.onDrawNewCoin(totalCoin)
            coinListener.setClearSurface(false)
        })
    }
}
