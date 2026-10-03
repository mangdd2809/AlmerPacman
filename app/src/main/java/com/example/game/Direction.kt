package com.example.game

enum class Direction(val dx: Int, val dy: Int, val rotationDegrees: Float) {
    NONE(0, 0, 0f),
    UP(0, -1, 270f),
    DOWN(0, 1, 90f),
    LEFT(-1, 0, 180f),
    RIGHT(1, 0, 0f);

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
        NONE -> NONE
    }
}
