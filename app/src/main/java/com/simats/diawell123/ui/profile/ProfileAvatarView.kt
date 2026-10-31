package com.simats.diawell123.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.AppTheme

/**
 * Reusable Circular Avatar Component.
 * Automatically displays the cached profile photo if available,
 * or gracefully falls back to the patient's bold two-letter initials.
 * Guarantees a perfectly circular aspect ratio with customizable size and border.
 */
@Composable
fun ProfileAvatarView(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp,
    bgCircleColor: Color = if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else AppTheme.colors.border,
    borderColor: Color = if (AppTheme.colors.isDark) AppTheme.colors.border else Color.White,
    textColor: Color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
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
            .background(bgCircleColor, CircleShape)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            Text(
                text = initials,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
