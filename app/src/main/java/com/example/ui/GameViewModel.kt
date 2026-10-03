package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.game.Direction
import com.example.game.FloatingScore
import com.example.game.Fruit
import com.example.game.FruitType
import com.example.game.GameDifficulty
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.Ghost
import com.example.game.GhostState
import com.example.game.GhostType
import com.example.game.MazeData
import com.example.game.TileType
import com.example.model.HighScore
import com.example.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager(application)
    private val database = AppDatabase.getDatabase(application)
    val repository = GameRepository(database.highScoreDao())

    val highScores: StateFlow<List<HighScore>> = repository.allScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topScore: StateFlow<Int?> = repository.highestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12500)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // 2D grid of tiles for the current maze
    var mazeGrid: Array<Array<TileType>> = MazeData.createInitialTiles()
        private set

    private var gameLoopJob: Job? = null
    private var ghostScoreMultiplier = 200
    private var fruitSpawnedStage1 = false
    private var fruitSpawnedStage2 = false
    private var mouthOpening = true

    init {
        initNewGame(GameDifficulty.KLASIK)
    }

    fun setDifficulty(diff: GameDifficulty) {
        if (_uiState.value.phase == GamePhase.READY || _uiState.value.phase == GamePhase.GAME_OVER) {
            initNewGame(diff)
        }
    }

    fun toggleSound() {
        val next = !soundManager.soundEnabled
        soundManager.soundEnabled = next
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun initNewGame(diff: GameDifficulty = _uiState.value.difficulty) {
        stopGameLoop()
        mazeGrid = MazeData.createInitialTiles()
        var dots = 0
        for (r in 0 until MazeData.ROWS) {
            for (c in 0 until MazeData.COLS) {
                if (mazeGrid[r][c] == TileType.DOT || mazeGrid[r][c] == TileType.ENERGIZER) {
                    dots++
                }
            }
        }

        val ghosts = listOf(
            Ghost(GhostType.BLINKY, 9f, 7f),
            Ghost(GhostType.PINKY, 9f, 9f),
            Ghost(GhostType.INKY, 8f, 9f),
            Ghost(GhostType.CLYDE, 10f, 9f)
        )
        ghosts.forEach { it.reset(1) }

        fruitSpawnedStage1 = false
        fruitSpawnedStage2 = false
        ghostScoreMultiplier = 200

        _uiState.update {
            it.copy(
                phase = GamePhase.READY,
                score = 0,
                level = 1,
                lives = diff.initialLives,
                dotsRemaining = dots,
                totalDotsInLevel = dots,
                pacmanX = 9f,
                pacmanY = 15f,
                pacmanDir = Direction.LEFT,
                pacmanNextDir = Direction.LEFT,
                mouthAngle = 35f,
                isPowerActive = false,
                powerRemainingMs = 0L,
                ghosts = ghosts,
                activeFruit = null,
                floatingScores = emptyList(),
                readyCountdown = 3,
                difficulty = diff,
                ghostsEatenCount = 0,
                dotsEatenCount = 0
            )
        }

        startReadyCountdown()
    }

    fun startReadyCountdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = GamePhase.READY, readyCountdown = 3) }
            delay(600)
            _uiState.update { it.copy(readyCountdown = 2) }
            delay(600)
            _uiState.update { it.copy(readyCountdown = 1) }
            delay(600)
            _uiState.update { it.copy(phase = GamePhase.PLAYING) }
            startGameLoop()
        }
    }

    fun setNextDirection(direction: Direction) {
        _uiState.update { it.copy(pacmanNextDir = direction) }
    }

    fun pauseGame() {
        if (_uiState.value.phase == GamePhase.PLAYING) {
            _uiState.update { it.copy(phase = GamePhase.PAUSED) }
            stopGameLoop()
        }
    }

    fun resumeGame() {
        if (_uiState.value.phase == GamePhase.PAUSED) {
            _uiState.update { it.copy(phase = GamePhase.PLAYING) }
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch {
            val frameTimeMs = 16L
            while (isActive && _uiState.value.phase == GamePhase.PLAYING) {
                updateGame(frameTimeMs)
                delay(frameTimeMs)
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun updateGame(deltaMs: Long) {
        val state = _uiState.value
        val diff = state.difficulty

        // 1. Update Pacman Position & Movement
        var currentDir = state.pacmanDir
        val nextDir = state.pacmanNextDir
        var px = state.pacmanX
        var py = state.pacmanY
        val speed = diff.pacmanSpeed

        // Buffer check: can we turn into nextDir?
        val nearGridX = (px - px.roundToInt()).let { abs(it) < 0.25f }
        val nearGridY = (py - py.roundToInt()).let { abs(it) < 0.25f }

        if (nextDir != Direction.NONE && nextDir != currentDir) {
            // Turning 180 degrees is always allowed immediately
            if (nextDir == currentDir.opposite()) {
                currentDir = nextDir
            } else if (nearGridX && nearGridY) {
                val gridX = px.roundToInt()
                val gridY = py.roundToInt()
                val targetCol = gridX + nextDir.dx
                val targetRow = gridY + nextDir.dy
                if (!MazeData.isWall(mazeGrid, targetCol, targetRow, allowGate = false)) {
                    currentDir = nextDir
                    px = gridX.toFloat()
                    py = gridY.toFloat()
                }
            }
        }

        // Try moving in current direction
        var isMoving = false
        if (currentDir != Direction.NONE) {
            val nextX = px + currentDir.dx * speed
            val nextY = py + currentDir.dy * speed

            // Check wrap-around tunnel at row 10
            if (py.roundToInt() == 10) {
                if (nextX < -0.6f) {
                    px = MazeData.COLS - 0.4f
                    isMoving = true
                } else if (nextX > MazeData.COLS - 0.4f) {
                    px = -0.6f
                    isMoving = true
                } else {
                    px = nextX
                    isMoving = true
                }
            } else {
                // Regular corridor movement
                val nextCheckCol = if (currentDir.dx > 0) (nextX + 0.48f).toInt() else if (currentDir.dx < 0) (nextX - 0.48f).toInt() else px.roundToInt()
                val nextCheckRow = if (currentDir.dy > 0) (nextY + 0.48f).toInt() else if (currentDir.dy < 0) (nextY - 0.48f).toInt() else py.roundToInt()

                if (!MazeData.isWall(mazeGrid, nextCheckCol, nextCheckRow, allowGate = false)) {
                    px = nextX
                    py = nextY
                    isMoving = true
                } else {
                    // Hit wall: snap to grid center
                    if (currentDir.dx != 0) px = px.roundToInt().toFloat()
                    if (currentDir.dy != 0) py = py.roundToInt().toFloat()
                }
            }
        }

        // 2. Chomp animation
        var mouthAngle = state.mouthAngle
        if (isMoving) {
            if (mouthOpening) {
                mouthAngle += 4.5f
                if (mouthAngle >= 45f) {
                    mouthAngle = 45f
                    mouthOpening = false
                }
            } else {
                mouthAngle -= 4.5f
                if (mouthAngle <= 5f) {
                    mouthAngle = 5f
                    mouthOpening = true
                }
            }
        }

        // 3. Dot & Energizer Eating
        val curCol = px.roundToInt()
        val curRow = py.roundToInt()
        var newScore = state.score
        var dotsRemaining = state.dotsRemaining
        var dotsEatenCount = state.dotsEatenCount
        var powerActive = state.isPowerActive
        var powerTime = state.powerRemainingMs

        if (curRow in 0 until MazeData.ROWS && curCol in 0 until MazeData.COLS) {
            val dist = hypot((px - curCol).toDouble(), (py - curRow).toDouble())
            if (dist < 0.35) {
                when (mazeGrid[curRow][curCol]) {
                    TileType.DOT -> {
                        mazeGrid[curRow][curCol] = TileType.EMPTY
                        newScore += (10 * diff.scoreMultiplier).toInt()
                        dotsRemaining--
                        dotsEatenCount++
                        soundManager.playChomp()
                    }
                    TileType.ENERGIZER -> {
                        mazeGrid[curRow][curCol] = TileType.EMPTY
                        newScore += (50 * diff.scoreMultiplier).toInt()
                        dotsRemaining--
                        dotsEatenCount++
                        powerActive = true
                        powerTime = diff.frightenedDurationMs
                        ghostScoreMultiplier = 200
                        soundManager.playPowerPellet()
                        // Set all active ghosts to frightened
                        state.ghosts.forEach { g ->
                            if (g.state == GhostState.ACTIVE || g.state == GhostState.FRIGHTENED) {
                                g.state = GhostState.FRIGHTENED
                                g.frightenedRemainingMs = diff.frightenedDurationMs
                                g.dir = g.dir.opposite()
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        // Power timer countdown
        if (powerActive) {
            powerTime -= deltaMs
            if (powerTime <= 0L) {
                powerActive = false
                powerTime = 0L
                state.ghosts.forEach { g ->
                    if (g.state == GhostState.FRIGHTENED) {
                        g.state = GhostState.ACTIVE
                    }
                }
            }
        }

        // 4. Fruit Spawning & Eating
        var activeFruit = state.activeFruit
        val totalDots = state.totalDotsInLevel
        if (!fruitSpawnedStage1 && dotsRemaining < totalDots * 0.7f) {
            fruitSpawnedStage1 = true
            val fruitType = when (state.level) {
                1 -> FruitType.CHERRY
                2 -> FruitType.STRAWBERRY
                3 -> FruitType.ORANGE
                else -> FruitType.APPLE
            }
            activeFruit = Fruit(fruitType, x = 9f, y = 12f, remainingTimeMs = 9000L)
        } else if (!fruitSpawnedStage2 && dotsRemaining < totalDots * 0.3f) {
            fruitSpawnedStage2 = true
            val fruitType = when (state.level) {
                1 -> FruitType.STRAWBERRY
                2 -> FruitType.ORANGE
                3 -> FruitType.APPLE
                else -> FruitType.KEY
            }
            activeFruit = Fruit(fruitType, x = 9f, y = 12f, remainingTimeMs = 9000L)
        }

        val newFloatingScores = state.floatingScores.toMutableList()
        if (activeFruit != null) {
            activeFruit.remainingTimeMs -= deltaMs
            if (activeFruit.remainingTimeMs <= 0) {
                activeFruit = null
            } else {
                val distFruit = hypot((px - activeFruit.x).toDouble(), (py - activeFruit.y).toDouble())
                if (distFruit < 0.5) {
                    newScore += activeFruit.type.points
                    soundManager.playFruit()
                    newFloatingScores.add(FloatingScore(points = activeFruit.type.points, x = activeFruit.x, y = activeFruit.y))
                    activeFruit = null
                }
            }
        }

        // Update floating score animations
        val iterator = newFloatingScores.iterator()
        while (iterator.hasNext()) {
            val fs = iterator.next()
            fs.y -= 0.02f
            fs.alpha -= 0.025f
            if (fs.alpha <= 0f) {
                iterator.remove()
            }
        }

        // 5. Update Ghosts
        val blinky = state.ghosts.first { it.type == GhostType.BLINKY }
        var ghostsEatenCount = state.ghostsEatenCount

        state.ghosts.forEach { ghost ->
            updateGhost(ghost, px, py, currentDir, blinky, diff.ghostSpeed, deltaMs)

            // Collision check with Pac-Man
            val dist = hypot((px - ghost.x).toDouble(), (py - ghost.y).toDouble())
            if (dist < 0.65) {
                if (ghost.state == GhostState.FRIGHTENED) {
                    // Eat ghost!
                    ghost.state = GhostState.EATEN
                    val points = ghostScoreMultiplier
                    ghostScoreMultiplier *= 2
                    newScore += points
                    ghostsEatenCount++
                    soundManager.playEatGhost()
                    newFloatingScores.add(FloatingScore(points = points, x = ghost.x, y = ghost.y))
                } else if (ghost.state == GhostState.ACTIVE) {
                    // Pac-Man hit!
                    handlePacmanDeath()
                    return
                }
            }
        }

        // 6. Check Level Cleared
        if (dotsRemaining <= 0) {
            handleLevelClear(newScore, ghostsEatenCount, dotsEatenCount)
            return
        }

        _uiState.update {
            it.copy(
                score = newScore,
                pacmanX = px,
                pacmanY = py,
                pacmanDir = currentDir,
                mouthAngle = mouthAngle,
                dotsRemaining = dotsRemaining,
                dotsEatenCount = dotsEatenCount,
                isPowerActive = powerActive,
                powerRemainingMs = powerTime,
                activeFruit = activeFruit,
                floatingScores = newFloatingScores,
                ghostsEatenCount = ghostsEatenCount
            )
        }
    }

    private fun updateGhost(
        ghost: Ghost,
        pacX: Float,
        pacY: Float,
        pacDir: Direction,
        blinky: Ghost,
        baseSpeed: Float,
        deltaMs: Long
    ) {
        when (ghost.state) {
            GhostState.HOME -> {
                ghost.homeTimerMs -= deltaMs
                if (ghost.homeTimerMs <= 0L) {
                    ghost.state = GhostState.LEAVING
                }
            }
            GhostState.LEAVING -> {
                // Move towards center gate at (9, 7)
                if (ghost.x < 8.9f) ghost.x += 0.04f
                else if (ghost.x > 9.1f) ghost.x -= 0.04f
                else ghost.x = 9f

                if (ghost.y > 7.05f) {
                    ghost.y -= 0.04f
                } else {
                    ghost.y = 7f
                    ghost.state = GhostState.ACTIVE
                    ghost.dir = if (Random.nextBoolean()) Direction.LEFT else Direction.RIGHT
                }
            }
            GhostState.EATEN -> {
                // Fast return to ghost house gate
                val speed = baseSpeed * 1.8f
                moveGhostTowards(ghost, 9f, 7f, speed, allowGate = true)
                if (hypot((ghost.x - 9f).toDouble(), (ghost.y - 7f).toDouble()) < 0.3) {
                    ghost.state = GhostState.HOME
                    ghost.homeTimerMs = 1500L
                }
            }
            GhostState.FRIGHTENED -> {
                ghost.frightenedRemainingMs -= deltaMs
                if (ghost.frightenedRemainingMs <= 0L) {
                    ghost.state = GhostState.ACTIVE
                }
                val speed = baseSpeed * 0.6f
                moveGhostAI(ghost, pacX, pacY, pacDir, blinky, speed)
            }
            GhostState.ACTIVE -> {
                moveGhostAI(ghost, pacX, pacY, pacDir, blinky, baseSpeed)
            }
        }
    }

    private fun moveGhostAI(
        ghost: Ghost,
        pacX: Float,
        pacY: Float,
        pacDir: Direction,
        blinky: Ghost,
        speed: Float
    ) {
        val (targetX, targetY) = ghost.getTargetTile(pacX, pacY, pacDir, blinky)
        moveGhostTowards(ghost, targetX, targetY, speed, allowGate = false)
    }

    private fun moveGhostTowards(
        ghost: Ghost,
        targetX: Float,
        targetY: Float,
        speed: Float,
        allowGate: Boolean
    ) {
        val nearX = (ghost.x - ghost.x.roundToInt()).let { abs(it) < speed * 1.5f }
        val nearY = (ghost.y - ghost.y.roundToInt()).let { abs(it) < speed * 1.5f }

        if (nearX && nearY) {
            val gridX = ghost.x.roundToInt()
            val gridY = ghost.y.roundToInt()
            ghost.x = gridX.toFloat()
            ghost.y = gridY.toFloat()

            // Find valid next directions (cannot immediately reverse direction)
            val possibleDirs = listOf(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT)
                .filter { it != ghost.dir.opposite() }
                .filter { dir ->
                    !MazeData.isWall(mazeGrid, gridX + dir.dx, gridY + dir.dy, allowGate = allowGate)
                }

            if (possibleDirs.isNotEmpty()) {
                // Pick direction that minimizes distance to target
                ghost.dir = possibleDirs.minByOrNull { dir ->
                    val checkX = gridX + dir.dx
                    val checkY = gridY + dir.dy
                    hypot((checkX - targetX).toDouble(), (checkY - targetY).toDouble())
                } ?: ghost.dir
            } else {
                // Dead end: allow reverse
                ghost.dir = ghost.dir.opposite()
            }
        }

        // Apply movement
        ghost.x += ghost.dir.dx * speed
        ghost.y += ghost.dir.dy * speed

        // Handle tunnel wrap for ghosts as well
        if (ghost.y.roundToInt() == 10) {
            if (ghost.x < -0.5f) ghost.x = MazeData.COLS - 0.5f
            else if (ghost.x > MazeData.COLS - 0.5f) ghost.x = -0.5f
        }
    }

    private fun handlePacmanDeath() {
        stopGameLoop()
        soundManager.playDeath()
        val remainingLives = _uiState.value.lives - 1

        viewModelScope.launch {
            _uiState.update { it.copy(phase = GamePhase.DYING, lives = remainingLives) }
            delay(1400)

            if (remainingLives > 0) {
                // Respawn
                _uiState.update {
                    it.copy(
                        pacmanX = 9f,
                        pacmanY = 15f,
                        pacmanDir = Direction.LEFT,
                        pacmanNextDir = Direction.LEFT,
                        isPowerActive = false,
                        powerRemainingMs = 0L,
                        phase = GamePhase.READY,
                        readyCountdown = 3
                    )
                }
                _uiState.value.ghosts.forEach { it.reset(_uiState.value.level) }
                startReadyCountdown()
            } else {
                // Game Over
                _uiState.update { it.copy(phase = GamePhase.GAME_OVER) }
            }
        }
    }

    private fun handleLevelClear(finalScore: Int, ghostsEaten: Int, dotsEaten: Int) {
        stopGameLoop()
        soundManager.playLevelWin()
        val nextLevel = _uiState.value.level + 1

        viewModelScope.launch {
            _uiState.update { it.copy(phase = GamePhase.LEVEL_CLEAR) }
            delay(2200)

            // Reset maze and dots for next level with bonus
            mazeGrid = MazeData.createInitialTiles()
            var dots = 0
            for (r in 0 until MazeData.ROWS) {
                for (c in 0 until MazeData.COLS) {
                    if (mazeGrid[r][c] == TileType.DOT || mazeGrid[r][c] == TileType.ENERGIZER) {
                        dots++
                    }
                }
            }

            _uiState.value.ghosts.forEach { it.reset(nextLevel) }
            fruitSpawnedStage1 = false
            fruitSpawnedStage2 = false
            ghostScoreMultiplier = 200

            _uiState.update {
                it.copy(
                    level = nextLevel,
                    score = finalScore + 500 * nextLevel, // Level clear bonus
                    dotsRemaining = dots,
                    totalDotsInLevel = dots,
                    pacmanX = 9f,
                    pacmanY = 15f,
                    pacmanDir = Direction.LEFT,
                    pacmanNextDir = Direction.LEFT,
                    isPowerActive = false,
                    powerRemainingMs = 0L,
                    activeFruit = null,
                    phase = GamePhase.READY,
                    readyCountdown = 3
                )
            }
            startReadyCountdown()
        }
    }

    fun saveHighScore(name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            repository.saveScore(
                playerName = name,
                score = state.score,
                level = state.level,
                dotsEaten = state.dotsEatenCount,
                ghostsEaten = state.ghostsEatenCount,
                difficulty = state.difficulty.title
            )
        }
    }

    fun clearLeaderboard() {
        viewModelScope.launch {
            repository.clearLeaderboard()
        }
    }

    fun restartGame() {
        initNewGame(_uiState.value.difficulty)
    }
}
