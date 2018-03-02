package com.project.jerrol.nehetutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jerro on 2/26/2018.
 */

public class Coins {

    private Context mContext;

    private int num = 1;					//Basic number of coins
    private Coin[] coins;					//Hold all our coin instances in this array

    private Random rand = new Random();		//Initiate Random for random values of coins

    private float zoom = -15.0f;			//Distance Away From coins
    private float tilt = 90.0f;				//Tilt The View
    private float spin;						//Spin coins

    /** Our texture pointer */
    private int numFaces;
    private int[] textures = new int[8];

    private int[] textureIDs;
    private int[] coins_images = {
      R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    };

    private Bitmap[] bitmaps = new Bitmap[8];

    /**
     * Constructor for our coins holder
     * with the number of maximum coins.
     * Initiate all coins with random
     * numbers.
     *
     * @param num - Number of coins
     */
    public Coins(Context context, int num) {
        this.mContext = context;
        this.num = num;

        //Initiate the stars array
        coins = new Coin[num];

        Random random = new Random();
        float min = -5.0f;
        float max = 5.0f;
        int z = 0;
        //Initiate our stars with random colors and increasing distance
        for(int loop = 0; loop < num; loop++) {
            int number = random.nextInt(7);
            //bitmaps[loop] = getBitmap(context, number);
            coins[loop] = new Coin(number);
            coins[loop].angle = 0.0f;
            coins[loop].dist = ((float) loop / num) * 30.0f;

            z = random.nextInt(10) - 5;

            Log.i("Coins", "z: " + z);
            coins[loop].pos = (float) z;

            //textureIDs = coins[loop].textureIDs;
            //bitmaps = coins[loop].bitmaps;
            //numFaces = coins[loop].numFaces;
        }
        generateBitmaps();
    }

    /**
     * The Coins drawing function.
     *
     * @param gl - The GL Context
     * @param twinkle - Twinkle on or off
     */
    public void draw(GL10 gl, boolean twinkle) {
        // Bind the coin texture for all coins
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        //Iterate through all stars
        for(int loop = 0; loop < num; loop++) {
            //Recover the current star into an object
            Coin coin = coins[loop];

            gl.glLoadIdentity(); //Reset The Current Modelview Matrix

            gl.glPushMatrix();
            gl.glTranslatef(0.0f, 0.0f, -15.0f); //Zoom Into The Screen (Using The Value In 'zoom')
            //gl.glTranslatef(coin.pos, coin.dist, -15.0f);
            //gl.glRotatef(spin, 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(coin.pos, coin.dist, -15.0f); // Move downward
            coin.draw(gl);
            gl.glPopMatrix();
            spin += 0.2f;
            coin.dist -= 0.04f;

            //changeTexture(gl, mContext, coin.loadNextCoin(coin.getCurrentCoin()));
            //coin.draw(gl);
            //Distance zero...
            if(coin.dist < 0.0f) {
                //Set back to a five distance
                coin.dist = 30.0f;
            }
            if (spin > 0.9f) {
                spin = 0.0f;
            }
        }
    }

    /**
     * Load the textures
     *
     * @param gl - The GL Context
     */
    public void loadGLTexture(GL10 gl, Context context) {
        /*//Get the texture from the Android resource directory
        InputStream is = context.getResources().openRawResource(coins_images[0]);
        Bitmap bitmap = null;
        try {
            //BitmapFactory is an Android graphics utility for images
            bitmap = BitmapFactory.decodeStream(is);

        } finally {
            //Always clear and close
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }

        //Generate there texture pointer
        gl.glGenTextures(1, textures, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        //Clean up
        bitmap.recycle();*/

        gl.glGenTextures(1, textures, 0);
        for (int i = 0; i < bitmaps.length; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmaps[i], 0);
        }
    }

    public void updateTexture(GL10 gl, Bitmap bitmap, int coinIndex) {
        //Create Linear Filtered Texture and bind it to texture
        // gl.glGenTextures(1, textures, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[coinIndex]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        //Clean up
        //bitmap.recycle();
    }

    public void loadTexture(int textureId) {

    }

    private void generateBitmaps() {
        for (int i = 0; i < coins_images.length; i++) {
            bitmaps[i] = getBitmap(mContext, i);
        }
    }

    private Bitmap getBitmap(Context context, int currentCoin) {
        //Get the texture from the Android resource directory
        InputStream is = context.getResources().openRawResource(coins_images[currentCoin]);
        Bitmap bitmap = null;
        try {
            //BitmapFactory is an Android graphics utility for images
            bitmap = BitmapFactory.decodeStream(is);

        } finally {
            //Always clear and close
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }

        return bitmap;
    }
}
