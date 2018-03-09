package com.project.jerrol.coinfalling.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.project.jerrol.coinfalling.model.Coins;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jerro on 2/28/2018.
 */

public class CoinRenderer implements GLSurfaceView.Renderer {
    private Context mContext;

    // Scaling value
    static float ssu = 1.0f;
    static float ssx = 1.0f;
    static float ssy = 1.0f;
    static float swp = 320.0f;
    static float shp = 480.0f;

    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Our screenresolution
    float mScreenWidth = 1280;
    float mScreenHeight = 768;

    // Our coins collection
    private Coins mCoins;

    private boolean hasCoinVisible;
    private int coinCount;

    public CoinRenderer(Context context, int coinCount) {
        this.mContext = context;
        this.coinCount = coinCount;
    }

    /**
     * The Surface is created/init()
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCoins = new Coins(mContext);

        // Setup our scaling system
        SetupScaling();
        // mCoins.loadGLTexture();

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // mCoins.loadVertexAndFragmentShader();
    }

    /**
     * Here we do our drawing
     */
    public void onDrawFrame(GL10 gl) {
        // clear Screen and Depth Buffer,
        // we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        if (isHasCoinVisible())
            mCoins.draw(mtrxProjectionAndView);
    }


    /**
     * If the surface changes, reset the view
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        mCoins.setScreenSize(mScreenWidth, mScreenHeight);
        mCoins.initializeCoin(coinCount);
        mCoins.loadVertexAndFragmentShader();

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        // Clear our matrices
        for(int i=0;i<4;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

        Matrix.translateM(mtrxProjectionAndView, 0, 0.0f, mScreenHeight, 0.0f);
        SetupScaling();
    }

    public void SetupScaling()
    {
        // The screen resolutions
        swp = (int) (mContext.getResources().getDisplayMetrics().widthPixels);
        shp = (int) (mContext.getResources().getDisplayMetrics().heightPixels);

        // Orientation is assumed portrait
        ssx = swp / 320.0f;
        ssy = shp / 480.0f;

        // Get our uniform scaler
        if(ssx > ssy)
            ssu = ssy;
        else
            ssu = ssx;
    }

    public void onPauseRender() {
         /* Do stuff to pause the renderer */
         if (mCoins != null) {
             Log.i("CoinRenderer", "onPauseRenderer called");
             mCoins.stopAnimation();
         }
    }

    public void onResumeRender(int coinCount) {
        /* Do stuff to resume the renderer */
        mCoins.restartAnimation(coinCount);
    }

    public void setHasCoinVisible(boolean isCoinVisible) {
        hasCoinVisible = isCoinVisible;
    }

    public boolean isHasCoinVisible() {
        return hasCoinVisible;
    }
}
