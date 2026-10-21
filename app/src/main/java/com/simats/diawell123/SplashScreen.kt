package com.simats.diawell123

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC)),
    ) {
        // Soft background ambient shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left soft gradient circle
            drawCircle(
                color = Color(0xFFFBF0F8),
                radius = width * 0.45f,
                center = Offset(x = width * 0.1f, y = height * 0.08f)
            )

            // Top-right soft gradient oval/circle
            drawCircle(
                color = Color(0xFFF7EBF7),
                radius = width * 0.55f,
                center = Offset(x = width * 0.85f, y = height * 0.12f)
            )

            // Bottom-right soft ring outline
            drawCircle(
                color = Color(0xFFF6E8F6),
                radius = width * 0.48f,
                center = Offset(x = width * 0.92f, y = height * 0.85f),
                style = Stroke(width = 44.dp.toPx())
            )
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center section: Logo + Title + Tagline + Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gradient Logo Container
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(22.dp),
                            clip = false,
                            ambientColor = Color(0xFF811978).copy(alpha = 0.3f),
                            spotColor = Color(0xFF811978).copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7A1C7D),
                                    Color(0xFFBD1E85),
                                    Color(0xFFE64263),
                                    Color(0xFFFF6238)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_pulse),
                        contentDescription = "DiaWell Logo",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App Title
                Text(
                    text = "DiaWell",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A1C7D),
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tagline / Headline
                Text(
                    text = "Eat Smart. Track Better. Live Well.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF232226),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Text
                Text(
                    text = "Your daily companion for mindful meals\nand medication routines.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6E6877),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Bottom Footer Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Designed around your wellbeing",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF811978)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    Diawell123Theme {
        SplashScreen()
    }
}
