package com.example.tetris3d

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.View

class TetrisView(context: Context) : View(context) {
    private val engine = TetrisEngine()
    private var cellSize = 0f
    private var boardOffsetX = 0f
    private var boardOffsetY = 0f
    private var scaleDensity = 1f
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1a1a2e") }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#16213e") }
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0f3460")
        strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, context.resources.displayMetrics)
    }
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, context.resources.displayMetrics)
        isFakeBoldText = true
    }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#e94560")
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22f, context.resources.displayMetrics)
        isFakeBoldText = true
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8899aa")
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, context.resources.displayMetrics)
    }
    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#e94560")
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40f, context.resources.displayMetrics)
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val pausePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40f, context.resources.displayMetrics)
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, context.resources.displayMetrics)
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val nextPiecePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8899aa")
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, context.resources.displayMetrics)
    }

    fun getEngine() = engine

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleDensity = context.resources.displayMetrics.density

        // Tabuleiro ocupa 90% da largura e 70% da altura (sobra espaço para HUD inferior)
        val boardWidthRatio = 0.90f
        val boardHeightRatio = 0.72f
        val maxCellW = (w * boardWidthRatio) / 10f
        val maxCellH = (h * boardHeightRatio) / 20f
        cellSize = minOf(maxCellW, maxCellH)

        // Centraliza o tabuleiro horizontalmente
        boardOffsetX = (w - 10 * cellSize) / 2f
        // Deixa espaço no topo para o título e embaixo para HUD
        val boardHeight = 20 * cellSize
        val totalHeight = h
        val spaceTop = (totalHeight - boardHeight) * 0.35f // 35% do espaço extra no topo
        boardOffsetY = spaceTop.coerceAtLeast(dpToPx(48f))

        // Ajusta fontes proporcionalmente ao cellSize
        val baseTextSize = cellSize * 0.45f
        textPaint.textSize = baseTextSize * 0.9f
        scorePaint.textSize = baseTextSize * 1.3f
        labelPaint.textSize = baseTextSize * 0.7f
        titlePaint.textSize = baseTextSize * 1.2f
        gameOverPaint.textSize = baseTextSize * 2.5f
        pausePaint.textSize = baseTextSize * 2.5f
        nextPiecePaint.textSize = baseTextSize * 0.7f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Fundo
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Título no topo
        canvas.drawText("TETRIS 3000", w / 2f, boardOffsetY - dpToPx(12f), titlePaint)

        // Área do tabuleiro
        val bw = 10 * cellSize
        val bh = 20 * cellSize
        canvas.drawRoundRect(RectF(boardOffsetX, boardOffsetY, boardOffsetX + bw, boardOffsetY + bh), dpToPx(6f), dpToPx(6f), gridPaint)

        // Linhas do grid
        gridLinePaint.strokeWidth = dpToPx(0.5f)
        for (r in 0..20) canvas.drawLine(boardOffsetX, boardOffsetY + r * cellSize, boardOffsetX + bw, boardOffsetY + r * cellSize, gridLinePaint)
        for (c in 0..10) canvas.drawLine(boardOffsetX + c * cellSize, boardOffsetY, boardOffsetX + c * cellSize, boardOffsetY + bh, gridLinePaint)

        // Blocos travados
        for (r in 0 until 20) for (c in 0 until 10) {
            engine.colors[r][c]?.let { drawBlock(canvas, r, c, it, 255) }
        }

        // Peça atual (ghost e real)
        if (!engine.isGameOver && !engine.isPaused) {
            // Ghost piece (sombra)
            val ghostY = calculateGhostY()
            for (p in engine.currentShape) {
                val br = ghostY + p.y
                val bc = engine.currentX + p.x
                if (br >= 0) drawBlock(canvas, br, bc, engine.currentColorType, 60)
            }
            // Peça real
            for (p in engine.currentShape) {
                val br = engine.currentY + p.y
                val bc = engine.currentX + p.x
                if (br >= 0) drawBlock(canvas, br, bc, engine.currentColorType, 255)
            }
        }

        // === HUD INFERIOR (sob o tabuleiro) ===
        val hudY = boardOffsetY + bh + dpToPx(12f)
        val hudCenterX = boardOffsetX + bw / 2f

        val scoreLabel = "SCORE: ${engine.score}"
        val levelLabel = "LEVEL: ${engine.level}"
        val linesLabel = "LINES: ${engine.lines}"

        labelPaint.textAlign = Paint.Align.LEFT
        scorePaint.textAlign = Paint.Align.LEFT

        // Desenha os labels lado a lado centralizados
        val totalWidth = scorePaint.measureText(scoreLabel) + dpToPx(24f) +
                textPaint.measureText("  |  ") +
                labelPaint.measureText(levelLabel) + dpToPx(24f) +
                labelPaint.measureText(linesLabel)

        var xPos = hudCenterX - totalWidth / 2f

        // SCORE (destacado)
        canvas.drawText(scoreLabel, xPos, hudY, scorePaint)
        xPos += scorePaint.measureText(scoreLabel) + dpToPx(12f)

        // Separador
        val sepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2a3a5a")
            textSize = textPaint.textSize
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("|", xPos, hudY, sepPaint)
        xPos += sepPaint.measureText("|") + dpToPx(12f)

        // LEVEL
        canvas.drawText(levelLabel, xPos, hudY, labelPaint)
        xPos += labelPaint.measureText(levelLabel) + dpToPx(24f)

        // LINES
        canvas.drawText(linesLabel, xPos, hudY, labelPaint)

        // Overlays de estado
        if (engine.isGameOver) {
            val overlay = Paint().apply {
                color = Color.argb(170, 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, w, h, overlay)
            canvas.drawText("GAME OVER", w / 2f, h / 2f - dpToPx(20f), gameOverPaint)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8899aa")
                textSize = textPaint.textSize * 1.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Score: ${engine.score}", w / 2f, h / 2f + dpToPx(28f), sub)
        }
        if (engine.isPaused && !engine.isGameOver) {
            val overlay = Paint().apply {
                color = Color.argb(140, 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, w, h, overlay)
            canvas.drawText("PAUSED", w / 2f, h / 2f, pausePaint)
        }
    }

    private fun calculateGhostY(): Int {
        var gy = engine.currentY
        while (true) {
            gy++
            for (p in engine.currentShape) {
                val nx = engine.currentX + p.x
                val ny = gy + p.y
                if (ny >= 20 || ny < 0 || (ny in 0 until 20 && engine.grid[ny][nx] != 0)) {
                    return gy - 1
                }
            }
        }
    }

    private fun drawBlock(canvas: Canvas, row: Int, col: Int, color: Int, alpha: Int) {
        val x = boardOffsetX + col * cellSize
        val y = boardOffsetY + row * cellSize
        val s = cellSize
        val d = s * 0.12f
        val corner = dpToPx(3f)

        blockPaint.color = color
        blockPaint.alpha = alpha
        canvas.drawRoundRect(x + 1f, y + 1f, x + s - 1f, y + s - 1f, corner, corner, blockPaint)

        // Brilho no topo
        blockPaint.color = adjust(color, 1.35f)
        blockPaint.alpha = (alpha * 0.6f).toInt()
        canvas.drawRect(x + 2f, y + 2f, x + s - 2f, y + s * 0.15f, blockPaint)

        // Sombra na base e na direita
        blockPaint.color = adjust(color, 0.55f)
        blockPaint.alpha = alpha
        canvas.drawRect(x + 1f, y + s - d, x + s - 1f, y + s - 1f, blockPaint)
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
        for (c in 0..10) canvas.drawLine(boardOffsetX + c * cellSize, boardOffsetY, boardOffsetX + c * cellSize, boardOffsetY + bh, gridLinePaint)

        // Blocos travados
        for (r in 0 until 20) for (c in 0 until 10) {
            engine.colors[r][c]?.let { drawBlock(canvas, r, c, it, 255) }
        }

        // Peça atual (ghost e real)
        if (!engine.isGameOver && !engine.isPaused) {
            // Ghost piece (sombra)
            val ghostY = calculateGhostY()
            for (p in engine.currentShape) {
                val br = ghostY + p.y
                val bc = engine.currentX + p.x
                if (br >= 0) drawBlock(canvas, br, bc, engine.currentColorType, 60)
            }
            // Peça real
            for (p in engine.currentShape) {
                val br = engine.currentY + p.y
                val bc = engine.currentX + p.x
                if (br >= 0) drawBlock(canvas, br, bc, engine.currentColorType, 255)
            }
        }

        // === HUD INFERIOR (sob o tabuleiro) ===
        val hudY = boardOffsetY + bh + dpToPx(12f)
        val hudCenterX = boardOffsetX + bw / 2f

        val scoreLabel = "SCORE: ${engine.score}"
        val levelLabel = "LEVEL: ${engine.level}"
        val linesLabel = "LINES: ${engine.lines}"

        labelPaint.textAlign = Paint.Align.LEFT
        scorePaint.textAlign = Paint.Align.LEFT

        // Desenha os labels lado a lado centralizados
        val totalWidth = scorePaint.measureText(scoreLabel) + dpToPx(24f) +
                textPaint.measureText("  |  ") +
                labelPaint.measureText(levelLabel) + dpToPx(24f) +
                labelPaint.measureText(linesLabel)

        var xPos = hudCenterX - totalWidth / 2f

        // SCORE (destacado)
        canvas.drawText(scoreLabel, xPos, hudY, scorePaint)
        xPos += scorePaint.measureText(scoreLabel) + dpToPx(12f)

        // Separador
        val sepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2a3a5a")
            textSize = textPaint.textSize
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("|", xPos, hudY, sepPaint)
        xPos += sepPaint.measureText("|") + dpToPx(12f)

        // LEVEL
        canvas.drawText(levelLabel, xPos, hudY, labelPaint)
        xPos += labelPaint.measureText(levelLabel) + dpToPx(24f)

        // LINES
        canvas.drawText(linesLabel, xPos, hudY, labelPaint)

        // Overlays de estado
        if (engine.isGameOver) {
            val overlay = Paint().apply {
                color = Color.argb(170, 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, w, h, overlay)
            canvas.drawText("GAME OVER", w / 2f, h / 2f - dpToPx(20f), gameOverPaint)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8899aa")
                textSize = textPaint.textSize * 1.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Score: ${engine.score}", w / 2f, h / 2f + dpToPx(28f), sub)
        }
        if (engine.isPaused && !engine.isGameOver) {
            val overlay = Paint().apply {
                color = Color.argb(140, 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, w, h, overlay)
            canvas.drawText("PAUSED", w / 2f, h / 2f, pausePaint)
        }
    }

    private fun calculateGhostY(): Int {
        var gy = engine.currentY
        while (true) {
            gy++
            for (p in engine.currentShape) {
                val nx = engine.currentX + p.x
                val ny = gy + p.y
                if (ny >= 20 || ny < 0 || (ny in 0 until 20 && engine.grid[ny][nx] != 0)) {
                    return gy - 1
                }
            }
        }
    }

    private fun drawBlock(canvas: Canvas, row: Int, col: Int, color: Int, alpha: Int) {
        val x = boardOffsetX + col * cellSize
        val y = boardOffsetY + row * cellSize
        val s = cellSize
        val d = s * 0.12f
        val corner = dpToPx(3f)

        blockPaint.color = color
        blockPaint.alpha = alpha
        canvas.drawRoundRect(x + 1f, y + 1f, x + s - 1f, y + s - 1f, corner, corner, blockPaint)

        // Brilho no topo
        blockPaint.color = adjust(color, 1.35f)
        blockPaint.alpha = (alpha * 0.6f).toInt()
        canvas.drawRect(x + 2f, y + 2f, x + s - 2f, y + s * 0.15f, blockPaint)

        // Sombra na base e na direita
        blockPaint.color = adjust(color, 0.55f)
        blockPaint.alpha = alpha
        canvas.drawRect(x + 1f, y + s - d, x + s - 1f, y + s - 1f, blockPaint)
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
}
