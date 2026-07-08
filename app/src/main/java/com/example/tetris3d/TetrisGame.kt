package com.example.tetris3d

import kotlin.random.Random

enum class PieceType(val blocks: Array<IntArray>, val r: Float, val g: Float, val b: Float) {
    I(arrayOf(intArrayOf(0,0,0,0),intArrayOf(1,1,1,1),intArrayOf(0,0,0,0),intArrayOf(0,0,0,0)), 0f, 0.9f, 1f),
    O(arrayOf(intArrayOf(1,1),intArrayOf(1,1)), 1f, 0.84f, 0f),
    T(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0.7f, 0.53f, 1f),
    S(arrayOf(intArrayOf(0,1,1),intArrayOf(1,1,0),intArrayOf(0,0,0)), 0.46f, 1f, 0.01f),
    Z(arrayOf(intArrayOf(1,1,0),intArrayOf(0,1,1),intArrayOf(0,0,0)), 1f, 0.09f, 0.27f),
    J(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0.27f, 0.51f, 1f),
    L(arrayOf(intArrayOf(0,0,1),intArrayOf(1,1,1),intArrayOf(0,0,0)), 1f, 0.57f, 0f)
}

data class Piece(val type: PieceType, var row: Int, var col: Int, var rotation: Int = 0) {
    fun getBlocks(): Array<IntArray> {
        var m = type.blocks
        repeat(rotation % 4) {
            val n = m.size; val r = Array(n){IntArray(n)}
            for(i in 0 until n) for(j in 0 until n) r[j][n-1-i]=m[i][j]
            m = r
        }
        return m
    }
}

interface TetrisGameListener {
    fun onBoardChanged()
    fun onGameOver()
    fun onScoreChanged(score: Int, level: Int, lines: Int)
    fun onNextPiece(piece: PieceType)
}

class TetrisGame(val rows: Int = 20, val cols: Int = 10) {
    val grid = Array(rows){IntArray(cols){0}}
    val boardColors = Array(rows){arrayOfNulls<FloatArray>(cols)}
    var currentPiece: Piece? = null; private set
    var nextPiece: PieceType = PieceType.T; private set
    var score = 0; private set
    var level = 1; private set
    var lines = 0; private set
    var isGameOver = false; private set
    var isPaused = false
    private var listener: TetrisGameListener? = null
    private var bag = mutableListOf<PieceType>()

    // Propriedades compatíveis com GameRenderer
    val currentShape: List<Pos> get() = currentPiece?.let { piece ->
        val blocks = piece.getBlocks()
        val positions = mutableListOf<Pos>()
        for (r in blocks.indices) for (c in blocks[0].indices) if (blocks[r][c] != 0) positions.add(Pos(c, r))
        positions
    } ?: emptyList()
    val currentX: Int get() = currentPiece?.col ?: 0
    val currentY: Int get() = currentPiece?.row ?: 0
    val currentColorType: Int get() = when (currentPiece?.type) {
        PieceType.I -> 1; PieceType.O -> 2; PieceType.T -> 3
        PieceType.S -> 4; PieceType.Z -> 5; PieceType.J -> 6; PieceType.L -> 7
        else -> 1
    }

    fun setListener(l: TetrisGameListener) { listener = l }
    fun getBoard() = grid
    fun getBoardColors() = boardColors

    fun start() {
        isGameOver = false; score = 0; level = 1; lines = 0; isPaused = false
        for(r in 0 until rows) for(c in 0 until cols) { grid[r][c]=0; boardColors[r][c]=null }
        bag.clear(); nextPiece = randomPiece(); spawnPiece()
        listener?.onScoreChanged(score, level, lines)
    }

    fun togglePause() { isPaused = !isPaused; listener?.onBoardChanged() }

    private fun randomPiece(): PieceType {
        if(bag.isEmpty()) { bag = PieceType.values().toMutableList(); bag.shuffle() }
        return bag.removeAt(bag.lastIndex)
    }

    private fun spawnPiece() {
        val type = nextPiece; nextPiece = randomPiece()
        val sz = type.blocks[0].size; val col = cols/2 - sz/2
        currentPiece = Piece(type, 0, col)
        listener?.onNextPiece(nextPiece)
        if(!isValid(currentPiece!!)) { isGameOver = true; currentPiece = null; listener?.onGameOver() }
        listener?.onBoardChanged()
    }

    private fun isValid(row: Int, col: Int, blocks: Array<IntArray>): Boolean {
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=row+r; val bc=col+c
            if(br<0||br>=rows||bc<0||bc>=cols) return false
            if(grid[br][bc]!=0) return false
        }
        return true
    }
    private fun isValid(p: Piece) = isValid(p.row, p.col, p.getBlocks())

    fun moveLeft(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col-1, p.getBlocks())) { p.col--; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveRight(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col+1, p.getBlocks())) { p.col++; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveDown(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row+1, p.col, p.getBlocks())) { p.row++; listener?.onBoardChanged(); return true }
        lock(); return false
    }
    fun rotate(): Boolean {
        val p = currentPiece ?: return false
        val newRot = (p.rotation+1)%4
        var m = p.type.blocks
        repeat(newRot) { val n=m.size; val r=Array(n){IntArray(n)}; for(i in 0 until n)for(j in 0 until n)r[j][n-1-i]=m[i][j]; m=r }
        if(isValid(p.row, p.col, m)) { p.rotation=newRot; listener?.onBoardChanged(); return true }
        for(off in intArrayOf(-1,1,-2,2)) if(isValid(p.row, p.col+off, m)) { p.col+=off; p.rotation=newRot; listener?.onBoardChanged(); return true }
        return false
    }
    fun hardDrop() {
        val p = currentPiece ?: return
        while(isValid(p.row+1, p.col, p.getBlocks())) p.row++
        lock()
    }

    private fun lock() {
        val p = currentPiece ?: return
        val blocks = p.getBlocks(); val color = floatArrayOf(p.type.r, p.type.g, p.type.b)
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=p.row+r; val bc=p.col+c
            if(br in 0 until rows && bc in 0 until cols) {
                grid[br][bc] = when(p.type) { PieceType.I->1; PieceType.O->2; PieceType.T->3; PieceType.S->4; PieceType.Z->5; PieceType.J->6; PieceType.L->7 }
                boardColors[br][bc]=color
            }
        }
        clearLines(); spawnPiece()
    }

    private fun clearLines() {
        var cleared = 0; var r = rows-1
        while(r >= 0) {
            if(grid[r].all{it!=0}) {
                for(cr in r downTo 1) { grid[cr]=grid[cr-1]; boardColors[cr]=boardColors[cr-1] }
                grid[0]=IntArray(cols){0}; boardColors[0]=arrayOfNulls(cols); cleared++
            } else r--
        }
        if(cleared>0) {
            lines += cleared
            score += intArrayOf(0,100,300,500,800)[cleared.coerceAtMost(4)] * level
            level = 1+lines/10
            listener?.onScoreChanged(score, level, lines)
        }
    }
}

data class Pos(val x: Int, val y: Int)

import kotlin.random.Random

enum class PieceType(val blocks: Array<IntArray>, val r: Float, val g: Float, val b: Float) {
    I(arrayOf(intArrayOf(0,0,0,0),intArrayOf(1,1,1,1),intArrayOf(0,0,0,0),intArrayOf(0,0,0,0)), 0f, 0.9f, 1f),
    O(arrayOf(intArrayOf(1,1),intArrayOf(1,1)), 1f, 0.84f, 0f),
    T(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0.7f, 0.53f, 1f),
    S(arrayOf(intArrayOf(0,1,1),intArrayOf(1,1,0),intArrayOf(0,0,0)), 0.46f, 1f, 0.01f),
    Z(arrayOf(intArrayOf(1,1,0),intArrayOf(0,1,1),intArrayOf(0,0,0)), 1f, 0.09f, 0.27f),
    J(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0.27f, 0.51f, 1f),
    L(arrayOf(intArrayOf(0,0,1),intArrayOf(1,1,1),intArrayOf(0,0,0)), 1f, 0.57f, 0f)
}

data class Piece(val type: PieceType, var row: Int, var col: Int, var rotation: Int = 0) {
    fun getBlocks(): Array<IntArray> {
        var m = type.blocks
        repeat(rotation % 4) {
            val n = m.size; val r = Array(n){IntArray(n)}
            for(i in 0 until n) for(j in 0 until n) r[j][n-1-i]=m[i][j]
            m = r
        }
        return m
    }
}

interface TetrisGameListener {
    fun onBoardChanged()
    fun onGameOver()
    fun onScoreChanged(score: Int, level: Int, lines: Int)
    fun onNextPiece(piece: PieceType)
}

class TetrisGame(val rows: Int = 20, val cols: Int = 10) {
    val grid = Array(rows){IntArray(cols){0}}
    val boardColors = Array(rows){arrayOfNulls<FloatArray>(cols)}
    var currentPiece: Piece? = null; private set
    var nextPiece: PieceType = PieceType.T; private set
    var score = 0; private set
    var level = 1; private set
    var lines = 0; private set
    var isGameOver = false; private set
    var isPaused = false
    private var listener: TetrisGameListener? = null
    private var bag = mutableListOf<PieceType>()

    // Propriedades compatíveis com GameRenderer
    val currentShape: List<Pos> get() = currentPiece?.let { piece ->
        val blocks = piece.getBlocks()
        val positions = mutableListOf<Pos>()
        for (r in blocks.indices) for (c in blocks[0].indices) if (blocks[r][c] != 0) positions.add(Pos(c, r))
        positions
    } ?: emptyList()
    val currentX: Int get() = currentPiece?.col ?: 0
    val currentY: Int get() = currentPiece?.row ?: 0
    val currentColorType: Int get() = when (currentPiece?.type) {
        PieceType.I -> 1; PieceType.O -> 2; PieceType.T -> 3
        PieceType.S -> 4; PieceType.Z -> 5; PieceType.J -> 6; PieceType.L -> 7
        else -> 1
    }

    fun setListener(l: TetrisGameListener) { listener = l }
    fun getBoard() = grid
    fun getBoardColors() = boardColors

    fun start() {
        isGameOver = false; score = 0; level = 1; lines = 0; isPaused = false
        for(r in 0 until rows) for(c in 0 until cols) { grid[r][c]=0; boardColors[r][c]=null }
        bag.clear(); nextPiece = randomPiece(); spawnPiece()
        listener?.onScoreChanged(score, level, lines)
    }

    fun togglePause() { isPaused = !isPaused; listener?.onBoardChanged() }

    private fun randomPiece(): PieceType {
        if(bag.isEmpty()) { bag = PieceType.values().toMutableList(); bag.shuffle() }
        return bag.removeAt(bag.lastIndex)
    }

    private fun spawnPiece() {
        val type = nextPiece; nextPiece = randomPiece()
        val sz = type.blocks[0].size; val col = cols/2 - sz/2
        currentPiece = Piece(type, 0, col)
        listener?.onNextPiece(nextPiece)
        if(!isValid(currentPiece!!)) { isGameOver = true; currentPiece = null; listener?.onGameOver() }
        listener?.onBoardChanged()
    }

    private fun isValid(row: Int, col: Int, blocks: Array<IntArray>): Boolean {
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=row+r; val bc=col+c
            if(br<0||br>=rows||bc<0||bc>=cols) return false
            if(grid[br][bc]!=0) return false
        }
        return true
    }
    private fun isValid(p: Piece) = isValid(p.row, p.col, p.getBlocks())

    fun moveLeft(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col-1, p.getBlocks())) { p.col--; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveRight(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col+1, p.getBlocks())) { p.col++; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveDown(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row+1, p.col, p.getBlocks())) { p.row++; listener?.onBoardChanged(); return true }
        lock(); return false
    }
    fun rotate(): Boolean {
        val p = currentPiece ?: return false
        val newRot = (p.rotation+1)%4
        var m = p.type.blocks
        repeat(newRot) { val n=m.size; val r=Array(n){IntArray(n)}; for(i in 0 until n)for(j in 0 until n)r[j][n-1-i]=m[i][j]; m=r }
        if(isValid(p.row, p.col, m)) { p.rotation=newRot; listener?.onBoardChanged(); return true }
        for(off in intArrayOf(-1,1,-2,2)) if(isValid(p.row, p.col+off, m)) { p.col+=off; p.rotation=newRot; listener?.onBoardChanged(); return true }
        return false
    }
    fun hardDrop() {
        val p = currentPiece ?: return
        while(isValid(p.row+1, p.col, p.getBlocks())) p.row++
        lock()
    }

    private fun lock() {
        val p = currentPiece ?: return
        val blocks = p.getBlocks(); val color = floatArrayOf(p.type.r, p.type.g, p.type.b)
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=p.row+r; val bc=p.col+c
            if(br in 0 until rows && bc in 0 until cols) {
                grid[br][bc] = when(p.type) { PieceType.I->1; PieceType.O->2; PieceType.T->3; PieceType.S->4; PieceType.Z->5; PieceType.J->6; PieceType.L->7 }
                boardColors[br][bc]=color
            }
        }
        clearLines(); spawnPiece()
    }

    private fun clearLines() {
        var cleared = 0; var r = rows-1
        while(r >= 0) {
            if(grid[r].all{it!=0}) {
                for(cr in r downTo 1) { grid[cr]=grid[cr-1]; boardColors[cr]=boardColors[cr-1] }
                grid[0]=IntArray(cols){0}; boardColors[0]=arrayOfNulls(cols); cleared++
            } else r--
        }
        if(cleared>0) {
            lines += cleared
            score += intArrayOf(0,100,300,500,800)[cleared.coerceAtMost(4)] * level
            level = 1+lines/10
            listener?.onScoreChanged(score, level, lines)
        }
    }
}

data class Pos(val x: Int, val y: Int)

import kotlin.random.Random

enum class PieceType(val blocks: Array<IntArray>, val r: Float, val g: Float, val b: Float) {
    I(arrayOf(intArrayOf(0,0,0,0),intArrayOf(1,1,1,1),intArrayOf(0,0,0,0),intArrayOf(0,0,0,0)), .0f,1f,1f),
    O(arrayOf(intArrayOf(1,1),intArrayOf(1,1)), 1f,1f,0f),
    T(arrayOf(intArrayOf(0,1,0),intArrayOf(1,1,1),intArrayOf(0,0,0)),0.63f,0f,0.94f),
    S(arrayOf(intArrayOf(0,1,1),intArrayOf(1,1,0),intArrayOf(0,0,0)), 0f,0.94f,0f),
    Z(arrayOf(intArrayOf(1,1,0),intArrayOf(0,1,1),intArrayOf(0,0,0)), 0.94f,0f,0f),
    J(arrayOf(intArrayOf(1,0,0),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0f,0f,0.94f),
    L(arrayOf(intArrayOf(0,0,1),intArrayOf(1,1,1),intArrayOf(0,0,0)), 0.94f,0.63f,0f)
}

data class Piece(val type: PieceType, var row: Int, var col: Int, var rotation: Int = 0) {
    fun getBlocks(): Array<IntArray> {
        var m = type.blocks
        repeat(rotation % 4) {
            val n = m.size; val r = Array(n){IntArray(n)}
            for(i in 0 until n) for(j in 0 until n) r[j][n-1-i]=m[i][j]
            m = r
        }
        return m
    }
}

interface TetrisGameListener {
    fun onBoardChanged()
    fun onGameOver()
    fun onScoreChanged(score: Int, level: Int, lines: Int)
    fun onNextPiece(piece: PieceType)
}

class TetrisGame(val rows: Int = 20, val cols: Int = 10) {
    private val board = Array(rows){IntArray(cols){0}}
    private val boardColors = Array(rows){arrayOfNulls<FloatArray>(cols)}
    var currentPiece: Piece? = null; private set
    var nextPiece: PieceType = PieceType.T; private set
    var score = 0; private set
    var level = 1; private set
    var lines = 0; private set
    var isGameOver = false; private set
    var isPaused = false
    private var listener: TetrisGameListener? = null
    private var bag = mutableListOf<PieceType>()

    fun setListener(l: TetrisGameListener) { listener = l }
    fun getBoard() = board
    fun getBoardColors() = boardColors

    fun start() {
        isGameOver = false; score = 0; level = 1; lines = 0; isPaused = false
        for(r in 0 until rows) for(c in 0 until cols) { board[r][c]=0; boardColors[r][c]=null }
        bag.clear(); nextPiece = randomPiece(); spawnPiece()
        listener?.onScoreChanged(score, level, lines)
    }

    fun togglePause() { isPaused = !isPaused; listener?.onBoardChanged() }

    private fun randomPiece(): PieceType {
        if(bag.isEmpty()) { bag = PieceType.values().toMutableList(); bag.shuffle() }
        return bag.removeAt(bag.lastIndex)
    }

    private fun spawnPiece() {
        val type = nextPiece; nextPiece = randomPiece()
        val sz = type.blocks[0].size; val col = cols/2 - sz/2
        currentPiece = Piece(type, 0, col)
        listener?.onNextPiece(nextPiece)
        if(!isValid(currentPiece!!)) { isGameOver = true; currentPiece = null; listener?.onGameOver() }
        listener?.onBoardChanged()
    }

    private fun isValid(row: Int, col: Int, blocks: Array<IntArray>): Boolean {
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=row+r; val bc=col+c
            if(br<0||br>=rows||bc<0||bc>=cols) return false
            if(board[br][bc]!=0) return false
        }
        return true
    }
    private fun isValid(p: Piece) = isValid(p.row, p.col, p.getBlocks())

    fun moveLeft(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col-1, p.getBlocks())) { p.col--; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveRight(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row, p.col+1, p.getBlocks())) { p.col++; listener?.onBoardChanged(); return true }
        return false
    }
    fun moveDown(): Boolean {
        val p = currentPiece ?: return false
        if(isValid(p.row+1, p.col, p.getBlocks())) { p.row++; listener?.onBoardChanged(); return true }
        lock(); return false
    }
    fun rotate(): Boolean {
        val p = currentPiece ?: return false
        val newRot = (p.rotation+1)%4
        var m = p.type.blocks
        repeat(newRot) { val n=m.size; val r=Array(n){IntArray(n)}; for(i in 0 until n)for(j in 0 until n)r[j][n-1-i]=m[i][j]; m=r }
        if(isValid(p.row, p.col, m)) { p.rotation=newRot; listener?.onBoardChanged(); return true }
        for(off in intArrayOf(-1,1,-2,2)) if(isValid(p.row, p.col+off, m)) { p.col+=off; p.rotation=newRot; listener?.onBoardChanged(); return true }
        return false
    }
    fun hardDrop() {
        val p = currentPiece ?: return
        while(isValid(p.row+1, p.col, p.getBlocks())) p.row++
        lock()
    }
    fun getGhostPiece(): Piece? {
        val p = currentPiece ?: return null
        val g = Piece(p.type, p.row, p.col, p.rotation)
        while(isValid(g.row+1, g.col, g.getBlocks())) g.row++
        return g
    }

    private fun lock() {
        val p = currentPiece ?: return
        val blocks = p.getBlocks(); val color = floatArrayOf(p.type.r, p.type.g, p.type.b)
        for(r in blocks.indices) for(c in blocks[0].indices) if(blocks[r][c]!=0) {
            val br=p.row+r; val bc=p.col+c
            if(br in 0 until rows && bc in 0 until cols) { board[br][bc]=1; boardColors[br][bc]=color }
        }
        clearLines(); spawnPiece()
    }

    private fun clearLines() {
        var cleared = 0; var r = rows-1
        while(r >= 0) {
            if(board[r].all{it!=0}) {
                for(cr in r downTo 1) { board[cr]=board[cr-1]; boardColors[cr]=boardColors[cr-1] }
                board[0]=IntArray(cols){0}; boardColors[0]=arrayOfNulls(cols); cleared++
            } else r--
        }
        if(cleared>0) {
            lines += cleared
            score += intArrayOf(0,100,300,500,800)[cleared.coerceAtMost(4)] * level
            level = 1+lines/10
            listener?.onScoreChanged(score, level, lines)
        }
    }
}
