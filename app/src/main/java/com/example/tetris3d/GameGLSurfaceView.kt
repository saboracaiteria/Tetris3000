package com.example.tetris3d

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.abs

class GameGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: GameRenderer
    private val game = TetrisGame()
    private var startX = 0f
    private var startY = 0f
    private val minDistance = 100f
    private var gameThread: Thread? = null
    @Volatile private var isRunning = true

    init {
        setEGLContextClientVersion(2)
        renderer = GameRenderer(game)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        startGameLoop()
    }

    private fun startGameLoop() {
        isRunning = true
        gameThread = Thread {
            while (isRunning) {
                try {
                    Thread.sleep(700)
                    game.tick()
                    requestRender()
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        gameThread?.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                val deltaY = event.y - startY

                if (abs(deltaX) < minDistance && abs(deltaY) < minDistance) {
                    game.rotate()
                } else if (abs(deltaX) > abs(deltaY)) {
                    if (deltaX > 0) game.move(1, 0) else game.move(-1, 0)
                } else {
                    if (deltaY > 0) game.move(0, 1)
                }
                requestRender()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
        gameThread?.interrupt()
    }

    override fun onResume() {
        super.onResume()
        if (!isRunning) {
            startGameLoop()
        }
    }
}
