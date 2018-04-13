#include <jni.h>
#include <string>
#include "NativeRenderer.h"
#include <android/log.h>
#include <GLES2/gl2.h>
#include <graphics/GLUtils.h>

#define LOG_TAG "CoinRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s \n", name, v);
}

static void checkGLError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x) \n", op, error);
    }
}

const char *VERTEX_SHADER = "uniform mat4 uMVPMatrix;  \n"  // A constant representing the combined model/view/projection matrix.
                            "attribute vec4 vPosition;  \n" // Per-vertex position information we will pass in.
                            // "attribute vec4 a_Color; \n"     // Per-vertex color information we will pass in.
                            // "varying vec4 v_Color;  \n"      // This will be passed into the fragment shader.
                            "void main()    { \n"             // The entry point for our vertex shader.
                            // "   v_Color = a_Color; \n"          // Pass the color through to the fragment shader.
                            "   gl_Position = uMVPMatrix * vPosition; \n" // gl_Postion is a special variable used to store the final position.
                            "}";                          // normalized screen coordinates.

const char *FRAGMENT_SHADER = "precision mediump float; \n" // Set the default precision to medium. We don't need as high of a
                              // "varying vec4 v_Color; \n" // This is the color from the vertex shader interpolated across the
                              "void main() { \n"    // The entry point for our fragment shader.
                              // " gl_FragColor = v_Color; \n" // Pass the color directly through the pipeline.
                              " gl_FragColor = vec4(0.5,0,0,1); \n"
                              "}";

// This triangle is red, green and blue.
GLfloat triangle[] = {
        // X, Y, Z
        // R, G, B, A
        /*-0.5f, -0.25f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        0.5f, -0.25f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.0f, 0.559016994f, 0.0f,
        0.0f, 1.0f, 0.0f, 1.0f*/
        10.0f, 200.0f, 0.0f,
        10.0f, 100.0f, 0.0f,
        100.0f, 100.0f, 0.0f
};

GLshort indices[] = { 0, 1, 2};

NativeRenderer::NativeRenderer() {
    mModelMatrix = NULL;
    mMVPMatrix = NULL;
    mProjectionMatrix = NULL;
    mViewMatrix = NULL;
}

NativeRenderer::~NativeRenderer() {
    delete mModelMatrix;
    mModelMatrix = NULL;
    delete mMVPMatrix;
    mMVPMatrix = NULL;
    delete mProjectionMatrix;
    mProjectionMatrix = NULL;
    delete mViewMatrix;
    mViewMatrix = NULL;
}

void NativeRenderer::onCreate() {
    LOGD("native-lib onCreate called");
    mProgram = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (!mProgram) {
        LOGD("Could not create program.");
        return;
    }
    glClearColor(0.0F, 0.0F, 0.0F, 1.0F);

    mModelMatrix = new Matrix();
    mMVPMatrix = new Matrix();
}

void NativeRenderer::onChanged(int width, int height) {
    LOGD("native-lib onChanged called");
    glViewport(0, 0, width, height);

    // Create a new perspective projection matrix. The height will stay the same
    // while the width will vary as per aspect ratio.
    float ratio = (float) width / height;
    /*float left = -ratio;
    float right = ratio;
    float bottom = -1.0f;
    float top = 1.0f;
    float near = 1.0f;
    float far = 2.0f;*/
    float left = 0.0f;
    float right = width;
    float bottom = 0.0f;
    float top = height;
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
}

void NativeRenderer::onDrawFrame() {
    glClear(GL_COLOR_BUFFER_BIT); // Clear the screen and the depth buffer
    checkGLError("glClear");
    //mModelMatrix->identity();
    glUseProgram(mProgram);

    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mProgram, "uMVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mProgram, "vPosition");
    // mColorHandle = (GLuint) glGetAttribLocation(mProgram, "a_Color");
    //mModelMatrix->identity();
    drawTriangle(triangle);
}

void NativeRenderer::drawTriangle(GLfloat *verticesData) {
    // glVertexAttribPointer((GLuint) mPositionHandle, 3,
    // GL_FLOAT, GL_FALSE, 4*7, verticesData);
    // GL_FLOAT, GL_FALSE, 4*7, verticesData);
    glVertexAttribPointer((GLuint) mPositionHandle, 3,
                          GL_FLOAT, GL_FALSE, 0, verticesData);

    glEnableVertexAttribArray((GLuint) mPositionHandle);

    // glVertexAttribPointer((GLuint) mColorHandle, 4,
    // GL_FLOAT, GL_FALSE, 4*7, verticesData + 3);

    // glEnableVertexAttribArray((GLuint) mColorHandle);

    // model + view
    //mMVPMatrix->multiply(*mViewMatrix, *mModelMatrix);

    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, mMVPMatrix->mData);

    // glDrawArrays(GL_TRIANGLES, 0, 3);
    glDrawElements(GL_TRIANGLES, sizeof(indices), GL_UNSIGNED_SHORT, indices);
    checkGLError("glDrawElements");

    glDisableVertexAttribArray(mPositionHandle);
}

NativeRenderer *nativeRenderer;

extern "C" {
    JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnCreate(JNIEnv *env, jobject obj);
    JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnChanged(JNIEnv *env, jobject obj, int width, int height);
    JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnDrawFrame(JNIEnv *env, jobject obj);
};

JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnCreate(JNIEnv *env, jobject obj) {
    if (nativeRenderer) {
        delete nativeRenderer;
        nativeRenderer = NULL;
    }
    nativeRenderer = new NativeRenderer();
    nativeRenderer->onCreate();
}

JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnChanged(JNIEnv *env, jobject obj, int width, int height) {
    nativeRenderer->onChanged(width, height);
}

JNIEXPORT void JNICALL Java_com_delayre_jerrol_sampleopengl_MainActivity_nativeOnDrawFrame(JNIEnv *env, jobject obj) {
    nativeRenderer->onDrawFrame();
}
