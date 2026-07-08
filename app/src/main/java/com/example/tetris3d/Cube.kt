package com.example.tetris3d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube(private val program: Int) {
    private val cubeCoords = floatArrayOf(
        -0.45f,  0.45f,  0.45f,   0.45f,  0.45f,  0.45f,   0.45f, -0.45f,  0.45f,  -0.45f, -0.45f,  0.45f,
        -0.45f,  0.45f, -0.45f,   0.45f,  0.45f, -0.45f,   0.45f, -0.45f, -0.45f,  -0.45f, -0.45f, -0.45f
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,   1, 5, 6, 1, 6, 2,   4, 0, 3, 4, 3, 7,
        4, 5, 1, 4, 1, 0,   3, 2, 6, 3, 6, 7,   5, 4, 7, 5, 7, 6
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(cubeCoords.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply { put(cubeCoords); position(0) }
    }

    private val drawListBuffer: ShortBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply { put(drawOrder); position(0) }
    }

    fun draw(mvpMatrix: FloatArray, color: FloatArray) {
        GLES20.glUseProgram(program)
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        }

        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glVertexAttrib4fv(colorHandle, color, 0)

        GLES20.glGetUniformLocation(program, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}
