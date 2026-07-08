package com.example.tetris3d

import android.graphics.Point
import kotlin.random.Random

class TetrisGame {
    val grid = Array(20) { IntArray(10) }
    var currentX = 4
    var currentY = 0
    var currentColorType = 1
    var currentShape = ArrayList<Point>()

    private val shapes = arrayOf(
        arrayListOf(Point(0, 0), Point(-1, 0), Point(1, 0), Point(2, 0)),
        arrayListOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)),
        arrayListOf(Point(0, 0), Point(-1, 0), Point(1, 0), Point(0, 1)),
        arrayListOf(Point(0, 0), Point(-1, 0), Point(0, 1), Point(1, 1)),
        arrayListOf(Point(0, 0), Point(-1, 0), Point(-1, 1), Point(1, 0))
    )

    init { spawnPiece() }

    private fun spawnPiece() {
        val id = Random.nextInt(shapes.size)
        currentShape.clear()
        shapes[id].forEach { currentShape.add(Point(it.x, it.y)) }
        currentColorType = id + 1
        currentX = 4
        currentY = 0

        if (checkCollision(currentX, currentY, currentShape)) {
            for (y in 0 until 20) grid[y].fill(0)
        }
    }

    fun tick() {
        if (!move(0, 1)) {
            lockPiece()
            clearRows()
            spawnPiece()
        }
    }

    fun move(dx: Int, dy: Int): Boolean {
        if (!checkCollision(currentX + dx, currentY + dy, currentShape)) {
            currentX += dx
            currentY += dy
            return true
        }
        return false
    }

    fun rotate() {
        val rotated = ArrayList<Point>()
        currentShape.forEach { rotated.add(Point(-it.y, it.x)) }
        if (!checkCollision(currentX, currentY, rotated)) {
            currentShape = rotated
        }
    }

    private fun checkCollision(cx: Int, cy: Int, shape: ArrayList<Point>): Boolean {
        for (p in shape) {
            val gx = cx + p.x
            val gy = cy + p.y
            if (gx !in 0..9 || gy >= 20) return true
            if (gy >= 0 && grid[gy][gx] != 0) return true
        }
        return false
    }

    private fun lockPiece() {
        for (p in currentShape) {
            val gy = currentY + p.y
            val gx = currentX + p.x
            if (gy in 0..19 && gx in 0..9) {
                grid[gy][gx] = currentColorType
            }
        }
    }

    private fun clearRows() {
        for (y in 19 downTo 0) {
            var full = true
            for (x in 0 until 10) {
                if (grid[y][x] == 0) full = false
            }
            if (full) {
                for (ty in y downTo 1) {
                    grid[ty] = grid[ty - 1].clone()
                }
                grid[0].fill(0)
                clearRows()
                break
            }
        }
    }
}
