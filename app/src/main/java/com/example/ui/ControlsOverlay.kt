package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Direction
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeCard
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.NeonAccent
import com.example.ui.theme.PacYellow

@Composable
fun TopGameHeader(
    uiState: GameUiState,
    onPauseToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArcadeSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = ArcadeBorder, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score & High Score
            Column {
                Text(
                    text = "SKOR ALMER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAccent
                    )
                )
                Text(
                    text = String.format("%,d", uiState.score),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("score_display")
                )
            }

            // High Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REKOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PacYellow
                    )
                )
                Text(
                    text = String.format("%,d", maxOf(uiState.score, uiState.highScore)),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PacYellow
                    )
                )
            }

            // Level & Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArcadeCard)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LVL ${uiState.level}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = NeonAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onSoundToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Suara",
                        tint = if (uiState.soundEnabled) PacYellow else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("pause_game_button")
                ) {
                    Icon(
                        imageVector = if (uiState.phase == GamePhase.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Jeda Game",
                        tint = NeonAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomHud(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lives represented by mini Almer icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "NYAWA: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            )
            for (i in 0 until uiState.lives) {
                MiniAlmerLifeIcon(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }

        // Difficulty tag & Power Pellet Timer Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.isPowerActive) {
                val secLeft = (uiState.powerRemainingMs / 1000f)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE91E63))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "POWER: ${String.format("%.1fs", secLeft)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArcadeCard)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = uiState.difficulty.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBAC5E8)
                    )
                )
            }
        }
    }
}

@Composable
fun MiniAlmerLifeIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(PacYellow)
            .border(1.dp, Color(0xFFF57F17), CircleShape)
    ) {
        // Mini dark hair fringe
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 7.dp)
                .align(Alignment.TopCenter)
                .background(Color(0xFF261C14))
        )
    }
}

@Composable
fun VirtualArcadeDpad(
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // UP
        DpadButton(
            icon = Icons.Default.ArrowUpward,
            contentDescription = "Atas",
            onClick = { onDirection(Direction.UP) },
            tag = "dpad_up"
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT
            DpadButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Kiri",
                onClick = { onDirection(Direction.LEFT) },
                tag = "dpad_left"
            )

            // Center Arcade Hub
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E2E5D), ArcadeDark)
                        )
                    )
                    .border(2.dp, ArcadeBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ALMER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonAccent
                    )
                )
            }

            // RIGHT
            DpadButton(
                icon = Icons.Default.ArrowForward,
                contentDescription = "Kanan",
                onClick = { onDirection(Direction.RIGHT) },
                tag = "dpad_right"
            )
        }

        // DOWN
        DpadButton(
            icon = Icons.Default.ArrowDownward,
            contentDescription = "Bawah",
            onClick = { onDirection(Direction.DOWN) },
            tag = "dpad_down"
        )
    }
}

@Composable
private fun DpadButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(58.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A264D), Color(0xFF101935))
                )
            )
            .border(1.5.dp, Color(0xFF2F4588), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = NeonAccent),
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PacYellow,
            modifier = Modifier.size(30.dp)
        )
    }
}
