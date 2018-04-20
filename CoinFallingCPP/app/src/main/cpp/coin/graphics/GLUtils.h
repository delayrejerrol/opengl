//
// Created by biezhihua on 2017/7/8.
//

#ifndef OPENGLLESSON_GLUTILS_H
#define OPENGLLESSON_GLUTILS_H

#include <jni.h>

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

class GLUtils {
public:

    /**
     * Set Environment parameter
     */
    static void setEnvAndAssetManager(JNIEnv *env, jobject assetManager);

    /**
     * Loads a texture from assets/texture/<name>
     */
    static GLuint loadTexture(const char *name);

    /**
     * Create a program with the given vertex and framgent
     * shader source code.
     */
    static GLuint createProgram(const char **vertexSource, const char **fragmentSource);

    /**
     * Current Time Millis
     */
    static long currentTimeMillis();

    /**
     * Current Realtime
     */
    static int getElapseRealtime();
};

#endif //OPENGLLESSON_GLUTILS_H
