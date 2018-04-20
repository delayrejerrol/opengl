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
    mMVPMatrix = NULL;
    mProjectionMatrix = NULL;
    mViewMatrix = NULL;
}

CoinRenderer::~CoinRenderer() {
    // The destructor
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
    glEnable(GL_CULL_FACE);

    imageProgramHandle = GLUtils::createProgram(&VERTEXT_SHADER_IMAGE, &FRAGMENT_SHADER_IMAGE);
    if (!imageProgramHandle) {
        LOGD("Could not create a program.");
    }

    // Get handle to vertex shader's vPosition number
    mPositionHandle = (GLuint) glGetAttribLocation(imageProgramHandle, "vPosition");
    // Get handle to texture coordinates location
    mTexCoord = (GLuint) glGetAttribLocation(imageProgramHandle, "a_texCoord");
    // Get handle to shape's transformation matrix
    mMatrixHandle = (GLuint) glGetUniformLocation(imageProgramHandle, "uMVPMatrix");
    // Get handle to textures locations
    mSamplerLoc = (GLuint) glGetUniformLocation(imageProgramHandle, "s_texture");

    // createTextures
    textures[0] = GLUtils::loadTexture("coin/coin_1.png");
    textures[1] = GLUtils::loadTexture("coin/coin_2.png");
    textures[2] = GLUtils::loadTexture("coin/coin_3.png");
    textures[3] = GLUtils::loadTexture("coin/coin_4.png");
    textures[4] = GLUtils::loadTexture("coin/coin_5.png");
    textures[5] = GLUtils::loadTexture("coin/coin_6.png");
    textures[6] = GLUtils::loadTexture("coin/coin_7.png");
    textures[7] = GLUtils::loadTexture("coin/coin_8.png");

    mMVPMatrix = new Matrix();

    mClearSurface = true;
}

void CoinRenderer::change(int width, int height) {
//    screenWidth = width;
//    screenHeight = height;

    glViewport(0, 0, width, height);

    // Create a new perspective projection matrix. The height will stay the same
    // while the width will vary as per aspect ratio.
    float left = FIXED_WIDTH;
    float right = 0.0f;
    float bottom = FIXED_HEIGHT;
    float top = 0.0f;
    float near = 1.0f;
    float far = 50.0f;

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

    // model * view * projection
     mMVPMatrix->multiply(*mProjectionMatrix, *mViewMatrix);

    setupScaling(width, height);
}

void CoinRenderer::draw() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    if (!mClearSurface) {
        drawCoin();
    }

    glDisable(GL_BLEND);
}

void CoinRenderer::setupScaling(int width, int height) {
    // The screen resolution
    widthPixel = width;
    heightPixel = height;

    // Orientation is assumed portrait
    //scaleX = widthPixel / 320.0f;
    //scaleY = heightPixel / 480.0f;
    scaleX = widthPixel / FIXED_WIDTH;
    scaleY = heightPixel / FIXED_HEIGHT;

    // Get our uniform scalier
    if (scaleX > scaleY)
        scaleValue = scaleY;
    else
        scaleValue = scaleX;

    LOGD("scaleValue %.2f", scaleValue);
}

void CoinRenderer::drawCoin() {
    glUseProgram(imageProgramHandle);
    //long slowTime = GLUtils::currentTimeMillis() % 100000L;
    // int elapse = (int) ((0.01f) * (slowTime));
    int elapse = GLUtils::getElapseRealtime();

//  LOGI("getElapseReal Time: %d", GLUtils::getElapseRealtime());
//  LOGI("elapse Time: %d", elapse);

    for (unsigned i = 0; i < mCoinCollection.size(); i++) {

        Coin* prevCoin = mCoinCollection.at(i);
        if (mCurrentTime < elapse) {
            prevCoin->setTextureId(prevCoin->getTextureId());
            prevCoin->translate(0.0f, 10.0f);
        }
        prevCoin->Render(prevCoin->getCurrentTextureId(), mMVPMatrix, mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc);
    }
    if (!hasVisibleCoin()) {
        LOGD("All coins y < 0");
        clearSurface(true);
    }

    mCurrentTime = elapse;
}

void CoinRenderer::drawNewCoin(int coinSize) {
    LOGD("drawNewCoin called");
    // LOGD("Size of textures %d", (int) sizeof(textures));
    LOGD("screenWidth %d", screenWidth);
    LOGD("screenHeight %d", screenHeight);
    LOGD("FIXED_WIDTH %d", FIXED_WIDTH);
    LOGD("FIXED_HEIGHT %d", FIXED_HEIGHT);
    for (int i = 0; i < coinSize; i++) {
        GLuint r = GLuint (i % TEXTURE_SIZE);
        LOGD("r = %d", r);
        float pointX = rand() % (int)(((FIXED_WIDTH - 48) - 48) + 1) + 48;
        //float pointX = rand() % (FIXED_WIDTH) + 48;;
        LOGD("PointX %.2f", pointX);
        float pointY = rand() % 310 + 1;
        LOGD("PointY %.2f", pointY);
        // Coin* coin = new Coin(r, pointX, pointY + screenHeight);
        //Coin* coin = new Coin(r, pointX - 50.0f, pointY + (screenHeight / 2)); // Same all height
        Coin* coin = new Coin(r, pointX, pointY, scaleValue); // Same all height
        coin->setTextures(textures);
        mCoinCollection.push_back(coin);
        // LOGD("drawNewCoin %d", i);
    }
    LOGD("mCoinCollectionSize = %d", (int) mCoinCollection.size());
    LOGD("drawNewCoin end");
}

void CoinRenderer::clearSurface(bool isClearSurface) {
    if (isClearSurface) {
        mCoinCollection.clear();
    }
    mClearSurface = isClearSurface;
}

bool CoinRenderer::hasVisibleCoin() {
    bool hasVisibleCoin = false;
    for (unsigned i = 0; i < mCoinCollection.size(); i++) {
        Coin *coin = mCoinCollection.at(i);
        if (coin->getY() < 410.0f + 20.0f) {
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