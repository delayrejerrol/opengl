package com.project.jerrol.coinfalling.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.project.jerrol.coinfalling.R;
import com.project.jerrol.coinfalling.common.riGraphicTools;
import com.project.jerrol.coinfalling.interfaces.CoinListener;
import com.project.jerrol.coinfalling.opengl.CoinRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;

/**
 * Created by jerro on 3/1/2018.
 */

public class Coins {
    private Context mContext;

    private Coin[] mCoins;

    // The maximum coin collection
    private int mCoinMaxCount = 7;
    // Drawables of coin.
    private int[] mCoinCollection = {
            R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    };
    // Alter this number if more bitmaps needed.
    Bitmap[] bitmaps = new Bitmap[8];

    // Coin count
    private int mCoinCount;

    private int currentTime;

    float mWidth;
    float mHeight;
    float moveToDown = -0.20f;
    float moveToDownDistance = mHeight;

    public Coins(Context context) {
        mContext = context;
        createBitmaps();
    }

    public void initializeCoinPosition(int coinCount, float width, float height) {
        mCoinCount = coinCount;

        mHeight = height;

        // Initiate the coins array
        mCoins = new Coin[coinCount];

        // Initiate our coins with random bitmaps and increase distance
        Random random = new Random();

        Log.i("Coins", "Width: " + width);
        Log.i("Coins", "Height: " + height);
        int currentCoin = 0;
        for (int count = 0; count < coinCount; count++) {
            // int currentCoin = random.nextInt(mCoinMaxCount);

            if (currentCoin > 7) currentCoin = 0;
            // Generate a position between -5 to 5
            float coinPosition = random.nextInt(10);

            float pointX = ((float) random.nextInt((int) width - 50) + 50);
            float pointY = ((float) random.nextInt((int) height - 50) + 50);

            mCoins[count] = new Coin(currentCoin, pointX, pointY);
            mCoins[count].textureId = loadGLTexture(currentCoin);

            mCoins[count].distance = ((float)count / coinCount) * 30.0f;

            mCoins[count].position = coinPosition;
            currentCoin++;
        }
    }

    //public void

    private void createBitmaps() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;	// No pre-scaling

        for (int i = 0; i < mCoinCollection.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(mContext.getResources(), mCoinCollection[i], options);
        }
    }

    public void loadVertexAndFragmentShader() {
        // Create the shaders, solid color
        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                riGraphicTools.vs_SolidColor);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                riGraphicTools.fs_SolidColor);

        riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);

        // Create the shaders, images
        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                riGraphicTools.vs_Image);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                riGraphicTools.fs_Image);

        riGraphicTools.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
        GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
        GLES20.glLinkProgram(riGraphicTools.sp_Image);

        // Set our shader programm
        GLES20.glUseProgram(riGraphicTools.sp_Image);
    }

    public int loadGLTexture(int bitmapIndex) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        // Bind texture to texturename
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmaps[bitmapIndex], 0);

        return textureHandle[0];
    }

    public int updateGLTexture(int textureIndex) {
        // Bind texture to texturename
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmaps[textureIndex], 0);

        return textureIndex;
    }

    public void draw(float[] m) {
        long time = SystemClock.uptimeMillis() % 10000L;
        long slowTime = SystemClock.uptimeMillis() % 100000L;
        float slowAngleInDegrees = (360.0f / 100000.0f) * ((int) slowTime);

        int elapse = ((int) slowAngleInDegrees);


        //Log.i("Coins", "elapse: " + elapse);
        //Log.i("Coins", "currentTime: " + currentTime);

        for (int loop = 0; loop < mCoinCount; loop++) {
            //Recover the current coin into an object
            Coin coin = mCoins[loop];

            //Matrix.setIdentityM(m, 0);
            Matrix.translateM(m, 0, 0.0f, 0.0f, 0.0f);
            Matrix.translateM(m, 0, 0.0f, 0.0f, 0.0f);

            //coin.draw(m);

            //coin.distance -= 0.02f;

            // Get the current time

            if (currentTime < elapse) {
                int currentCoin = coin.getNextCoin(coin.getCurrentCoin());
                // Log.i("Coins", "CurrentCoin: " + currentCoin);
                // coin.updateTexture(coin.getNextCoin(currentCoin), bitmaps[currentCoin]);
                //updateGLTexture(currentCoin);
                coin.textureId = updateGLTexture(currentCoin);

            }
            coin.Render(m, coin.textureId);
            coin.translate(500.0f,500.0f);
        }

        /*if (moveToDownDistance < 0.0f) {
            Log.i("Coins", "moveToDownDistance is less than zero " );
            //Matrix.translateM(m, 0, 0.0f, mHeight, 0.0f);
            moveToDownDistance  += 0.40f;

            CoinListener mListener = (CoinListener) mContext;
            mListener.onCoinFallingComplete(true);
        } else {
            moveToDownDistance -= 0.40f;
        }*/
        //moveToDown -= 0.02f;

        currentTime = elapse;
    }
}
