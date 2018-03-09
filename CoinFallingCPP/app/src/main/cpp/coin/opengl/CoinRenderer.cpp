//
// Created by jerro on 3/8/2018.
//

#include <jni.h>
#include "CoinRenderer.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <coin/graphics/GLUtils.h>
#include <coin/graphics/Matrix.h>

#define LOG_TAG "CoinRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

CoinRenderer::CoinRenderer() {
    // The constructor
}

CoinRenderer::~CoinRenderer() {
    // The destructor
}

void CoinRenderer::create() {
    setupScaling(0, 0);

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    const char *vertexShader = GLUtils::openTextFile("vertex/color_vertex_shader.glsl");
    const char *fragmentShader = GLUtils::openTextFile("fragment/color_fragment_shader.glsl");

    programHandle = GLUtils::createProgram(&vertexShader, &fragmentShader);

    if (!programHandle) {
        LOGD("Could not create a program");
        //return;
    }

    vertexShader = GLUtils::openTextFile("vertex/image_vertex_shader.glsl");
    fragmentShader = GLUtils::openTextFile("fragment/image_fragment_shader.glsl");

    imageHandle = GLUtils::createProgram(&vertexShader, &fragmentShader);
    if (!imageHandle) {
        LOGD("Could not create a program");
    }
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

    Matrix::orthoM(matrixProjection, 0, 0.0f, screenWidth, 0.0f, screenHeight, 0, 50f);

    // Matrix::newLookAt(matrixView, 0, 0, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    setupScaling(width, height);
}

void CoinRenderer::draw() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
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

static CoinRenderer *coinRenderer;

extern "C"
JNIEXPORT void JNICALL
Java_com_project_jerrol_coinfallingcpp_opengl_CoinRenderer_nativeSurfaceCreate(
        JNIEnv *env,
        jclass type
) {
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