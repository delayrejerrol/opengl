package com.project.jerrol.nehetutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jerro on 2/26/2018.
 */

public class Coin {

    public int r, g, b;				//Coins Color
    public float dist;				//Coins Distance From Center
    public float angle;				//Coins Current Angle
    public float pos;

    public int currentCoin;

    /** The buffer holding the vertices */
    private FloatBuffer vertexBuffer;
    /** The buffer holding the texture coordinates */
    private FloatBuffer textureBuffer;

    /** The initial vertex definition */
    private float vertices[] = {
            1.0f, 1.0f, 0.0f,   // Top Right
            -1.0f, 1.0f, 0.0f,  // Top Left
            1.0f, -1.0f, 0.0f,  // Bottom Right
            -1.0f, -1.0f, 0.0f, // Bottom left
    };

    /** The initial texture coordinates (u, v) */
    private float texture[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private float cubeHalfSize = 1.2f;

    private int[] imageFileIDs = {  // Image file IDs
            R.drawable.coin_1, R.drawable.coin_2, R.drawable.coin_3, R.drawable.coin_4,
            R.drawable.coin_5, R.drawable.coin_6, R.drawable.coin_7, R.drawable.coin_8
    };

    public int numFaces = 8;
    public int[] textureIDs = new int[numFaces];
    public Bitmap[] bitmap = new Bitmap[numFaces];

    public Coin(Context context) {
        this.currentCoin = 0;

        //
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        //
        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    public Coin(int currentCoin) {
        this.currentCoin = currentCoin;

        //
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        //
        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    /**
     * The object own drawing function.
     * Called from the renderer to redraw this instance
     * with possible changes in values.
     *
     * @param gl - The GL Context
     */
    public void draw(GL10 gl) {
        //Enable the vertex, texture and normal state
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        //Point to our buffers
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        //Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public void updateTexture(GL10 gl, Bitmap bitmap, int[] textures) {
        // Create Linear Filtered Texture and bind it to texture
        // gl.glGenTextures(1, textures, 0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        //Clean up
        bitmap.recycle();
    }

    public int loadNextCoin() {
        int nextCoin = this.currentCoin;

        if (nextCoin > 7) nextCoin = 0;
        else nextCoin++;

        return nextCoin;
    }

    public int getCurrentCoin() {
        return this.currentCoin;
    }
}
