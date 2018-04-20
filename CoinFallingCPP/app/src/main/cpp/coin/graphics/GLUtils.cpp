//
// Created by biezhihua on 2017/7/8.
//

#include "GLUtils.h"
#include <android/asset_manager_jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <sys/time.h>

#define LOG_TAG "Lesson"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

static JNIEnv *sEnv = NULL;
static jobject sAssetManager = NULL;

/**
 * Loads the given source code as a shader of the given type.
 */
static GLuint loadShader(GLenum shaderType, const char **source) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, source, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 0) {
                char *infoLog = (char *) malloc(sizeof(char) * infoLen);
                glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
                LOGE("Error compiling shader:\n%s\n", infoLog);
                free(infoLog);
            }
            glDeleteShader(shader);
            shader = 0;
        }
    }
    return shader;
}

GLuint GLUtils::createProgram(const char **vertexSource, const char **fragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (!fragmentShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        // Bind the vertex shader to the program
        glAttachShader(program, vertexShader);

        // Bind the fragment shader to the program.
        glAttachShader(program, fragmentShader);

        GLint linkStatus;
        glLinkProgram(program);
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);

        if (!linkStatus) {
            GLint infoLen = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen > 0) {
                char *infoLog = (char *) malloc(sizeof(char) * infoLen);
                glGetProgramInfoLog(program, infoLen, NULL, infoLog);
                LOGE("Error linking program:\n%s\n", infoLog);
                free(infoLog);
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

long GLUtils::currentTimeMillis() {
    struct timeval tv;
    gettimeofday(&tv, (struct timezone *) NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

int GLUtils::getElapseRealtime() {
    int elapse = 0;
    jclass utilsClass = sEnv->FindClass("com/project/jerrol/coinfallingcpp/Utils");
    if (utilsClass == NULL) {
        LOGE("Couldn't find utils class");
        return (GLuint) -1;
    }
    jmethodID getElapseRealtime = sEnv->GetStaticMethodID(utilsClass, "getElapseRealtime", "()I");
    if (getElapseRealtime == NULL) {
        LOGE("Couldn't find getElapseRealtime method");
        return -1;
    }

    elapse = sEnv->CallStaticIntMethod(utilsClass, getElapseRealtime);

    return elapse;
}

GLuint GLUtils::loadTexture(const char *path) {
    GLuint textureId = 0;
    jclass utilsClass = sEnv->FindClass("com/project/jerrol/coinfallingcpp/Utils");
    if (utilsClass == NULL) {
        LOGE("Couldn't find utils class");
        return (GLuint) -1;
    }
    jmethodID loadTexture = sEnv->GetStaticMethodID(utilsClass, "loadTexture",
                                                    "(Landroid/content/res/AssetManager;Ljava/lang/String;)I");
    if (loadTexture == NULL) {
        LOGE("Couldn't find loadTexture method");
        return (GLuint) -1;
    }
    jstring pathStr = sEnv->NewStringUTF(path);
    textureId = (GLuint) sEnv->CallStaticIntMethod(utilsClass, loadTexture, sAssetManager, pathStr);
    sEnv->DeleteLocalRef(pathStr);
    return textureId;
}

void GLUtils::setEnvAndAssetManager(JNIEnv *env, jobject assetManager) {
    sEnv = env;
    sAssetManager = assetManager;
}
