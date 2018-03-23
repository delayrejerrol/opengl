//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COINRENDERER_H
#define COINFALLINGCPP_COINRENDERER_H

#include <GLES2/gl2.h>
#include <coin/model/Coin.h>

class CoinRenderer {

public:
    CoinRenderer();

    ~CoinRenderer();

    void create();

    void change(int width, int height);

    void draw();

    // GL handle
    GLuint mPositionHandle;
    GLuint mTexCoord;
    GLuint mMatrixHandle;
    GLuint mSamplerLoc;
    GLuint *textures;

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

    GLsizei screenWidth = 1280;
    GLsizei screenHeight = 768;

    GLuint programHandle;
    GLuint imageHandle;

    Coin coin = Coin(1, 50.0f, 50.0f);

    int mCurrentTime;

    void drawCoin();
    void setupScaling(int width, int height);
};
#endif //COINFALLINGCPP_COINRENDERER_H
