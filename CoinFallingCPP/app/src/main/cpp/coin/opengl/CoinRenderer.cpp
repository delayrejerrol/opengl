//
// Created by jerro on 3/8/2018.
//

#include <jni.h>
#include "CoinRenderer.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <coin/graphics/GLUtils.h>
#include <coin/graphics/Matrix.h>
#include <coin/model/Coin.h>
#include <string>

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

    // coin = new Coin(1, 50.0f, 50.0f);
}

CoinRenderer::~CoinRenderer() {
    // The destructor
}

void CoinRenderer::create() {
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
}

void CoinRenderer::change(int width, int height) {
    screenWidth = width;
    screenHeight = height;

    glViewport(0, 0, screenWidth, screenHeight);

    for (int i = 0; i < 16; i++) {
        matrixProjection[i] = 0.0f;
        matrixView[i] = 0.0f;
        matrixProjectionAndView[i] = 0.0f;
    }

    Matrix::orthoM(matrixProjection, 0, 0.0f, screenWidth, 0.0f, screenHeight, 0, 50.0);

    // Matrix::newLookAt(matrixView, 0, 0, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    setupScaling(width, height);
}

void CoinRenderer::draw() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

    drawCoin();
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
    glUseProgram(programHandle);
    glUseProgram(imageHandle);

    long slowTime = GLUtils::currentTimeMillis() % 100000L;
    int elapse = (int) ((0.01f) * (slowTime));

    /*for (int i = 0; i < sizeof(coin); i++) {
        if (mCurrentTime < elapse) {
            int nextCoinFace = coin->getNextCoinFace();
            coin->setTextureId(textures[i]);
            coin->translate(0.0f, 0.20f);
        }
        coin->Render(coin->getTextureId(), mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc);
    }*/

    coin.Render(textures[0], mPositionHandle, mTexCoord, mMatrixHandle, mSamplerLoc);

    //LOGD("Elapse time: %d", elapse);
    mCurrentTime = elapse;
}

static CoinRenderer *coinRenderer;

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeSurfaceCreate(
        JNIEnv *env,
        jclass type,
        jobject assetManager
) {
    GLUtils::setEnvAndAssetManager(env, assetManager);
    coinRenderer = new CoinRenderer();
    if (coinRenderer != nullptr) {
        coinRenderer->create();
    }
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

    jclass arrayClass = (*env).FindClass("java/util/ArrayList");

}