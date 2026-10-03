package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.Direction
import com.example.game.GameDifficulty
import com.example.game.MazeData
import com.example.game.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Almer Pac-Man", appName)
  }

  @Test
  fun `verify maze grid dimensions and wall integrity`() {
    val tiles = MazeData.createInitialTiles()
    assertEquals(MazeData.ROWS, tiles.size)
    assertEquals(MazeData.COLS, tiles[0].size)

    // Verify boundary walls
    for (c in 0 until MazeData.COLS) {
      assertEquals(TileType.WALL, tiles[0][c])
      assertEquals(TileType.WALL, tiles[MazeData.ROWS - 1][c])
    }

    // Verify Pac-Man start position at (15, 9) is empty
    assertEquals(TileType.EMPTY, tiles[15][9])
  }

  @Test
  fun `verify direction opposites`() {
    assertEquals(Direction.DOWN, Direction.UP.opposite())
    assertEquals(Direction.UP, Direction.DOWN.opposite())
    assertEquals(Direction.RIGHT, Direction.LEFT.opposite())
    assertEquals(Direction.LEFT, Direction.RIGHT.opposite())
  }

  @Test
  fun `verify difficulty parameters`() {
    assertTrue(GameDifficulty.TURBO.pacmanSpeed > GameDifficulty.SANTAI.pacmanSpeed)
    assertTrue(GameDifficulty.TURBO.scoreMultiplier > GameDifficulty.SANTAI.scoreMultiplier)
  }
}
