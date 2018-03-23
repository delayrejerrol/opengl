package com.project.jerrol.coinfallingkotlin.model

import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by jerro on 3/6/2018.
 */
class Coin(currentCoinFace: Int, pointX: Float, pointY: Float, scaledValue: Float) {
    // Geometric variables
    private lateinit var mVertices: FloatArray
    private var mIndices: ShortArray
    private var mUVCoord: FloatArray

    // These buffer is not included in c++
    private lateinit var mVertexBuffer: FloatBuffer
    private var mDrawListBuffer: ShortBuffer
    private var mUVBuffer: FloatBuffer

    private var mCurrentCoinFace: Int = 0
    private var mTextureId: Int = 0

    private var mBase: RectF
    private var mTranslation: PointF

    public var mTextures = IntArray(8)

    private var mScaledValue = 0

    init {
        // Initialize the current coin image position.
        // this.mCurrentCoinFace = currentCoinFace
        this.mTextureId = currentCoinFace

        // Initialize our initial size of coin around the 0,0 point
        mBase = RectF(-10f * scaledValue, 10f * scaledValue, 10f * scaledValue, -10f * scaledValue)

        // Initialize the current coin position (translate)
        mTranslation = PointF(pointX, pointY)

        updateVertices()

        // The order of vertex rendering for a quad
        mIndices = shortArrayOf(
                0, 1, 2,
                0, 2, 3
        )

        // Initialize byte buffer for the draw list
        mDrawListBuffer = getByteBuffer(mIndices.size * 2).asShortBuffer()
        mDrawListBuffer.put(mIndices)
        mDrawListBuffer.position(0)

        // Create our UV coordinates
        mUVCoord = floatArrayOf(
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        )

        // The texture buffer
        mUVBuffer = getByteBuffer(mUVCoord.size * 4).asFloatBuffer()
        mUVBuffer.put(mUVCoord)
        mUVBuffer.position(0)
    }

    /*fun getCurrentCoinFace(): Int {
        return mCurrentCoinFace
    }

    fun getNextCoinFace(currentCoinFace: Int): Int {
        var nextCoinFace = currentCoinFace

        nextCoinFace = if (nextCoinFace == 7) 0 else nextCoinFace.inc()

        this.mCurrentCoinFace = nextCoinFace
        return nextCoinFace
    }*/

    fun getCurrentCoinFace(): Int {
        return mCurrentCoinFace
    }

    fun getNextCoinFace(currentCoinFace: Int): Int {
        var nextCoinFace = currentCoinFace

        nextCoinFace = if (nextCoinFace == 7) 0 else nextCoinFace.inc()

        this.mCurrentCoinFace = nextCoinFace
        return nextCoinFace
    }

    /*fun getTextures(): Int {
        return mTextures[0]
    }*/

    fun getCurrentTextureId(): Int {
        return mTextures[this.mTextureId]
    }

    fun getTextures(): IntArray {
        return mTextures
    }

    fun getTextureId(): Int {
        //return mTextureId
        var nextTextureId = mTextureId

        nextTextureId = if (nextTextureId == 7) 0 else nextTextureId.inc()

        this.mTextureId = nextTextureId
        //return mTextures[nextTextureId]
        return nextTextureId
    }

    fun getTransformedVertices(): FloatArray {
        // Start with scaling
        val x1 = mBase.left
        val x2 = mBase.right
        val y1 = mBase.bottom
        val y2 = mBase.top

        // We now detach from our Rect because when rotating,
        // we need the seperate points, so we do so in opengl order
        val one = PointF(x1, y2)
        val two = PointF(x1, y1)
        val three = PointF(x2, y1)
        val four = PointF(x2, y2)

        // Finally we translate the coin to its correct position.
        one.x += mTranslation.x
        one.y += mTranslation.y

        two.x += mTranslation.x
        two.y += mTranslation.y

        three.x += mTranslation.x
        three.y += mTranslation.y

        four.x += mTranslation.x
        four.y += mTranslation.y

        // We now return our float array of vertices.
        return floatArrayOf(
                one.x,   one.y,   0.0f,
                two.x,   two.y,   0.0f,
                three.x, three.y, 0.0f,
                four.x,  four.y,  0.0f
        )
    }

    fun getY(): Float {
        return mTranslation.y
    }

    // Render each coin
    fun Render(matrix: FloatArray, textureId: Int,
               positionHandle: Int, textCoord: Int, matrixHandle: Int, samplerLoc: Int) {
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textCoord)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        // Prepare the texture coordinates
        GLES20.glVertexAttribPointer(textCoord, 2, GLES20.GL_FLOAT, false, 0, mUVBuffer)
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)
        // Pass in the texture information
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // Bind the texture to this unit
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //  Set the sampler texture unit to 0, where we have saved the texture
        GLES20.glUniform1i(samplerLoc, 0)

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.size, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer)
        // GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, mIndices.size)

        // Disable vertex array
        //GLES20.glDisableVertexAttribArray(positionHandle)
        //GLES20.glDisableVertexAttribArray(textCoord)
    }

    fun setTextures(textures: IntArray) {
        this.mTextures = textures
    }

    fun setTextureId(textureId: Int) {
        mTextureId = textureId
        //mTextures[mTextureId] = mTextures[textureId]
    }

    fun translate(deltaX: Float, deltaY: Float) {
        mTranslation.x += deltaX
        mTranslation.y += deltaY

        updateVertices()
    }

    private fun getByteBuffer(capacity: Int): ByteBuffer {
        val bb = ByteBuffer.allocateDirect(capacity)
        bb.order(ByteOrder.nativeOrder())
        return bb
    }

    private fun updateVertices() {
        // Update transform vertices.
        mVertices = getTransformedVertices()

        mVertexBuffer = getByteBuffer(mVertices.size * 4).asFloatBuffer()
        mVertexBuffer.put(mVertices)
        mVertexBuffer.position(0)
    }
}