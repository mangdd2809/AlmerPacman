package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.Direction
import com.example.game.FloatingScore
import com.example.game.Fruit
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.Ghost
import com.example.game.GhostState
import com.example.game.GhostType
import com.example.game.MazeData
import com.example.game.TileType
import com.example.ui.theme.AlmerCheek
import com.example.ui.theme.AlmerEye
import com.example.ui.theme.AlmerHair
import com.example.ui.theme.AlmerHairStrand
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.GhostFlashing
import com.example.ui.theme.GhostScared
import com.example.ui.theme.MazeBlue
import com.example.ui.theme.MazeGlow
import com.example.ui.theme.PacYellow
import com.example.ui.theme.PacYellowDark
import com.example.ui.theme.PelletGold
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlmerPacmanCanvas(
    uiState: GameUiState,
    mazeGrid: Array<Array<TileType>>,
    onSwipeDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(modifier = modifier) {
        val totalCols = MazeData.COLS
        val totalRows = MazeData.ROWS

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(totalCols.toFloat() / totalRows.toFloat())
                .background(ArcadeDark)
                .pointerInput(Unit) {
                    var totalDragX = 0f
                    var totalDragY = 0f
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        },
                        onDragEnd = {
                            val threshold = 25f
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (abs(totalDragX) > threshold) {
                                    if (totalDragX > 0) onSwipeDirection(Direction.RIGHT)
                                    else onSwipeDirection(Direction.LEFT)
                                }
                            } else {
                                if (abs(totalDragY) > threshold) {
                                    if (totalDragY > 0) onSwipeDirection(Direction.DOWN)
                                    else onSwipeDirection(Direction.UP)
                                }
                            }
                        }
                    )
                }
        ) {
            val cellWidth = size.width / totalCols
            val cellHeight = size.height / totalRows
            val cellSize = minOf(cellWidth, cellHeight)

            // 1. Draw Maze Tiles (Walls, Dots, Energizers, Gate)
            drawMaze(mazeGrid, cellWidth, cellHeight, pulseScale)

            // 2. Draw Active Bonus Fruit
            uiState.activeFruit?.let { fruit ->
                drawFruit(fruit, cellWidth, cellHeight)
            }

            // 3. Draw Ghosts
            uiState.ghosts.forEach { ghost ->
                drawGhost(ghost, cellWidth, cellHeight, uiState.powerRemainingMs)
            }

            // 4. Draw Almer Pac-Man Character!
            drawAlmerPacman(
                x = uiState.pacmanX,
                y = uiState.pacmanY,
                dir = uiState.pacmanDir,
                mouthAngle = uiState.mouthAngle,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                isPowerActive = uiState.isPowerActive,
                isDying = uiState.phase == GamePhase.DYING,
                pulse = pulseScale
            )

            // 5. Draw Floating Scores
            uiState.floatingScores.forEach { fs ->
                drawFloatingScore(fs, cellWidth, cellHeight)
            }
        }
    }
}

private fun DrawScope.drawMaze(
    mazeGrid: Array<Array<TileType>>,
    cellW: Float,
    cellH: Float,
    pulse: Float
) {
    val wallStroke = cellW * 0.16f
    val wallInnerStroke = cellW * 0.08f

    for (r in 0 until MazeData.ROWS) {
        for (c in 0 until MazeData.COLS) {
            val left = c * cellW
            val top = r * cellH
            val centerX = left + cellW / 2
            val centerY = top + cellH / 2

            when (mazeGrid[r][c]) {
                TileType.WALL -> {
                    // Draw neon arcade wall tile with glowing border
                    drawRect(
                        color = Color(0xFF0C1638),
                        topLeft = Offset(left + 1f, top + 1f),
                        size = Size(cellW - 2f, cellH - 2f)
                    )
                    drawRect(
                        color = MazeBlue,
                        topLeft = Offset(left + 1.5f, top + 1.5f),
                        size = Size(cellW - 3f, cellH - 3f),
                        style = Stroke(width = wallInnerStroke)
                    )
                }
                TileType.DOT -> {
                    // Small food pellet
                    drawCircle(
                        color = PelletGold,
                        radius = cellW * 0.13f,
                        center = Offset(centerX, centerY)
                    )
                }
                TileType.ENERGIZER -> {
                    // Big glowing power pellet
                    drawCircle(
                        color = Color(0x55FFE082),
                        radius = cellW * 0.40f * pulse,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color(0xFFFFF176),
                        radius = cellW * 0.28f * pulse,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = cellW * 0.14f,
                        center = Offset(centerX, centerY)
                    )
                }
                TileType.GATE -> {
                    // Pink / white glowing gate bar
                    drawLine(
                        color = Color(0xFFFF4081),
                        start = Offset(left, centerY),
                        end = Offset(left + cellW, centerY),
                        strokeWidth = wallStroke,
                        cap = StrokeCap.Round
                    )
                }
                TileType.GHOST_HOUSE -> {
                    drawRect(
                        color = Color(0xFF0E1328),
                        topLeft = Offset(left, top),
                        size = Size(cellW, cellH)
                    )
                }
                else -> Unit
            }
        }
    }
}

/**
 * Draws Almer Pac-Man with distinctive features matching the photo:
 * - Round circular body with opening and closing chomping mouth.
 * - Almer's distinctive dark fringe/bangs hair covering the upper forehead.
 * - Expressive dark almond eyes with playful catchlight pupil.
 * - Soft rosy cheeks reflecting Almer's cute child face.
 * - Power-up aura when energized.
 */
private fun DrawScope.drawAlmerPacman(
    x: Float,
    y: Float,
    dir: Direction,
    mouthAngle: Float,
    cellWidth: Float,
    cellHeight: Float,
    isPowerActive: Boolean,
    isDying: Boolean,
    pulse: Float
) {
    val centerX = x * cellWidth + cellWidth / 2
    val centerY = y * cellHeight + cellHeight / 2
    val radius = minOf(cellWidth, cellHeight) * 0.58f

    if (isDying) {
        // Shrinking spinning death animation
        drawCircle(
            color = Color(0xFFE57373),
            radius = radius * 0.5f,
            center = Offset(centerX, centerY)
        )
        return
    }

    // Power Aura Glow
    if (isPowerActive) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x9900E5FF), Color(0x33FFD54F), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = radius * 1.55f * pulse
            ),
            radius = radius * 1.55f * pulse,
            center = Offset(centerX, centerY)
        )
    }

    val rotation = when (dir) {
        Direction.RIGHT -> 0f
        Direction.DOWN -> 90f
        Direction.LEFT -> 180f
        Direction.UP -> 270f
        Direction.NONE -> 0f
    }

    rotate(degrees = rotation, pivot = Offset(centerX, centerY)) {
        // Red Boots (Chunky boots of the figure stepping)
        val bootWidth = radius * 0.45f
        val bootHeight = radius * 0.35f
        drawRoundRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(centerX - radius * 0.75f, centerY + radius * 0.72f),
            size = Size(bootWidth, bootHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.15f, radius * 0.15f)
        )
        drawRoundRect(
            color = Color(0xFFB71C1C),
            topLeft = Offset(centerX - radius * 0.25f, centerY + radius * 0.76f),
            size = Size(bootWidth, bootHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.15f, radius * 0.15f)
        )

        // 1. Pac-Man Circular Head with Chomp Cutout
        val sweepAngle = 360f - mouthAngle * 2
        val startAngle = mouthAngle

        // Dark inner mouth background cavity
        if (mouthAngle > 10f) {
            drawCircle(
                color = Color(0xFF1A0A05),
                radius = radius * 0.95f,
                center = Offset(centerX, centerY)
            )
            // Cute pink/red tongue inside
            drawArc(
                color = Color(0xFFFF5252),
                startAngle = 10f,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(centerX - radius * 0.5f, centerY - radius * 0.1f),
                size = Size(radius * 1.2f, radius * 0.8f)
            )
        }

        drawArc(
            color = PacYellow,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Fill
        )

        // Subtle shaded border for depth
        drawArc(
            color = PacYellowDark,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = radius * 0.1f)
        )

        // 2. Cute Rosy Cheek
        drawCircle(
            color = AlmerCheek,
            radius = radius * 0.22f,
            center = Offset(centerX - radius * 0.25f, centerY + radius * 0.28f)
        )

        // 3. Almer's Big Dark Eye with Twinkle
        val eyeCenterX = centerX + radius * 0.08f
        val eyeCenterY = centerY - radius * 0.38f
        val eyeRadiusX = radius * 0.18f
        val eyeRadiusY = radius * 0.22f

        // Eye White & Iris
        drawCircle(
            color = Color.White,
            radius = eyeRadiusX * 1.1f,
            center = Offset(eyeCenterX, eyeCenterY)
        )
        drawCircle(
            color = AlmerEye,
            radius = eyeRadiusX,
            center = Offset(eyeCenterX + eyeRadiusX * 0.25f, eyeCenterY)
        )
        // Catchlight pupil shine
        drawCircle(
            color = Color.White,
            radius = eyeRadiusX * 0.35f,
            center = Offset(eyeCenterX + eyeRadiusX * 0.45f, eyeCenterY - eyeRadiusY * 0.25f)
        )

        // Eyebrow
        drawLine(
            color = AlmerHair,
            start = Offset(eyeCenterX - eyeRadiusX * 0.9f, eyeCenterY - eyeRadiusY * 1.25f),
            end = Offset(eyeCenterX + eyeRadiusX * 1.1f, eyeCenterY - eyeRadiusY * 1.1f),
            strokeWidth = radius * 0.08f,
            cap = StrokeCap.Round
        )

        // 4. Almer's Signature Dark Fringe / Bangs Haircut
        // Drawn on the upper forehead matching the photo's fringe cut
        val hairPath = Path().apply {
            moveTo(centerX - radius * 0.85f, centerY - radius * 0.25f)
            // Arch along top of head
            cubicTo(
                centerX - radius * 0.75f, centerY - radius * 0.95f,
                centerX + radius * 0.15f, centerY - radius * 0.95f,
                centerX + radius * 0.75f, centerY - radius * 0.45f
            )
            // Front fringe / bangs points (Almer's signature look!)
            lineTo(centerX + radius * 0.55f, centerY - radius * 0.50f)
            lineTo(centerX + radius * 0.45f, centerY - radius * 0.65f)
            lineTo(centerX + radius * 0.30f, centerY - radius * 0.48f)
            lineTo(centerX + radius * 0.15f, centerY - radius * 0.68f)
            lineTo(centerX - radius * 0.05f, centerY - radius * 0.52f)
            lineTo(centerX - radius * 0.25f, centerY - radius * 0.66f)
            lineTo(centerX - radius * 0.45f, centerY - radius * 0.48f)
            lineTo(centerX - radius * 0.65f, centerY - radius * 0.58f)
            close()
        }

        drawPath(path = hairPath, color = AlmerHair)

        // Hair shine strands
        drawLine(
            color = AlmerHairStrand,
            start = Offset(centerX - radius * 0.5f, centerY - radius * 0.82f),
            end = Offset(centerX + radius * 0.3f, centerY - radius * 0.82f),
            strokeWidth = radius * 0.07f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF5D4037),
            start = Offset(centerX - radius * 0.2f, centerY - radius * 0.88f),
            end = Offset(centerX + radius * 0.45f, centerY - radius * 0.70f),
            strokeWidth = radius * 0.05f,
            cap = StrokeCap.Round
        )

        // 5. Orange / Red Boxing Glove Hand holding a glowing white pellet (Image 2 style)
        val gloveX = centerX + radius * 0.75f
        val gloveY = centerY + radius * 0.25f
        // Glove
        drawCircle(
            color = Color(0xFFE64A19),
            radius = radius * 0.30f,
            center = Offset(gloveX, gloveY)
        )
        drawCircle(
            color = Color(0xFFFF5722),
            radius = radius * 0.22f,
            center = Offset(gloveX - radius * 0.05f, gloveY - radius * 0.05f)
        )

        // Glowing white pellet held right at mouth
        val pelletX = centerX + radius * 0.88f
        val pelletY = centerY
        drawCircle(
            color = Color(0x99B3E5FC),
            radius = radius * 0.28f,
            center = Offset(pelletX, pelletY)
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.18f,
            center = Offset(pelletX, pelletY)
        )
    }
}

private fun DrawScope.drawGhost(
    ghost: Ghost,
    cellW: Float,
    cellH: Float,
    powerRemainingMs: Long
) {
    val centerX = ghost.x * cellW + cellW / 2
    val centerY = ghost.y * cellH + cellH / 2
    val radius = minOf(cellW, cellH) * 0.52f

    if (ghost.state == GhostState.EATEN) {
        // Draw only floating white eyes returning home
        drawGhostEyesOnly(centerX, centerY, ghost.dir, radius)
        return
    }

    // Ghost body color based on state
    val isFlashing = ghost.state == GhostState.FRIGHTENED && powerRemainingMs < 2500L && (powerRemainingMs / 250L) % 2L == 0L
    val bodyColor = when {
        isFlashing -> GhostFlashing
        ghost.state == GhostState.FRIGHTENED -> GhostScared
        else -> ghost.type.baseColor
    }

    // Ghost body path (dome top, straight sides, wavy skirt)
    val path = Path().apply {
        val top = centerY - radius
        val bottom = centerY + radius
        val left = centerX - radius
        val right = centerX + radius

        moveTo(left, centerY)
        // Top dome
        cubicTo(left, top, right, top, right, centerY)
        // Right side down
        lineTo(right, bottom)
        // 3 wavy skirt tentacles
        val w = (right - left) / 3f
        lineTo(right - w * 0.5f, bottom - radius * 0.35f)
        lineTo(right - w, bottom)
        lineTo(left + w * 1.5f, bottom - radius * 0.35f)
        lineTo(left + w, bottom)
        lineTo(left + w * 0.5f, bottom - radius * 0.35f)
        lineTo(left, bottom)
        close()
    }

    drawPath(path = path, color = bodyColor)

    // Ghost Eyes
    if (ghost.state == GhostState.FRIGHTENED) {
        // Scared beady eyes + squiggly mouth
        val eyeColor = if (isFlashing) Color(0xFF1E3A8A) else Color(0xFFFFF176)
        drawCircle(color = eyeColor, radius = radius * 0.16f, center = Offset(centerX - radius * 0.35f, centerY - radius * 0.15f))
        drawCircle(color = eyeColor, radius = radius * 0.16f, center = Offset(centerX + radius * 0.35f, centerY - radius * 0.15f))

        // Wavy frightened mouth
        val mouthColor = if (isFlashing) Color(0xFFFF3333) else Color(0xFFFF8A80)
        val mouthY = centerY + radius * 0.35f
        drawLine(
            color = mouthColor,
            start = Offset(centerX - radius * 0.45f, mouthY),
            end = Offset(centerX - radius * 0.15f, mouthY - radius * 0.15f),
            strokeWidth = radius * 0.12f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = mouthColor,
            start = Offset(centerX - radius * 0.15f, mouthY - radius * 0.15f),
            end = Offset(centerX + radius * 0.15f, mouthY),
            strokeWidth = radius * 0.12f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = mouthColor,
            start = Offset(centerX + radius * 0.15f, mouthY),
            end = Offset(centerX + radius * 0.45f, mouthY - radius * 0.15f),
            strokeWidth = radius * 0.12f,
            cap = StrokeCap.Round
        )
    } else {
        // Normal eyes looking in movement direction
        drawGhostEyes(centerX, centerY, ghost.dir, radius)
    }
}

private fun DrawScope.drawGhostEyes(
    centerX: Float,
    centerY: Float,
    dir: Direction,
    radius: Float
) {
    val pupilOffset = radius * 0.14f
    val pdx = dir.dx * pupilOffset
    val pdy = dir.dy * pupilOffset

    val eyeLeftX = centerX - radius * 0.38f
    val eyeRightX = centerX + radius * 0.38f
    val eyeY = centerY - radius * 0.18f

    // Eye whites
    drawCircle(color = Color.White, radius = radius * 0.28f, center = Offset(eyeLeftX, eyeY))
    drawCircle(color = Color.White, radius = radius * 0.28f, center = Offset(eyeRightX, eyeY))

    // Blue pupils
    drawCircle(color = Color(0xFF1565C0), radius = radius * 0.15f, center = Offset(eyeLeftX + pdx, eyeY + pdy))
    drawCircle(color = Color(0xFF1565C0), radius = radius * 0.15f, center = Offset(eyeRightX + pdx, eyeY + pdy))
}

private fun DrawScope.drawGhostEyesOnly(
    centerX: Float,
    centerY: Float,
    dir: Direction,
    radius: Float
) {
    val pupilOffset = radius * 0.16f
    val pdx = dir.dx * pupilOffset
    val pdy = dir.dy * pupilOffset

    val eyeLeftX = centerX - radius * 0.35f
    val eyeRightX = centerX + radius * 0.35f
    val eyeY = centerY - radius * 0.1f

    drawCircle(color = Color.White, radius = radius * 0.32f, center = Offset(eyeLeftX, eyeY))
    drawCircle(color = Color.White, radius = radius * 0.32f, center = Offset(eyeRightX, eyeY))

    drawCircle(color = Color(0xFF1E88E5), radius = radius * 0.18f, center = Offset(eyeLeftX + pdx, eyeY + pdy))
    drawCircle(color = Color(0xFF1E88E5), radius = radius * 0.18f, center = Offset(eyeRightX + pdx, eyeY + pdy))
}

private fun DrawScope.drawFruit(
    fruit: Fruit,
    cellW: Float,
    cellH: Float
) {
    val centerX = fruit.x * cellW + cellW / 2
    val centerY = fruit.y * cellH + cellH / 2
    val radius = minOf(cellW, cellH) * 0.55f

    // Fruit background circle
    drawCircle(color = Color(0x33FF4081), radius = radius * 1.3f, center = Offset(centerX, centerY))

    // Draw native text icon
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = radius * 2.2f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText(fruit.type.iconResSymbol, centerX, centerY + radius * 0.7f, paint)
    }
}

private fun DrawScope.drawFloatingScore(
    fs: FloatingScore,
    cellW: Float,
    cellH: Float
) {
    val centerX = fs.x * cellW + cellW / 2
    val centerY = fs.y * cellH + cellH / 2
    val alphaInt = (fs.alpha.coerceIn(0f, 1f) * 255).toInt()

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(alphaInt, 0, 245, 212) // Neon Cyan
            textSize = cellW * 0.9f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText("+${fs.points}", centerX, centerY, paint)
    }
}
