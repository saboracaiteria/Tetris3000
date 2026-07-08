package com.example.tetris3d

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import kotlin.math.abs

class GameGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: GameRenderer
    private val game = TetrisGame()
    private var startX = 0f
    private var startY = 0f
    private var minDistance = 0f
    private val gameHandler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var dropDelay = 500L

    init {
        setEGLContextClientVersion(2)
        renderer = GameRenderer(game)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        minDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics
        )
        startGame()
    }

    private fun startGame() {
        game.setListener(object : TetrisGameListener {
            override fun onBoardChanged() { requestRender() }
            override fun onGameOver() { stopDropLoop(); requestRender() }
            override fun onScoreChanged(score: Int, level: Int, lines: Int) {
                dropDelay = (500L / level).coerceAtLeast(50L)
            }
            override fun onNextPiece(piece: PieceType) {}
        })
        game.start()
        startDropLoop()
    }

    private fun startDropLoop() {
        stopDropLoop()
        isRunning = true
        scheduleNextDrop()
    }

    private fun stopDropLoop() {
        isRunning = false
        gameHandler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextDrop() {
        if (!isRunning || game.isGameOver || game.isPaused) return
        gameHandler.postDelayed({
            if (isRunning && !game.isGameOver && !game.isPaused) {
                game.moveDown()
                scheduleNextDrop()
            }
        }, dropDelay)
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
                val absDx = abs(deltaX)
                val absDy = abs(deltaY)

                when {
                    absDx < minDistance && absDy < minDistance -> {
                        game.rotate()
                    }
                    absDx > absDy -> {
                        if (deltaX > 0) game.moveRight() else game.moveLeft()
                    }
                    else -> {
                        if (deltaY > 0) game.hardDrop() else game.rotate()
                    }
                }
                requestRender()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        stopDropLoop()
    }

    override fun onResume() {
        super.onResume()
        if (!isRunning && !game.isGameOver) {
            startDropLoop()
        }
    }
}

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import kotlin.math.abs

class GameGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: GameRenderer
    private val game = TetrisGame()
    private var startX = 0f
    private var startY = 0f
    private var minDistance = 0f
    private val gameHandler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var dropDelay = 500L

    init {
        setEGLContextClientVersion(2)
        renderer = GameRenderer(game)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        minDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics
        )
        startGame()
    }

    private fun startGame() {
        game.setListener(object : TetrisGameListener {
            override fun onBoardChanged() { requestRender() }
            override fun onGameOver() { stopDropLoop(); requestRender() }
            override fun onScoreChanged(score: Int, level: Int, lines: Int) {
                dropDelay = (500L / level).coerceAtLeast(50L)
            }
            override fun onNextPiece(piece: PieceType) {}
        })
        game.start()
        startDropLoop()
    }

    private fun startDropLoop() {
        stopDropLoop()
        isRunning = true
        scheduleNextDrop()
    }

    private fun stopDropLoop() {
        isRunning = false
        gameHandler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextDrop() {
        if (!isRunning || game.isGameOver || game.isPaused) return
        gameHandler.postDelayed({
            if (isRunning && !game.isGameOver && !game.isPaused) {
                game.moveDown()
                scheduleNextDrop()
            }
        }, dropDelay)
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
                val absDx = abs(deltaX)
                val absDy = abs(deltaY)

                when {
                    absDx < minDistance && absDy < minDistance -> {
                        game.rotate()
                    }
                    absDx > absDy -> {
                        if (deltaX > 0) game.moveRight() else game.moveLeft()
                    }
                    else -> {
                        if (deltaY > 0) game.hardDrop() else game.rotate()
                    }
                }
                requestRender()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        stopDropLoop()
    }

    override fun onResume() {
        super.onResume()
        if (!isRunning && !game.isGameOver) {
            startDropLoop()
        }
    }
}
                    }
                    absDx > absDy -> {
                        if (deltaX > 0) game.moveRight() else game.moveLeft()
                    }
                    else -> {
                        if (deltaY > 0) game.hardDrop() else game.rotate()
                    }
                }
                requestRender()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        stopDropLoop()
    }

    override fun onResume() {
        super.onResume()
        if (!isRunning && !game.isGameOver) {
            startDropLoop()
        }
    }
}