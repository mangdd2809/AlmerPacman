package com.example.game

object MazeData {
    const val COLS = 19
    const val ROWS = 21

    val MAP_LAYOUT = listOf(
        "###################", // 0
        "#........#........#", // 1
        "#O##.###.#.###.##O#", // 2
        "#.##.###.#.###.##.#", // 3
        "#.................#", // 4
        "#.##.#.#####.#.##.#", // 5
        "#....#...#...#....#", // 6
        "####.### # ###.####", // 7
        "   #.#  HHH  #.#   ", // 8
        "####.# HHGHH #.####", // 9 (Gate G at col 9)
        "         H         ", // 10 (Wrap Tunnel row)
        "####.# HHHHH #.####", // 11
        "   #.#       #.#   ", // 12
        "####.# ##### #.####", // 13
        "#........#........#", // 14
        "#.##.###.#.###.##.#", // 15
        "#O.#...........#.O#", // 16
        "##.#.#.#####.#.#.##", // 17
        "#....#...#...#....#", // 18
        "#.######.#.######.#", // 19
        "###################"  // 20
    )

    fun createInitialTiles(): Array<Array<TileType>> {
        val grid = Array(ROWS) { r ->
            Array(COLS) { c ->
                val char = MAP_LAYOUT[r][c]
                when (char) {
                    '#' -> TileType.WALL
                    '.' -> TileType.DOT
                    'O' -> TileType.ENERGIZER
                    'G' -> TileType.GATE
                    'H' -> TileType.GHOST_HOUSE
                    else -> TileType.EMPTY
                }
            }
        }
        // Ensure starting tile under Pacman is empty
        grid[15][9] = TileType.EMPTY
        return grid
    }

    fun isWall(grid: Array<Array<TileType>>, col: Int, row: Int, allowGate: Boolean = false): Boolean {
        if (row !in 0 until ROWS) return true
        if (col !in 0 until COLS) {
            // Tunnel rows allow wrapping
            return row != 10
        }
        val tile = grid[row][col]
        return if (allowGate) {
            tile == TileType.WALL
        } else {
            tile == TileType.WALL || tile == TileType.GATE
        }
    }
}
