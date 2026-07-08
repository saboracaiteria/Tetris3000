package com.example.tetris3d

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GameRenderer(private val game: TetrisGame) : GLSurfaceView.Renderer {
    private lateinit var cube: Cube
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var program = 0
    private var aspectRatio = 1f

    private val vertexShaderCode = "uniform mat4 uMVPMatrix;\nattribute vec4 vPosition;\nattribute vec4 vColor;\nvarying vec4 vOutColor;\nvoid main() {\n  gl_Position = uMVPMatrix * vPosition;\n  vOutColor = vColor;\n}"
    private val fragmentShaderCode = "precision mediump float;\nvarying vec4 vOutColor;\nvoid main() {\n  gl_FragColor = vOutColor;\n}"

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.04f, 0.05f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
        cube = Cube(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / height
        val viewHeight = 12f
        val viewWidth = viewHeight * aspectRatio
        Matrix.frustumM(projectionMatrix, 0, -viewWidth / 2f, viewWidth / 2f, -viewHeight / 2f, viewHeight / 2f, 3f, 40f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val camZ = 14f + (aspectRatio - 0.5f) * 4f
        val camY = -8f - (aspectRatio - 0.5f) * 2f
        Matrix.setLookAtM(viewMatrix, 0, 4.5f, camY, camZ, 4.5f, -9.5f, 0f, 0f, 1f, 0f)

        for (y in 0 until 20) for (x in 0 until 10) {
            val colorType = game.grid[y][x]
            if (colorType != 0) drawBlock(x, y, colorType)
        }
        val shape = game.currentShape
        for (pos in shape) {
            val px = game.currentX + pos.x
            val py = game.currentY + pos.y
            if (py in 0..19 && px in 0..9) drawBlock(px, py, game.currentColorType)
        }
    }

    private fun drawBlock(x: Int, y: Int, colorType: Int) {
        val scratch = FloatArray(16)
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x.toFloat(), -y.toFloat(), 0f)
        Matrix.multiplyMM(scratch, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(scratch, 0, projectionMatrix, 0, scratch, 0)
        val color = when (colorType) {
            1 -> floatArrayOf(0.0f, 0.9f, 1.0f, 1.0f)
            2 -> floatArrayOf(1.0f, 0.84f, 0.0f, 1.0f)
            3 -> floatArrayOf(0.7f, 0.53f, 1.0f, 1.0f)
            4 -> floatArrayOf(0.46f, 1.0f, 0.01f, 1.0f)
            else -> floatArrayOf(1.0f, 0.09f, 0.27f, 1.0f)
        }
        cube.draw(scratch, color)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
