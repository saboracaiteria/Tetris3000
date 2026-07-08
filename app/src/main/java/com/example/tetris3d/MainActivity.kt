package com.example.tetris3d

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import kotlin.math.abs

class MainActivity : Activity(), TetrisEngineCallback {

    private lateinit var tetrisView: TetrisView
    private lateinit var engine: TetrisEngine
    private var dropHandler = Handler(Looper.getMainLooper())
    private var isRunning = false

    companion object {
        private const val BASE_DROP_DELAY = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#0a0d1a"))
        }

        tetrisView = TetrisView(this)
        engine = tetrisView.getEngine()
        engine.setCallback(this)

        val buttonOverlay = View.inflate(this, R.layout.activity_main, null) as FrameLayout

        buttonOverlay.findViewById<Button>(R.id.btnLeft)?.setOnClickListener {
            engine.moveLeft()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRight)?.setOnClickListener {
            engine.moveRight()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnDown)?.setOnClickListener {
            engine.moveDown()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRotate)?.setOnClickListener {
            engine.rotate()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnDrop)?.setOnClickListener {
            engine.hardDrop()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnPause)?.setOnClickListener {
            engine.togglePause()
            tetrisView.updateBoard()
            if (engine.isPaused) stopDropLoop() else startDropLoop()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRestart)?.setOnClickListener {
            engine.start()
            tetrisView.updateBoard()
            startDropLoop()
        }

        rootLayout.addView(tetrisView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        rootLayout.addView(buttonOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(rootLayout)
        setupSwipeControls()
    }

    override fun onResume() {
        super.onResume()
        if (!engine.isGameOver) {
            engine.start()
            tetrisView.updateBoard()
            startDropLoop()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDropLoop()
    }

    private fun startDropLoop() {
        stopDropLoop()
        if (engine.isGameOver) return
        isRunning = true
        scheduleNextDrop()
    }

    private fun stopDropLoop() {
        isRunning = false
        dropHandler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextDrop() {
        if (!isRunning || engine.isGameOver || engine.isPaused) return
        val delay = (BASE_DROP_DELAY / engine.level).coerceAtLeast(50L)
        dropHandler.postDelayed({
            if (isRunning && !engine.isGameOver && !engine.isPaused) {
                engine.moveDown()
                tetrisView.updateBoard()
                scheduleNextDrop()
            }
        }, delay)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun setupSwipeControls() {
        var startX = 0f
        var startY = 0f
        var startTime = 0L
        val minSwipeDistance = dpToPx(50f)

        tetrisView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    startTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - startX
                    val dy = event.y - startY
                    val time = System.currentTimeMillis() - startTime
                    val absDx = abs(dx)
                    val absDy = abs(dy)

                    if (time < 300) {
                        when {
                            absDx > absDy && absDx > minSwipeDistance -> {
                                if (dx > 0) engine.moveRight() else engine.moveLeft()
                                tetrisView.updateBoard()
                            }
                            absDy > absDx && absDy > minSwipeDistance -> {
                                if (dy > 0) engine.hardDrop() else engine.rotate()
                                tetrisView.updateBoard()
                            }
                            absDx < minSwipeDistance * 0.5f && absDy < minSwipeDistance * 0.5f -> {
                                engine.rotate()
                                tetrisView.updateBoard()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    // TetrisEngineCallback implementations
    override fun onBoardChanged() {
        tetrisView.updateBoard()
    }

    override fun onGameOver() {
        stopDropLoop()
        tetrisView.updateBoard()
    }

    override fun onScoreChanged(score: Int, level: Int, lines: Int) {}

    override fun onNextPieceChanged(piece: PieceType) {}
}

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import kotlin.math.abs

class MainActivity : Activity(), TetrisEngineCallback {

    private lateinit var tetrisView: TetrisView
    private lateinit var engine: TetrisEngine
    private var dropHandler = Handler(Looper.getMainLooper())
    private var isRunning = false

    companion object {
        private const val BASE_DROP_DELAY = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#1a1a2e"))
        }

        tetrisView = TetrisView(this)
        engine = tetrisView.getEngine()
        engine.setCallback(this)

        val buttonOverlay = View.inflate(this, R.layout.activity_main, null) as FrameLayout

        buttonOverlay.findViewById<Button>(R.id.btnLeft)?.setOnClickListener {
            engine.moveLeft()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRight)?.setOnClickListener {
            engine.moveRight()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnDown)?.setOnClickListener {
            engine.moveDown()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRotate)?.setOnClickListener {
            engine.rotate()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnDrop)?.setOnClickListener {
            engine.hardDrop()
            tetrisView.updateBoard()
        }
        buttonOverlay.findViewById<Button>(R.id.btnPause)?.setOnClickListener {
            engine.togglePause()
            tetrisView.updateBoard()
            if (engine.isPaused) stopDropLoop() else startDropLoop()
        }
        buttonOverlay.findViewById<Button>(R.id.btnRestart)?.setOnClickListener {
            engine.start()
            tetrisView.updateBoard()
            startDropLoop()
        }

        rootLayout.addView(tetrisView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        rootLayout.addView(buttonOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(rootLayout)
        setupSwipeControls()
    }

    override fun onResume() {
        super.onResume()
        if (!engine.isGameOver) {
            engine.start()
            tetrisView.updateBoard()
            startDropLoop()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDropLoop()
    }

    private fun startDropLoop() {
        stopDropLoop()
        if (engine.isGameOver) return
        isRunning = true
        scheduleNextDrop()
    }

    private fun stopDropLoop() {
        isRunning = false
        dropHandler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNextDrop() {
        if (!isRunning || engine.isGameOver || engine.isPaused) return
        // Delay inversamente proporcional ao level: level 1 = 500ms, level 10 = 50ms
        val delay = (BASE_DROP_DELAY / engine.level).coerceAtLeast(50L)
        dropHandler.postDelayed({
            if (isRunning && !engine.isGameOver && !engine.isPaused) {
                engine.moveDown()
                tetrisView.updateBoard()
                scheduleNextDrop()
            }
        }, delay)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun setupSwipeControls() {
        var startX = 0f
        var startY = 0f
        var startTime = 0L
        val minSwipeDistance = dpToPx(50f) // 50dp em pixels

        tetrisView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    startTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - startX
                    val dy = event.y - startY
                    val time = System.currentTimeMillis() - startTime
                    val absDx = abs(dx)
                    val absDy = abs(dy)

                    if (time < 300) {
                        when {
                            absDx > absDy && absDx > minSwipeDistance -> {
                                if (dx > 0) engine.moveRight() else engine.moveLeft()
                                tetrisView.updateBoard()
                            }
                            absDy > absDx && absDy > minSwipeDistance -> {
                                if (dy > 0) engine.hardDrop() else engine.rotate()
                                tetrisView.updateBoard()
                            }
                            absDx < minSwipeDistance * 0.5f && absDy < minSwipeDistance * 0.5f -> {
                                // Tap = rotate
                                engine.rotate()
                                tetrisView.updateBoard()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    // TetrisEngineCallback implementations
    override fun onBoardChanged() {
        tetrisView.updateBoard()
    }

    override fun onGameOver() {
        stopDropLoop()
        tetrisView.updateBoard()
    }

    override fun onScoreChanged(score: Int, level: Int, lines: Int) {}

    override fun onNextPieceChanged(piece: PieceType) {}
}
                                if (dx > 0) engine.moveRight() else engine.moveLeft()
                                tetrisView.updateBoard()
                            }
                            absDy > absDx && absDy > 100 -> {
                                if (dy > 0) engine.hardDrop() else engine.rotate()
                                tetrisView.updateBoard()
                            }
                            absDx < 50 && absDy < 50 -> {
                                // Tap = rotate
                                engine.rotate()
                                tetrisView.updateBoard()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    // TetrisEngineCallback implementations
    override fun onBoardChanged() {
        tetrisView.updateBoard()
    }

    override fun onGameOver() {
        stopDropLoop()
        tetrisView.updateBoard()
    }

    override fun onScoreChanged(score: Int, level: Int, lines: Int) {
        // Will be drawn by TetrisView on next frame
    }

    override fun onNextPieceChanged(piece: PieceType) {
        // Will be drawn by TetrisView on next frame
    }
}
