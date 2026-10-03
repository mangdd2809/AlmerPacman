package com.example.game

data class FloatingScore(
    val id: Long = System.nanoTime(),
    val points: Int,
    val x: Float,
    var y: Float,
    var alpha: Float = 1.0f
)
