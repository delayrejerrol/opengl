package com.example.jerro.coinfallingkotlinv2.tool

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

class GLTool {
    companion object {
        @JvmStatic
        fun compileShader(shaderType: Int, shaderSource: String): Int {
            var shaderHandle = GLES20.glCreateShader(shaderType)

            if (shaderHandle != 0) {
                // Pass in the shader source
                GLES20.glShaderSource(shaderHandle, shaderSource)

                // Compile the shader
                GLES20.glCompileShader(shaderHandle)

                // Get the compilation status
                val compileStatus = IntArray(1)
                GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

                // If the compilation failed, delete the shader
                if (compileStatus[0] == 0) {
                    Log.e("CoinRenderer", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle))
                    GLES20.glDeleteShader(shaderHandle)
                    shaderHandle = 0
                }
            }

            if (shaderHandle == 0) {
                throw RuntimeException("Error creating shader")
            }

            return shaderHandle
        }

        @JvmStatic
        fun createAndLinkProgram(vertexShader: Int, fragmentShader: Int, attributes: ArrayList<String>?): Int {
            var programHandle = GLES20.glCreateProgram()

            if (programHandle != 0) {
                // Bind the vertex shader to the program
                GLES20.glAttachShader(programHandle, vertexShader)

                // Bind the fragment shader to the program
                GLES20.glAttachShader(programHandle, fragmentShader)

                // Bind attributes
                if (attributes != null) {
                    for (i in 0..attributes.size) {
                        GLES20.glBindAttribLocation(programHandle, i, attributes[i])
                    }
                }

                // Link the two shaders together in a program
                GLES20.glLinkProgram(programHandle)

                // Get the link status
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

                // if the link failed, delete the program
                if (linkStatus[0] == 0) {
                    Log.e("CoinRenderer", "Error compiling program: " + GLES20.glGetShaderInfoLog(programHandle))
                    GLES20.glDeleteProgram(programHandle)
                    programHandle = 0
                }
            }

            if (programHandle == 0) {
                throw RuntimeException("Error creating a program")
            }

            return programHandle
        }


        @JvmStatic
        fun readTextFileFromRawResources(context: Context, resourceId: Int): String {
            val inputStream = context.resources.openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var nextLine: String? = null
            val body = StringBuilder()

            try {
                nextLine = bufferedReader.readLine()
                while ((nextLine) != null) {

                    body.appendln(nextLine)
                    //body.append(System.getProperty("line.separator"))
                    nextLine = bufferedReader.readLine()
                }
            } catch (e: IOException) {
                return null.toString()
            }

            return body.toString()
        }
    }
}