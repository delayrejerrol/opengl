package com.example.jerro.sampleopengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;

/**
 * Created by jerro on 2/22/2018.
 */

public class GLRenderer implements GLSurfaceView.Renderer {

    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Geometric variables
    public static float vertices[];
    public static float vertices2[];
    public static short indices[];
    public static short indices2[];
    public static float uvs[];
    public static float uvs2[];
    public FloatBuffer vertexBuffer;
    public FloatBuffer vertexBuffer2;
    public ShortBuffer drawListBuffer;
    public ShortBuffer drawListBuffer2;
    public FloatBuffer uvBuffer;
    public FloatBuffer uvBuffer2;

    // Our screenresolution
    float   mScreenWidth = 1280;
    float   mScreenHeight = 768;

    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;

    // Generate Textures, if more needed, alter these numbers.
    int[] textures = new int[8];
    String[] textureName = {
      "coin_1", "coin_2", "coin_3", "coin_4",
            "coin_5", "coin_6", "coin_7", "coin_8",
    };
    int currentIndex;

    public Sprite sprite;
    public Sprite sprite2;

    float 	ssu = 1.0f;
    float 	ssx = 1.0f;
    float 	ssy = 1.0f;
    float 	swp = 320.0f;
    float 	shp = 480.0f;

    public float distance = 30.0f;

    int texture1;
    int texture2;

    public GLRenderer(Context c)
    {
        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;

        sprite = new Sprite();
        sprite2 = new Sprite(5.0f);
    }

    public void onPause()
    {
        /* Do stuff to pause the renderer */
    }

    public void onResume()
    {
        /* Do stuff to resume the renderer */
        mLastTime = System.currentTimeMillis() + 100;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Setup our scaling system
        SetupScaling();
        // Create the triangles
        SetupTriangle();
        // Create the image information
        SetupImage();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // Create the triangles
        SetupTriangle2();
        // Create the image information
        SetupImage2();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        // Clear our matrices
        for(int i=0;i<16;i++)
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

        //Matrix.translateM(mtrxProjectionAndView, 0, 0.0f, mScreenHeight / 2, 0.0f);
        // Setup our scaling system
        SetupScaling();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Get the current time
        long now = System.currentTimeMillis();

        // We should make sure we are valid and sane
        if (mLastTime > now) return;

        // Get the amount of time the last frame took.
        long elapsed = now - mLastTime;

        //Log.i("GLRenderer", "mLastTime: " + mLastTime);
        //Log.i("GLRenderer", "elapsed: " + elapsed);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // Update our example
        if (elapsed % 4 == 0) {
            //sprite.translate(-1f*ssu, 0);
           // gl.glTranslatef(0.0f, distance, 0.0f);
            //Matrix.translateM(mtrxProjectionAndView, 0, 0.0f, -2.0f, 0.0f);
            //UpdateSprite();
        }
        //distance -= 0.02f;
        // Log.i("GLRenderer", "Elapse: " + elapsed);

        // UpdateSprite();
        // Render our example
        Render(mtrxProjectionAndView);

        Render2(mtrxProjectionAndView);

        //long time = SystemClock.uptimeMillis() % 10000L;
        //float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        //Matrix.rotateM(mtrxProjectionAndView, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        // Save the current time to see how long it took <img src="http://androidblog.reindustries.com/wp-includes/images/smilies/icon_smile.gif" alt=":)" class="wp-smiley"> .
        mLastTime = now;
    }

    private void Render(float[] m) {
        // clear Screen and Depth Buffer,
        // we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1);

        // get handle to vertex shader's vPosition member
        int mPositionHandle =
                GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image,
                "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image,
                "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image,
                "s_texture" );

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    private void Render2(float[] m) {
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2);
        // clear Screen and Depth Buffer,
        // we have set the clear color as black.
        // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // get handle to vertex shader's vPosition member
        int mPositionHandle =
                GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer2);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image,
                "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer2);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image,
                "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image,
                "s_texture" );

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices2.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer2);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    public void SetupTriangle()
    {
        // Get information of sprite.
        vertices = sprite.getTransformedVertices();

        // The order of vertexrendering for a quad
        indices = new short[] {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void SetupTriangle2()
    {
        // Get information of sprite.
        vertices2 = sprite2.getTransformedVertices();

        // The order of vertexrendering for a quad
        indices2 = new short[] {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices2.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer2 = bb.asFloatBuffer();
        vertexBuffer2.put(vertices2);
        vertexBuffer2.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices2.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer2 = dlb.asShortBuffer();
        drawListBuffer2.put(indices2);
        drawListBuffer2.position(0);
    }

    public void SetupImage()
    {
        // Create our UV coordinates.
        uvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        Random random = new Random();
        currentIndex = random.nextInt(7);
        // Retrieve our image from resources.

        int id = mContext.getResources().getIdentifier("drawable/" + textureName[0], null,
                mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        // Bind texture to texturename
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();

        texture1 = textureHandle[0];
    }

    public void SetupImage2()
    {
        // Create our UV coordinates.
        uvs2 = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs2.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer2 = bb.asFloatBuffer();
        uvBuffer2.put(uvs2);
        uvBuffer2.position(0);

        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        Random random = new Random();
        currentIndex = random.nextInt(7);
        // Retrieve our image from resources.

        int id = mContext.getResources().getIdentifier("drawable/" + textureName[2], null,
                mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        // Bind texture to texturename
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();

        texture2 = textureHandle[0];
    }

    public void UpdateSprite()
    {
        /*// Get new transformed vertices
        vertices = sprite.getTransformedVertices();*/
        // Get information of sprite.
        vertices = sprite.getTransformedVertices();

        // The order of vertexrendering for a quad
        indices = new short[] {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        currentIndex++;
        if (currentIndex > 7) {
            currentIndex = 0;
        }
        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/" + textureName[0], null,
                mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        loadTexture(bmp, currentIndex);
    }

    public void processTouchEvent(MotionEvent event)
    {
        // Get the half of screen value
        int screenhalf = (int) (mScreenWidth / 2);
        int screenheightpart = (int) (mScreenHeight / 3);
        if(event.getX()<screenhalf)
        {
            // Left screen touch
            if(event.getY() < screenheightpart)
                sprite.scale(-0.01f);
            else if(event.getY() < (screenheightpart*2))
                sprite.translate(-10f*ssu, -10f*ssu);
            else
                sprite.rotate(0.01f);
        }
        else
        {
            // Right screen touch
            if(event.getY() < screenheightpart)
                sprite.scale(0.01f);
            else if(event.getY() < (screenheightpart*2))
                sprite.translate(10f*ssu, 10f*ssu);
            else
                sprite.rotate(-0.01f);
        }
    }

    class Sprite {
        float angle;
        float scale;
        RectF base;
        PointF translation;

        public Sprite() {
            // Initialise our intital size around the 0,0 point
            base = new RectF(-50f*ssu, 50f*ssu, 50f*ssu, -50f*ssu);

            // Initial translation
            translation = new PointF(50f*ssu,50f*ssu);

            // We start with our inital size
            scale = 1f;

            // We start in our inital angle
            angle = 0f;
        }

        public Sprite(float point) {
            // Initialise our intital size around the 0,0 point
            base = new RectF(-50f*ssu, 50f*ssu, 50f*ssu, -50f*ssu);

            // Initial translation
            translation = new PointF(50f*point,50f*point);

            // We start with our inital size
            scale = 1f;

            // We start in our inital angle
            angle = 0f;
        }


        public void translate(float deltax, float deltay) {
            // Update our location.
            translation.x += deltax;
            //translation.y += deltay;
        }

        public void scale(float deltas) {
            scale += deltas;
        }

        public void rotate(float deltaa) {
            angle += deltaa;
        }

        public float[] getTransformedVertices() {
            // Start with scaling
            float x1 = base.left * scale;
            float x2 = base.right * scale;
            float y1 = base.bottom * scale;
            float y2 = base.top * scale;

            // We now detach from our Rect because when rotating,
            // we need the seperate points, so we do so in opengl order
            PointF one = new PointF(x1, y2);
            PointF two = new PointF(x1, y1);
            PointF three = new PointF(x2, y1);
            PointF four = new PointF(x2, y2);

            // We create the sin and cos function once,
            // so we do not have calculate them each time.
            float s = (float) Math.sin(angle);
            float c = (float) Math.cos(angle);

            // Then we rotate each point
            one.x = x1 * c - y2 * s;
            one.y = x1 * s + y2 * c;
            two.x = x1 * c - y1 * s;
            two.y = x1 * s + y1 * c;
            three.x = x2 * c - y1 * s;
            three.y = x2 * s + y1 * c;
            four.x = x2 * c - y2 * s;
            four.y = x2 * s + y2 * c;

            // Finally we translate the sprite to its correct position.
            one.x += translation.x;
            one.y += translation.y;
            two.x += translation.x;
            two.y += translation.y;
            three.x += translation.x;
            three.y += translation.y;
            four.x += translation.x;
            four.y += translation.y;

            // We now return our float array of vertices.
            return new float[]
                    {
                            one.x, one.y, 0.0f,
                            two.x, two.y, 0.0f,
                            three.x, three.y, 0.0f,
                            four.x, four.y, 0.0f,
                    };
        }
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

    private void loadTexture(Bitmap bitmap, int textureIndex) {
        //GLES20.glGenTextures ( 1, textures, textureIndex );

        //GLES20.glActiveTexture( textures[textureIndex] );
        GLES20.glBindTexture ( GLES20.GL_TEXTURE_2D, textures[textureIndex] );

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }

    private void loadTexture2(int textureIndex) {
        GLES20.glActiveTexture( textures[textureIndex] );
        GLES20.glBindTexture ( GLES20.GL_TEXTURE_2D, textures[textureIndex] );
    }
}
