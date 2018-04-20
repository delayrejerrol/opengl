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
    Matrix *mViewMatrix;
    Matrix *mProjectionMatrix;
    Matrix *mMVPMatrix;

    float widthPixel = 320.0f;
    float heightPixel = 480.0f;

    float scaleY;
    float scaleX;
    float scaleValue;

    GLsizei screenWidth = 1280;
    GLsizei screenHeight = 768;

    const int FIXED_WIDTH = 320;
    const int FIXED_HEIGHT = 568;

    static const int TEXTURE_SIZE = 8;
    static const int DROP_COIN = 20; // Drop per coin
    int mCurrentTime;

    bool mClearSurface;

    // GL handle
    GLuint imageProgramHandle;
    GLuint mPositionHandle;
    GLuint mTexCoord;
    GLuint mMatrixHandle;
    GLuint mSamplerLoc;
    GLuint textures[TEXTURE_SIZE];

    vector<Coin *> mCoinCollection;

    void drawCoin();
    void setupScaling(int width, int height);
};
#endif //COINFALLINGCPP_COINRENDERER_H
