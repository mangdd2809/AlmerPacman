package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GhostBlinky
import com.example.ui.theme.GhostClyde
import com.example.ui.theme.GhostInky
import com.example.ui.theme.GhostPinky
import kotlin.math.hypot
import kotlin.random.Random

enum class GhostType(val ghostName: String, val nickName: String, val baseColor: Color) {
    BLINKY("Blinky", "Si Merah", GhostBlinky),
    PINKY("Pinky", "Si Manis", GhostPinky),
    INKY("Inky", "Si Biru", GhostInky),
    CLYDE("Clyde", "Si Penakut", GhostClyde)
}

enum class GhostState {
    HOME,
    LEAVING,
    ACTIVE,
    FRIGHTENED,
    EATEN
}

data class Ghost(
    val type: GhostType,
    var x: Float,
    var y: Float,
    var dir: Direction = Direction.UP,
    var state: GhostState = GhostState.HOME,
    var frightenedRemainingMs: Long = 0L,
    var homeTimerMs: Long = 0L
) {
    fun reset(level: Int = 1) {
        state = GhostState.HOME
        frightenedRemainingMs = 0L
        when (type) {
            GhostType.BLINKY -> {
                x = 9f
                y = 7f // Starts already outside above gate
                dir = Direction.LEFT
                state = GhostState.ACTIVE
                homeTimerMs = 0L
            }
            GhostType.PINKY -> {
                x = 9f
                y = 9f
                dir = Direction.UP
                homeTimerMs = 1500L
            }
            GhostType.INKY -> {
                x = 8f
                y = 9f
                dir = Direction.UP
                homeTimerMs = 4000L
            }
            GhostType.CLYDE -> {
                x = 10f
                y = 9f
                dir = Direction.UP
                homeTimerMs = 7000L
            }
        }
    }

    fun getTargetTile(pacmanX: Float, pacmanY: Float, pacmanDir: Direction, blinky: Ghost): Pair<Float, Float> {
        return when (state) {
            GhostState.EATEN -> Pair(9f, 9f) // Head back into house
            GhostState.FRIGHTENED -> Pair(Random.nextInt(MazeData.COLS).toFloat(), Random.nextInt(MazeData.ROWS).toFloat())
            GhostState.ACTIVE -> when (type) {
                GhostType.BLINKY -> Pair(pacmanX, pacmanY) // Directly chases Pac-Man
                GhostType.PINKY -> {
                    // Ambush 4 tiles ahead of Pac-Man
                    Pair(pacmanX + pacmanDir.dx * 4, pacmanY + pacmanDir.dy * 4)
                }
                GhostType.INKY -> {
                    // Flanker vector using Blinky and Pacman
                    val targetX = pacmanX + pacmanDir.dx * 2
                    val targetY = pacmanY + pacmanDir.dy * 2
                    val vecX = targetX - blinky.x
                    val vecY = targetY - blinky.y
                    Pair(blinky.x + vecX * 2, blinky.y + vecY * 2)
                }
                GhostType.CLYDE -> {
                    // Chases if far (> 6 tiles), retreats to corner (1, 19) if close
                    val dist = hypot((x - pacmanX).toDouble(), (y - pacmanY).toDouble())
                    if (dist > 6.0) {
                        Pair(pacmanX, pacmanY)
                    } else {
                        Pair(1f, 19f)
                    }
                }
            }
            else -> Pair(9f, 7f)
        }
    }
}
