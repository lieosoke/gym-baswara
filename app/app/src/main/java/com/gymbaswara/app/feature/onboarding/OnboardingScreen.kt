package com.gymbaswara.app.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt

private data class OnboardingPage(val title: String, val description: String)

private val pages = listOf(
    OnboardingPage(
        title = "Latih Lebih Terukur",
        description = "Catat setiap set, beban, dan repetisi tanpa hambatan saat Anda fokus di gym."
    ),
    OnboardingPage(
        title = "Pantau Rekor & Progres",
        description = "Lihat grafik pencapaian beban maksimal dan volume latihan yang terus meningkat."
    ),
    OnboardingPage(
        title = "Data Aman Tersinkron",
        description = "Rutinitas dan riwayat latihan tersimpan rapi dan dapat dicadangkan ke Google Drive."
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var index by remember { mutableIntStateOf(0) }
    val isLast = index == pages.lastIndex
    val page = pages[index]

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.finish(onFinished) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Lewati",
                        color = AppleGraySubtext,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Step Badge Squircle
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(AppleVolt),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Title & Description
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = AppleGraySubtext,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Dynamic Pagination Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { i ->
                    val isSelected = i == index
                    val width by animateDpAsState(targetValue = if (isSelected) 28.dp else 8.dp, label = "dotWidth")
                    val color by animateColorAsState(targetValue = if (isSelected) AppleVolt else AppleGrayDarkControl, label = "dotColor")

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom CTA
            Button(
                onClick = {
                    if (isLast) viewModel.finish(onFinished) else index++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleVolt,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isLast) "Mulai Sekarang" else "Lanjut",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
