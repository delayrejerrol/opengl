//
// Created by jerro on 3/8/2018.
//

#ifndef COINFALLINGCPP_COINRENDERER_H
#define COINFALLINGCPP_COINRENDERER_H

#include <GLES2/gl2.h>
#include <coin/model/Coin.h>
#include <vector>
#include <coin/graphics/Matrix.h>

using namespace std;

class CoinRenderer {

public:
    CoinRenderer();

    ~CoinRenderer();

    void create();

    void change(int width, int height);

    void draw();

    void drawNewCoin(int coinSize);

    void clearSurface(bool isClearSurface);

    bool hasVisibleCoin();

private:
    // Our matrices
    //float matrixProjection[16];
    //float matrixView[16];
    //float matrixProjectionAndView[16];
    Matrix *mViewMatrix;
    Matrix *mModelMatrix;
    Matrix *mProjectionMatrix;
    Matrix *mMVPMatrix;

    float widthPixel = 320.0f;
    float heightPixel = 480.0f;

    float scaleY;
    float scaleX;
    float scaleValue;

    GLsizei screenWidth = 1280;
    GLsizei screenHeight = 768;

    GLuint programHandle;
    GLuint imageHandle;

    //Coin coin = Coin(1, 50.0f, 50.0f);

    int mCurrentTime;
    bool mClearSurface;

    void drawCoin();
    void setupScaling(int width, int height);

    int tmpCoinSize = 0;

    static const int TEXTURE_SIZE = 8;

    // GL handle
    GLuint mPositionHandle;
    GLuint mTexCoord;
    GLuint mMatrixHandle;
    GLuint mSamplerLoc;
    GLuint textures[TEXTURE_SIZE];

    //<Coin> *mCoinCollection;
    vector<Coin*> mCoinCollection;
};
#endif //COINFALLINGCPP_COINRENDERER_H
