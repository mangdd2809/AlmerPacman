package com.example.game

enum class FruitType(val fruitName: String, val points: Int, val iconResSymbol: String) {
    CHERRY("Ceri", 100, "🍒"),
    STRAWBERRY("Stroberi", 300, "🍓"),
    ORANGE("Jeruk", 500, "🍊"),
    APPLE("Apel", 700, "🍎"),
    KEY("Kunci Emas", 1000, "🔑")
}

data class Fruit(
    val type: FruitType,
    val x: Float = 9f,
    val y: Float = 12f,
    var remainingTimeMs: Long = 10000L
)
