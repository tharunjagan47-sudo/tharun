package com.simats.diawell123.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileAvatarView(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp,
    bgCircleColor: Color = Color(0xFFF3E2F0),
    textColor: Color = Color(0xFF811978),
    fontSizeSp: Int = 16,
) {
    val bitmap = ProfileState.profileImageBitmap
    val initials = ProfileState.userName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "MJ" }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(bgCircleColor)
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(sizeDp)
                    .clip(CircleShape),
            )
        } else {
            Text(
                text = initials,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}
