package com.project.jerrol.coinfalling.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.project.jerrol.coinfalling.R;
import com.project.jerrol.coinfalling.common.RawResourceReader;
import com.project.jerrol.coinfalling.common.ShaderHelper;
import com.project.jerrol.coinfalling.common.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jerro on 2/26/2018.
 */

public class CoinGL extends GLSurfaceView {

    private CoinRenderer mRenderer;

    public CoinGL(Context context, int coinCount) {
        super(context);

        setEGLContextClientVersion(2);

        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        mRenderer = new CoinRenderer(context, coinCount);
        setRenderer(mRenderer);

        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onResumeGL(final int coinCount) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.onResumeRender(coinCount);
                mRenderer.setHasCoinVisible(true);
            }
        });
    }

    public void onPauseGL() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setHasCoinVisible(false);
                mRenderer.onPauseRender();
            }
        });
    }
}
