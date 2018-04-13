//
// Created by jerro on 3/8/2018.
//

#include <coin/graphics/Matrix.h>
#include <android/log.h>
#include "Coin.h"

#define LOG_TAG "Coin"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static const GLshort mIndices[] = {
        0, 1, 2,
        0, 2, 3
};

static const GLfloat mUVCoord[] = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
};

Coin::Coin(GLuint currentCoinFace, float pointX, float pointY) {
    //mCurrentCoinFace = currentCoinFace;
    mTextureId = currentCoinFace;

    mBase[0] = -50.0f; // Lefts
    mBase[1] = 50.0f;  // Top
    mBase[2] = 50.0f;  // Right
    mBase[3] = -50.0f; // Bottom

    mTranslation[0] = (GLfloat) pointX;
    mTranslation[1] = (GLfloat) pointY;

    updateTransformedVertices();
}

Coin::~Coin() {
    LOGD("Coin destruct");

    mTextureId = NULL;
    mBase[0] = NULL; // Lefts
    mBase[1] = NULL;  // Top
    mBase[2] = NULL;  // Right
    mBase[3] = NULL; // Bottom

    mTranslation[0] = NULL;
    mTranslation[1] = NULL;
}

void Coin::updateTransformedVertices() {
    // Start with scaling
    GLfloat x1 = mBase[0]; // Left
    GLfloat x2 = mBase[2]; // Right
    GLfloat y1 = mBase[3]; // Bottom
    GLfloat y2 = mBase[1]; // Top

    // We now detach from our Rect because when rotating,
    // we need the seperate points, so we do so in opengl order
    GLfloat one[2] = {x1, y2};
    GLfloat two[2] = {x1, y1};
    GLfloat three[2] = {x2, y1};
    GLfloat four[2] = {x2, y2};

    // Finally we translate the coin to its correct position.
    one[0] += mTranslation[0];
    one[1] += mTranslation[1];

    two[0] += mTranslation[0];
    two[1] += mTranslation[1];

    three[0] += mTranslation[0];
    three[1] += mTranslation[1];

    four[0] += mTranslation[0];
    four[1] += mTranslation[1];

    // Left
    mVertices[0] = one[0];
    mVertices[1] = one[1];
    mVertices[2] = 0.0f;

    // Top
    mVertices[3] = two[0];
    mVertices[4] = two[1];
    mVertices[5] = 0.0f;

    // Right
    mVertices[6] = three[0];
    mVertices[7] = three[1];
    mVertices[8] = 0.0f;

    // Bottom
    mVertices[9] = four[0];
    mVertices[10] = four[1];
    mVertices[11] = 0.0f;
}

void Coin::Render(GLuint textureId, Matrix *mMVPMatrix, GLuint positionHandle, GLuint texCoord, GLint matrixHandle, GLint samplerLoc) {
    // Enable generic vertex attribute array
    glEnableVertexAttribArray(positionHandle);
    glEnableVertexAttribArray(texCoord);

    // Prepare the triangle coordinate data
    glVertexAttribPointer(positionHandle, 3, GL_FLOAT, GL_FALSE, 0, mVertices);
    // Prepare the texture coordinates
    glVertexAttribPointer(texCoord, 2, GL_FLOAT, GL_FALSE, 0, mUVCoord);

    // Apply the projection and view transformation
    glUniformMatrix4fv(matrixHandle, 1, GL_FALSE, mMVPMatrix->mData);
    //  Set the sampler texture unit to 0, where we have saved the texture
    glUniform1i(samplerLoc, 0);

    // Pass in the texture information
    // Set the active texture unit to texture unit 0.
    glActiveTexture(GL_TEXTURE0);
    // Bind the texture to this unit
    glBindTexture(GL_TEXTURE_2D, textureId);

    // Draw the triangle
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, mIndices);

    // Disable vertex array
    // glDisableVertexAttribArray(positionHandle);
    // glDisableVertexAttribArray(texCoord);
}

int Coin::getCurrentCoinFace() {
    return mCurrentCoinFace;
}

int Coin::getNextCoinFace() {
    int nextCoinFace = mCurrentCoinFace;

    if (nextCoinFace == 7) {
        nextCoinFace = 0;
    } else {
        nextCoinFace++;
    }

    mCurrentCoinFace = nextCoinFace;
    return nextCoinFace;
}

GLuint Coin::getTextureId() {
    //return mTextureId;
    GLuint nextTextureId = mTextureId;

    if (nextTextureId == 7) {
        nextTextureId = 0;
    } else {
        nextTextureId++;
    }

    mTextureId = nextTextureId;
    return nextTextureId;
}

void Coin::setTextureId(GLuint textureId) {
    mTextureId = textureId;
    // LOGD("TextureID: %d", (int) mTextureId);
}

float Coin::getY() {
    return mTranslation[1];
}

void Coin::translate(float deltaX, float deltaY) {
    mTranslation[0] += (GLfloat) deltaX;
    mTranslation[1] += (GLfloat) deltaY;

    updateTransformedVertices();
}

void Coin::setTextures(GLuint *textures) {
    mTextures = textures;
}

GLuint Coin::getCurrentTextureId() {
    return mTextures[mTextureId];
}
