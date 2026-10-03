package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeCard
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.GhostBlinky
import com.example.ui.theme.GhostClyde
import com.example.ui.theme.GhostInky
import com.example.ui.theme.GhostPinky
import com.example.ui.theme.NeonAccent
import com.example.ui.theme.PacYellow

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Surface(
        color = ArcadeDark,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("how_to_play_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = NeonAccent
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PANDUAN BERMAIN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Guide
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎮 Kontrol Permainan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NeonAccent)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Virtual D-Pad: Tekan tombol panah di bagian bawah layar untuk mengarahkan Almer.\n• Usap Layar (Swipe): Geser jari ke arah atas, bawah, kiri, atau kanan di area labirin untuk berbelok otomatis.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, lineHeight = 20.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scoring Guide
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⭐ Poin & Makanan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PacYellow)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreItem("🟡 Dot Kuning", "10 Poin", "Makanan pokok yang harus dihabiskan untuk menang!")
                    ScoreItem("✨ Power Pellet", "50 Poin", "Membuat hantu berwarna biru dan bisa dimakan Almer!")
                    ScoreItem("👻 Makan Hantu", "200 - 1.600 Poin", "Setiap hantu berurutan bernilai ganda (200, 400, 800, 1.600)!")
                    ScoreItem("🍒 Buah Bonus", "100 - 1.000 Poin", "Ceri, Stroberi, Jeruk, Apel & Kunci Emas di tengah labirin.")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ghost Characters Guide
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👻 Mengenal 4 Hantu",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GhostGuideItem(GhostBlinky, "Blinky (Si Merah)", "Pengejar tangguh. Selalu mengincar langsung ke posisi Almer.")
                    GhostGuideItem(GhostPinky, "Pinky (Si Manis)", "Penghadang cerdik. Berusaha memotong jalan di depan Almer.")
                    GhostGuideItem(GhostInky, "Inky (Si Biru)", "Pengepung tak terduga. Bekerja sama dengan Blinky menyudutkan Almer.")
                    GhostGuideItem(GhostClyde, "Clyde (Si Penakut)", "Mengejar saat jauh, namun berbalik kabur saat mendekati Almer.")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ScoreItem(name: String, points: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Text(text = points, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, color = PacYellow))
        }
        Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp))
    }
}

@Composable
private fun GhostGuideItem(color: Color, name: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = color))
            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp))
        }
    }
}
