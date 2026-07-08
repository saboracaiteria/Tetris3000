package com.example.tetris3d

import android.content.Context
import android.graphics.*
import android.view.View

data class CellInfo(val row: Int, val col: Int, val color: Int)

class TetrisView(context: Context) : View(context) {
    private val engine = TetrisEngine()
    private var cellSize = 0f
    private var boardOffsetX = 0f
    private var boardOffsetY = 0f
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1a1a2e") }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#16213e") }
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0f3460"); strokeWidth = 1f }
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 36f; isFakeBoldText = true }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#e94560"); textSize = 28f; isFakeBoldText = true }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8899aa"); textSize = 16f }
    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#e94560"); textSize = 48f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
    private val pausePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 48f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }

    fun getEngine() = engine

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val maxCellW = (w * 0.65f) / 10f
        val maxCellH = (h * 0.85f) / 20f
        cellSize = minOf(maxCellW, maxCellH)
        boardOffsetX = (w - 10 * cellSize) / 2f
        boardOffsetY = (h - 20 * cellSize) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val bw = 10 * cellSize; val bh = 20 * cellSize
        canvas.drawRoundRect(RectF(boardOffsetX, boardOffsetY, boardOffsetX + bw, boardOffsetY + bh), 8f, 8f, gridPaint)

        // Grid lines
        for (r in 0..20) canvas.drawLine(boardOffsetX, boardOffsetY + r * cellSize, boardOffsetX + bw, boardOffsetY + r * cellSize, gridLinePaint)
        for (c in 0..10) canvas.drawLine(boardOffsetX + c * cellSize, boardOffsetY, boardOffsetX + c * cellSize, boardOffsetY + bh, gridLinePaint)

        // Locked blocks
        for (r in 0 until 20) for (c in 0 until 10) {
            engine.colors[r][c]?.let { drawBlock(canvas, r, c, it, 255) }
        }

        // Current piece
        if (!engine.isGameOver && !engine.isPaused) {
            for (p in engine.currentShape) {
                val br = engine.currentY + p.y; val bc = engine.currentX + p.x
                drawBlock(canvas, br, bc, engine.currentColorType, 255)
            }
        }

        // HUD panel
        val px = boardOffsetX + bw + 12f; val py = boardOffsetY + 20f
        canvas.drawText("SCORE", px, py, labelPaint)
        canvas.drawText("${engine.score}", px, py + 30f, scorePaint)
        canvas.drawText("LEVEL", px, py + 80f, labelPaint)
        canvas.drawText("${engine.level}", px, py + 110f, textPaint)
        canvas.drawText("LINES", px, py + 160f, labelPaint)
        canvas.drawText("${engine.lines}", px, py + 190f, textPaint)

        // Game Over overlay
        if (engine.isGameOver) {
            val overlay = Paint().apply { color = Color.argb(150, 0, 0, 0) }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlay)
            canvas.drawText("GAME OVER", width / 2f, height / 2f - 30f, gameOverPaint)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8899aa"); textSize = 24f; textAlign = Paint.Align.CENTER }
            canvas.drawText("Score: ${engine.score}", width / 2f, height / 2f + 30f, sub)
        }
        // Pause overlay
        if (engine.isPaused && !engine.isGameOver) {
            val overlay = Paint().apply { color = Color.argb(120, 0, 0, 0) }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlay)
            canvas.drawText("PAUSED", width / 2f, height / 2f, pausePaint)
        }
        // Title
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 20f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        canvas.drawText("TETRIS 3000", width / 2f, 32f, title)
    }

    private fun drawBlock(canvas: Canvas, row: Int, col: Int, color: Int, alpha: Int) {
        val x = boardOffsetX + col * cellSize; val y = boardOffsetY + row * cellSize
        val s = cellSize; val d = s * 0.15f

        blockPaint.color = color; blockPaint.alpha = alpha
        canvas.drawRoundRect(x + 1f, y + 1f, x + s - 1f, y + s - 1f, 4f, 4f, blockPaint)
        // Top highlight
        blockPaint.color = adjust(color, 1.3f); blockPaint.alpha = alpha
        canvas.drawRect(x + 2f, y + 2f, x + s - 2f, y + s * 0.12f, blockPaint)
        // Bottom shadow
        blockPaint.color = adjust(color, 0.6f); blockPaint.alpha = alpha
        canvas.drawRect(x + 1f, y + s - d, x + s - 1f, y + s - 1f, blockPaint)
        // Right shadow
        canvas.drawRect(x + s - d, y + 1f, x + s - 1f, y + s - 1f, blockPaint)
    }

    private fun adjust(color: Int, factor: Float): Int {
        return Color.rgb(
            (Color.red(color) * factor).coerceIn(0f, 255f).toInt(),
            (Color.green(color) * factor).coerceIn(0f, 255f).toInt(),
            (Color.blue(color) * factor).coerceIn(0f, 255f).toInt()
        )
    }

    fun updateBoard() { postInvalidate() }
}
