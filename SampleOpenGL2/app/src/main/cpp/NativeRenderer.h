//
// Created by jerro on 4/12/2018.
//

#ifndef SAMPLEOPENGL2_NATIVERENDERER_H
#define SAMPLEOPENGL2_NATIVERENDERER_H


#include <GLES2/gl2.h>
#include <graphics/Matrix.h>

class NativeRenderer {
public:

    NativeRenderer();

    ~NativeRenderer();

    void onCreate();

    void onChanged(int width, int height);

    void onDrawFrame();

    void drawTriangle(GLfloat *verticesData);

private:

    Matrix *mViewMatrix;
    Matrix *mModelMatrix;
    Matrix *mProjectionMatrix;
    Matrix *mMVPMatrix;

    GLuint mProgram;

    GLuint mMVPMatrixHandle;
    GLuint mPositionHandle;
    GLuint mColorHandle;
};


#endif //SAMPLEOPENGL2_NATIVERENDERER_H
