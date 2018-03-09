package com.project.jerrol.coinfalling.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.Log;

import com.project.jerrol.coinfalling.R;
import com.project.jerrol.coinfalling.common.RawResourceReader;
import com.project.jerrol.coinfalling.common.ShaderHelper;
import com.project.jerrol.coinfalling.common.riGraphicTools;
import com.project.jerrol.coinfalling.interfaces.CoinListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;

/**
 * Created by jerro on 3/1/2018.
 */

public class Coins {
    private Context mContext;

    // private Coin[] mCoins;
    private ArrayList<Coin> mCoins;

    // The maximum coin collection
    private int mCoinMaxCount = 7;
    // Drawables of coin.
    private int[] mCoinCollection = {
            R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    };

    // Alter this number if more bitmaps needed.
    Bitmap[] bitmaps = new Bitmap[8];

    private int currentTime;

    float mWidth;
    float mHeight;

    public Coins(Context context) {
        mContext = context;
        mCoins = new ArrayList<>();

        createBitmaps();
    }

    public void setScreenSize(float width, float height) {
        mWidth = width;
        mHeight = height;
    }

    public void initializeCoin(int coinCount) {
        // mCoinCount = coinCount;

        // Initiate our coins with random bitmaps and increase distance
        Random random = new Random();

        int currentCoin = 0;
        for (int count = 0; count < coinCount; count++) {
            // int currentCoin = random.nextInt(mCoinMaxCount);

            if (currentCoin > 7) currentCoin = 0;
            // Generate a position between -5 to 5
            //float coinPosition = random.nextInt(10);

            // Generate points between 50 to width and 50 to height
            //float pointX = ((float) random.nextInt((int) mWidth - 50) + 50);
            //float pointY = ((float) random.nextInt((int) mHeight - 50) + 50);
            float pointX = ((float) random.nextInt((int) mWidth));
            float pointY = ((float) random.nextInt((int) mHeight));

            Coin coin = new Coin(currentCoin, pointX, pointY);
            coin.setTextureId(loadGLTexture(currentCoin));
            // coin.distance

            mCoins.add(coin);
            //mCoins[count] = new Coin(currentCoin, pointX, pointY);
            //mCoins[count].textureId = loadGLTexture(currentCoin);

            //mCoins[count].distance = ((float)count / coinCount) * 30.0f;

            //mCoins[count].position = coinPosition;
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
        // GLES20.glUseProgram(riGraphicTools.sp_Image);
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

    public int deleteTexture() {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        GLES20.glDeleteTextures(1, textureHandle, 0);
        // Bind texture to texturename
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        return textureHandle[0];
    }

    public void draw(float[] m) {
        GLES20.glUseProgram(riGraphicTools.sp_SolidColor);
        GLES20.glUseProgram(riGraphicTools.sp_Image);

        long time = SystemClock.uptimeMillis() % 10000L;
        long slowTime = SystemClock.uptimeMillis() % 100000L;

        // translate coin position every 1 sec.
        float slowTranslateY = (0.01f) * ((int) slowTime);

        int elapse = ((int) slowTranslateY);

        for (Coin coin : mCoins) {
            if (currentTime < elapse) {
                int currentCoin = coin.getNextCoin(coin.getCurrentCoin());
                coin.textureId = updateGLTexture(currentCoin);
                coin.translate(0.0f, -20f);

                // Log.i("Coins", "Coin #: " + loop + " Y:" + coin.translation.y);
            }
            coin.Render(m, coin.textureId);
        }

        if (!hasCoinVisible()) {
            Log.i("Coins", "All coins y < 0");
            CoinListener listener = (CoinListener) mContext;
            listener.onCoinFallingComplete(true);
        }

        currentTime = elapse;
    }

    public boolean hasCoinVisible() {
        boolean isCoinVisible = false;
        for (Coin coin : mCoins) {
            if (coin.translation.y > (-50.0f - mHeight)) {
                isCoinVisible = true;
                break;
            }
        }

        return isCoinVisible;
    }

    public void restartAnimation(int coinCount) {
        initializeCoin( coinCount);
        //loadVertexAndFragmentShader();
    }

    public void stopAnimation() {
        mCoins.clear();
    }
}
