//
// Created by jerro on 3/8/2018.
//

#include "Coin.h"

GLfloat *mVertices;
// The order of vertex rendering for a quad
float mIndices[] = {0, 1, 2,
                    0, 2, 3};
float mUVCoord[] = {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
};

// Rect
//float mBase[] = { -50.0f, 50.0f, 50.0f, -50.0f };
GLfloat *mBase;

// Point
GLfloat *mTranslation;

Coin::Coin(int currentCoinFace, float pointX, float pointY) {
    mCurrentCoinFace = currentCoinFace;

    mBase[0] = -50.0f; // Left
    mBase[1] = 50.0f;  // Top
    mBase[2] = 50.0f;  // Right
    mBase[3] = -50.0f; // Bottom

    mTranslation[0] = pointX;
    mTranslation[1] = pointY;
}

void Coin::updateTransformedVertices() {
    // Start with scaling
    float x1 = mBase[0];
    float x2 = mBase[1];
    float y1 = mBase[2];
    float y2 = mBase[3];

    // We now detach from our Rect because when rotating,
    // we need the seperate points, so we do so in opengl order
    float one[] = {x1, y2};
    float two[] = {x1, y1};
    float three[] = {x2, y1};
    float four[] = {x2, y2};

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
    mVertices[3] = 0.0f;

    // Top
    mVertices[4] = two[0];
    mVertices[5] = two[1];
    mVertices[6] = 0.0f;

    // Right
    mVertices[7] = three[0];
    mVertices[8] = three[1];
    mVertices[9] = 0.0f;

    // Bottom
    mVertices[10] = four[0];
    mVertices[11] = four[1];
    mVertices[12] = 0.0f;
}

void Coin::Render(GLuint textureId, GLuint positionHandle, GLuint texCoord, GLint matrixHandle, GLint samplerLoc) {
    // Bind the texture to this unit
    glBindTexture(GL_TEXTURE_2D, textureId);

    // Enable generic vertex attribute array
    glEnableVertexAttribArray(positionHandle);
    glEnableVertexAttribArray(texCoord);

    // Prepare the triangle coordinate data
    glVertexAttribPointer(positionHandle, 3, GL_FLOAT, GL_FALSE, 0, mVertices);
    // Prepare the texture coordinates
    glVertexAttribPointer(texCoord, 2, GL_FLOAT, GL_FALSE, 0, mUVCoord);
    // Apply the projection and view transformation
    glUniformMatrix4fv(matrixHandle, 1, GL_FALSE, 0);
    //  Set the sampler texture unit to 0, where we have saved the texture
    glUniform1i(samplerLoc, 0);

    // Draw the triangle
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, mIndices);

    // Disable vertex array
    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(texCoord);
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
    return mTextureId;
}

void Coin::setTextureId(GLuint textureId) {
    mTextureId = textureId;
}

float Coin::getY() {
    return mTranslation[1];
}

void Coin::translate(float deltaX, float deltaY) {
    mTranslation[0] += deltaX;
    mTranslation[1] += deltaY;

    updateTransformedVertices();
}
