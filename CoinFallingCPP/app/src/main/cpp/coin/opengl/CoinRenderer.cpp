//
// Created by jerro on 3/8/2018.
//

#include <jni.h>
#include "CoinRenderer.h"
#include <GLES2/gl2.h>
#include <coin/graphics/GLUtils.h>
#include <coin/model/Coin.h>
#include <android/log.h>
#include <coin/graphics/Matrix.h>
#include <android/legacy_stdlib_inlines.h>
#include <stdlib.h>

#define LOG_TAG "CoinRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

const char *VERTEX_SHADER_SOLID_COLOR = "uniform mat4 uMVPMatrix; \n"
                                        "attribute vec4 vPosition; \n"
                                        "void main() { \n"
                                        "    gl_Position = uMVPMatrix * vPosition; \n"
                                        "}";
const char *FRAGMENT_SHADER_SOLID_COLOR = "precision mediump float; \n"
                                          "void main() { \n"
                                          "    gl_FragColor = vec4(0.5,0,0,1); \n"
                                          "}";

const char *VERTEXT_SHADER_IMAGE = "uniform mat4 uMVPMatrix; \n"
                                   "attribute vec4 vPosition; \n"
                                   "attribute vec2 a_texCoord; \n"
                                   "varying vec2 v_texCoord; \n"
                                   "void main() { \n"
                                   "    gl_Position = uMVPMatrix * vPosition; \n"
                                   "    v_texCoord = a_texCoord; \n"
                                   "}";

const char *FRAGMENT_SHADER_IMAGE = "precision mediump float; \n"
                                    "varying vec2 v_texCoord; \n"
                                    "uniform sampler2D s_texture; \n"
                                    "void main() { \n"
                                    "    gl_FragColor = texture2D( s_texture, v_texCoord ); \n"
                                    "}";

CoinRenderer::CoinRenderer() {
    // The constructor
    mModelMatrix = NULL;
    mMVPMatrix = NULL;
    mProjectionMatrix = NULL;
    mViewMatrix = NULL;
}

CoinRenderer::~CoinRenderer() {
    // The destructor
    delete mModelMatrix;
    mModelMatrix = NULL;
    delete mMVPMatrix;
    mMVPMatrix = NULL;
    delete mProjectionMatrix;
    mProjectionMatrix = NULL;
    delete mViewMatrix;
    mViewMatrix = NULL;
}

void CoinRenderer::create() {
    LOGD("CoinRenderer create");
    setupScaling(0, 0);

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    programHandle = GLUtils::createProgram(&VERTEX_SHADER_SOLID_COLOR, &FRAGMENT_SHADER_SOLID_COLOR);
    if (!programHandle) {
        LOGD("Could not create a program.");
    }

    imageHandle = GLUtils::createProgram(&VERTEXT_SHADER_IMAGE, &FRAGMENT_SHADER_IMAGE);
    if (!imageHandle) {
        LOGD("Could not create a program.");
    }

    // Get handle to vertex shader's vPosition number
    mPositionHandle = (GLuint) glGetAttribLocation(imageHandle, "vPosition");
    // Get handle to texture coordinates location
    mTexCoord = (GLuint) glGetAttribLocation(imageHandle, "a_texCoord");
    // Get handle to shape's transformation matrix
    mMatrixHandle = (GLuint) glGetUniformLocation(imageHandle, "uMVPMatrix");
    // Get handle to textures locations
    mSamplerLoc = (GLuint) glGetUniformLocation(imageHandle, "s_texture");

    // createTextures
    textures[0] = GLUtils::loadTexture("coin/coin_1.png");
    textures[1] = GLUtils::loadTexture("coin/coin_2.png");
    textures[2] = GLUtils::loadTexture("coin/coin_3.png");
    textures[3] = GLUtils::loadTexture("coin/coin_4.png");
    textures[4] = GLUtils::loadTexture("coin/coin_5.png");
    textures[5] = GLUtils::loadTexture("coin/coin_6.png");
    textures[6] = GLUtils::loadTexture("coin/coin_7.png");
    textures[7] = GLUtils::loadTexture("coin/coin_8.png");

    mModelMatrix = new Matrix();
    mMVPMatrix = new Matrix();

    mClearSurface = true;
}

void CoinRenderer::change(int width, int height) {
    screenWidth = width;
    screenHeight = height;

    glViewport(0, 0, screenWidth, screenHeight);

    /*for (int i = 0; i < 16; i++) {
        matrixProjection[i] = 0.0f;
        matrixView[i] = 0.0f;
        matrixProjectionAndView[i] = 0.0f;
    }

    Matrix::orthoM(matrixProjection, 0, 0.0f, screenWidth, 0.0f, screenHeight, 0, 50.0);*/

    //matrixView = Matrix::newLookAt(0, 0, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    // Create a new perspective projection matrix. The height will stay the same
    // while the width will vary as per aspect ratio.
   /* float ratio = (float) width / height;
    float left = 0.0f;
    float right = width;
    float bottom = 0.0f;
    float top = height;
    float near = 0.0f;
    float far = 50.0f;*/

    float left = 0.0f;
    float right = width;
    float bottom = 0.0f;
    float top = height;
    float near = 1.0f;
    float far = 50.0f;

    //mProjectionMatrix = Matrix::orthoM(0, 0.0f, screenWidth, 0.0f, screenHeight, 0.0f, 50.0f);
    mProjectionMatrix = Matrix::newFrustum(left, right, bottom, top, near, far);

    // Position the eye in front of the origin.
    float eyeX = 0.0f;
    float eyeY = 0.0f;
    float eyeZ = 1.0f;

    // We are looking at the origin
    float centerX = 0.0f;
    float centerY = 0.0f;
    float centerZ = 0.0f;

    // Set our up vector.
    float upX = 0.0f;
    float upY = 1.0f;
    float upZ = 0.0f;

    // Set the view matrix.
    mViewMatrix = Matrix::newLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

    // model * view
    //mMVPMatrix->multiply(*mViewMatrix, *mModelMatrix);

    // model * view * projection
    //mMVPMatrix->multiply(*mProjectionMatrix, *mMVPMatrix);
    mMVPMatrix->multiply(*mProjectionMatrix, *mViewMatrix);

    setupScaling(width, height);
}

void CoinRenderer::draw() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

    if (!mClearSurface) {
        drawCoin();
    }
}

void CoinRenderer::setupScaling(int width, int height) {
    // The screen resolution
    widthPixel = width;
    heightPixel = height;

    // Orientation is assumed portrait
    scaleX = widthPixel / 320.0f;
    scaleY = heightPixel / 480.0f;

    // Get our uniform scalier
    if (scaleX > scaleY)
        scaleValue = scaleY;
    else
        scaleValue = scaleX;
}

void CoinRenderer::drawCoin() {
    // glUseProgram(programHandle);
    if (tmpCoinSize == mCoinCollection.size()) {
        // LOGD("drawCoin coin falling down");
        glUseProgram(imageHandle);
        long slowTime = GLUtils::currentTimeMillis() % 100000L;
        int elapse = (int) ((0.01f) * (slowTime));

        for (unsigned i = 0; i < mCoinCollection.size(); i++) {
            Coin* coin = mCoinCollection.at(i);
            if (mCurrentTime < elapse) {
                //int nextCoinFace = coin.getNextCoinFace();
                //coin.setTextureId(textures[i]);
                coin->setTextureId(coin->getTextureId());
                coin->translate(0.0f, -(15.0f * scaleValue));
                // LOGD("Coin Y %.2f", coin->getY());
            }
            //mModelMatrix->identity();
            coin->Render(coin->getCurrentTextureId(), mMVPMatrix, mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc);
        }

        //LOGD("Elapse time: %d", elapse);

        if (!hasVisibleCoin()) {
            LOGD("All coins y < 0");
            //glDeleteProgram(imageHandle);
            // mClearSurface = true;
            clearSurface(true);
            tmpCoinSize = -1;
        }

        mCurrentTime = elapse;
    }
}

void CoinRenderer::drawNewCoin(int coinSize) {

    LOGD("drawNewCoin called");
    LOGD("Size of textures %d", (int) sizeof(textures));
    for (int i = 0; i < coinSize; i++) {
        //GLuint r = rand() % sizeof(textures);
        GLuint r = GLuint (i % TEXTURE_SIZE);
        LOGD("r = %d", r);
        float pointX = rand() % screenWidth + 1;
        LOGD("PointX %.2f", pointX);
        float pointY = rand() % screenHeight + 1;
        LOGD("PointY %.2f", pointY);
        Coin* coin = new Coin(r, pointX, pointY + screenHeight);
        //coin->setTextureId(r);
        coin->setTextures(textures);
        mCoinCollection.push_back(coin);
        tmpCoinSize++;
        if (tmpCoinSize == 0) tmpCoinSize++;
        LOGD("drawNewCoin %d", i);
    }
    LOGD("mCoinCollectionSize = %d", (int) mCoinCollection.size());
    LOGD("drawNewCoin end");
}

void CoinRenderer::clearSurface(bool isClearSurface) {
    if (isClearSurface) {
        for (int i = 0; i < mCoinCollection.size(); i++) {
            Coin* coin = mCoinCollection[i];
            delete coin;
        }
        mCoinCollection.clear();
    }
    mClearSurface = isClearSurface;
}

bool CoinRenderer::hasVisibleCoin() {
    bool hasVisibleCoin = false;
    for (int i = 0; i < mCoinCollection.size(); i++) {
        Coin* coin = mCoinCollection[i];
        if (coin->getY() > 0) {
            hasVisibleCoin = true;
            break;
        }
    }

    return hasVisibleCoin;
}

CoinRenderer *coinRenderer;

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeSurfaceCreate(
        JNIEnv *env,
        jclass type,
        jobject assetManager
) {
    GLUtils::setEnvAndAssetManager(env, assetManager);
    if (coinRenderer) {
        delete coinRenderer;
        coinRenderer = NULL;
    }
    coinRenderer = new CoinRenderer();
    coinRenderer->create();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeSurfaceChange(
        JNIEnv *env,
        jclass type,
        int width, int height
) {
    if (coinRenderer != nullptr) {
        coinRenderer->change(width, height);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeDrawFrame(
        JNIEnv *env,
        jclass type
) {
    if (coinRenderer != nullptr) {
        coinRenderer->draw();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_drawNewCoin(
        JNIEnv *env,
        jclass type,
        int coinSize
) {

    // jclass arrayClass = (*env).FindClass("java/util/ArrayList");
    coinRenderer->drawNewCoin(coinSize);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeClearSurface(
        JNIEnv *env,
        jclass type,
        jboolean isClearSurface
) {
    coinRenderer->clearSurface(isClearSurface);
}