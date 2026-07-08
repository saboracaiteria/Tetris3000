package com.example.tetris3d

import android.graphics.Point
import kotlin.random.Random

class TetrisEngine {
    val grid = Array(20) { IntArray(10) }
    val colors = Array(20) { arrayOfNulls<Int>(10) }
    var currentX = 4
    var currentY = 0
    var currentColorType = 1
    var currentShape = ArrayList<Point>()
    var score = 0
    var level = 1
    var lines = 0
    var isGameOver = false
    var isPaused = false

    private var callback: TetrisEngineCallback? = null

    private val shapes = arrayOf(
        arrayListOf(Point(0, 0), Point(-1, 0), Point(1, 0), Point(2, 0)), // I
        arrayListOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)), // O
        arrayListOf(Point(0, 0), Point(-1, 0), Point(1, 0), Point(0, 1)), // T
        arrayListOf(Point(0, 0), Point(-1, 0), Point(0, 1), Point(1, 1)), // S
        arrayListOf(Point(0, 0), Point(-1, 0), Point(-1, 1), Point(1, 0)) // Z
    )
    private val shapeColors = intArrayOf(
        0xFF00F0F0.toInt(), // I cyan
        0xFFF0F000.toInt(), // O yellow
        0xFFA000F0.toInt(), // T purple
        0xFF00F000.toInt(), // S green
        0xFFF00000.toInt()  // Z red
    )

    fun setCallback(cb: TetrisEngineCallback) {
        callback = cb
    }

    fun start() {
        isGameOver = false
        score = 0; level = 1; lines = 0
        for (r in 0 until 20) {
            for (c in 0 until 10) {
                grid[r][c] = 0
                colors[r][c] = null
            }
        }
        spawnPiece()
        callback?.onScoreChanged(score, level, lines)
    }

    fun togglePause() {
        isPaused = !isPaused
        callback?.onBoardChanged()
    }

    private fun spawnPiece() {
        val id = Random.nextInt(shapes.size)
        currentShape.clear()
        for (p in shapes[id]) {
            currentShape.add(Point(p.x, p.y))
        }
        currentColorType = shapeColors[id]
        currentX = 4
        currentY = 0
        callback?.onBoardChanged()
        if (!isValidPosition()) {
            isGameOver = true
            callback?.onGameOver()
        }
    }

    private fun isValidPosition(): Boolean {
        for (p in currentShape) {
            val nx = currentX + p.x
            val ny = currentY + p.y
            if (nx < 0 || nx >= 10 || ny < 0 || ny >= 20) return false
            if (grid[ny][nx] != 0) return false
        }
        return true
    }

    fun moveLeft(): Boolean {
        currentX--
        if (!isValidPosition()) { currentX++; return false }
        callback?.onBoardChanged()
        return true
    }

    fun moveRight(): Boolean {
        currentX++
        if (!isValidPosition()) { currentX--; return false }
        callback?.onBoardChanged()
        return true
    }

    fun moveDown(): Boolean {
        currentY++
        if (!isValidPosition()) {
            currentY--
            lock()
            spawnPiece()
            return false
        }
        callback?.onBoardChanged()
        return true
    }

    fun rotate(): Boolean {
        // Rotate 90 degrees: (x,y) -> (-y,x)
        val rotated = ArrayList<Point>()
        for (p in currentShape) {
            rotated.add(Point(-p.y, p.x))
        }
        val oldShape = ArrayList(currentShape)
        currentShape = rotated
        if (!isValidPosition()) {
            currentShape = oldShape
            return false
        }
        callback?.onBoardChanged()
        return true
    }

    fun hardDrop() {
        while (true) {
            currentY++
            if (!isValidPosition()) {
                currentY--
                lock()
                spawnPiece()
                return
            }
        }
    }

    private fun lock() {
        for (p in currentShape) {
            val nx = currentX + p.x
            val ny = currentY + p.y
            if (ny in 0 until 20 && nx in 0 until 10) {
                grid[ny][nx] = 1
                colors[ny][nx] = currentColorType
            }
        }
        clearLines()
    }

    private fun clearLines() {
        var cleared = 0
        var r = 19
        while (r >= 0) {
            if (grid[r].all { it != 0 }) {
                for (cr in r downTo 1) {
                    grid[cr] = grid[cr - 1]
                    colors[cr] = colors[cr - 1]
                }
                grid[0] = IntArray(10) { 0 }
                colors[0] = arrayOfNulls(10)
                cleared++
            } else {
                r--
            }
        }
        if (cleared > 0) {
            lines += cleared
            score += intArrayOf(0, 100, 300, 500, 800)[cleared.coerceAtMost(4)] * level
            level = 1 + lines / 10
            callback?.onScoreChanged(score, level, lines)
        }
    }
}

interface TetrisEngineCallback {
    fun onBoardChanged()
    fun onGameOver()
    fun onScoreChanged(score: Int, level: Int, lines: Int)
    fun onNextPieceChanged(piece: PieceType) {}
}
