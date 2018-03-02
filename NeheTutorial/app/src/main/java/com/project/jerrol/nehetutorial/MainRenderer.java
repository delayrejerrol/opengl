package com.project.jerrol.nehetutorial;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jerro on 2/26/2018.
 */

public class MainRenderer extends GLSurfaceView implements Renderer {

    /*
	 * Number Of Stars To Draw.
	 *
	 * Depending on the power of your machine/phone
	 * you should reduce the number of stars.
	 */
    private final int num = 10;
    // private Stars stars;					//Our Stars class, managing all stars
    private Coins coins;

    /** Is twinkle enabled? */
    private boolean twinkle = false;

    /** Is blending enabled */
    private boolean blend = true;

    /** The Activity Context */
    private Context context;

    /**
     * Set this class as renderer for this GLSurfaceView.
     * Request Focus and set if focusable in touch mode to
     * receive the Input from Screen
     *
     * @param context - The Activity Context
     */
    public MainRenderer(Context context) {
        super(context);

        //Set this as Renderer
        this.setRenderer(this);
        this.setRenderMode(RENDERMODE_CONTINUOUSLY);
        //Request focus
        this.requestFocus();
        this.setFocusableInTouchMode(true);

        //
        this.context = context;
    }

    /**
     * The Surface is created/init()
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Settings
        gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
        gl.glShadeModel(GL10.GL_SMOOTH); 					//Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 			//Black Background
        gl.glClearDepthf(1.0f); 							//Depth Buffer Setup

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        gl.glEnable(GL10.GL_BLEND);							//Enable blending
        gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);		//Set The Blending Function For Translucency

        //Initiate our stars class with the number of stars
        coins = new Coins(context, num);
        //Load the texture for the stars once during Surface creation
        coins.loadGLTexture(gl, context);
    }

    /**
     * Here we do our drawing
     */
    public void onDrawFrame(GL10 gl) {
        //Clear Screen And Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //Check if the blend flag has been set to enable/disable blending
        if(blend) {
            gl.glEnable(GL10.GL_BLEND);			//Turn Blending On
            gl.glDisable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off

        } else {
            gl.glDisable(GL10.GL_BLEND);		//Turn Blending On
            gl.glEnable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off
        }

        //
        coins.draw(gl, twinkle);
    }


    /**
     * If the surface changes, reset the view
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(height == 0) { 						//Prevent A Divide By Zero By
            height = 1; 						//Making Height Equal One
        }

        gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
        gl.glLoadIdentity(); 					//Reset The Projection Matrix

        //Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
        gl.glLoadIdentity(); 					//Reset The Modelview Matrix
    }

    /* ***** Listener Events ***** */
    /**
     * Override the touch screen listener.
     *
     * React to moves and presses on the touchscreen.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //
        float x = event.getX();
        float y = event.getY();

        //A press on the screen
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //Define an upper area of 10% to define a lower area
            int upperArea = this.getHeight() / 10;
            int lowerArea = this.getHeight() - upperArea;

            //
            if(y > lowerArea) {
                //Change the blend setting if the lower area left has been pressed ( NEW )
                if(x < (this.getWidth() / 2)) {
                    if(blend) {
                        blend = false;
                    } else {
                        blend = true;
                    }

                    //Change the twinkle setting if the lower area right has been pressed
                } else {
                    if(twinkle) {
                        twinkle = false;
                    } else {
                        twinkle = true;
                    }
                }
            }
        }

        //We handled the event
        return true;
    }

    public void addTexture (final int textureId) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                coins.loadTexture(textureId);
            }
        });
    }
}
