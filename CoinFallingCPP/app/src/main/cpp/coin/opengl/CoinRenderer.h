//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COINRENDERER_H
#define COINFALLINGCPP_COINRENDERER_H

#include <GLES2/gl2.h>

class CoinRenderer {

public:
    CoinRenderer();

    ~CoinRenderer();

    void create();

    void change(int width, int height);

    void draw();

private:
    // Our matrices
    float matrixProjection[16];
    float matrixView[16];
    float matrixProjectionAndView[16];

    float widthPixel = 320.0f;
    float heightPixel = 480.0f;

    float scaleY;
    float scaleX;
    float scaleValue;

    float screenWidth = 1280.0;
    float screenHeight = 768.0;

    GLuint programHandle;
    GLuint imageHandle;

    void setupScaling(int width, int height);
};
#endif //COINFALLINGCPP_COINRENDERER_H
