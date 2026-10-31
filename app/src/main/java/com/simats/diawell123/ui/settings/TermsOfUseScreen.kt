package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun TermsOfUseScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(4.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Terms of Use",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Last Updated: October 15, 2026 • DiaWell",
                fontSize = 11.sp,
                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📜", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Please read these terms carefully before using the DiaWell application.",
                        fontSize = 12.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Terms Sections
            TermsSection(
                title = "1. Medical Disclaimer",
                body = "DiaWell provides general wellness tracking, self-logging, and meal ideas. DiaWell is NOT a medical device, diagnosis tool, or substitute for professional healthcare advice. Always consult your doctor or clinician regarding medical conditions or medication adjustments.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            TermsSection(
                title = "2. User Responsibilities",
                body = "You are responsible for maintaining the accuracy of information entered into the app and for keeping your account credentials secure.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            TermsSection(
                title = "3. Acceptable Use",
                body = "You agree to use DiaWell solely for personal, non-commercial health tracking purposes and in compliance with all applicable local laws.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            TermsSection(
                title = "4. Intellectual Property",
                body = "All software, branding, UI designs, graphics, and AI features in DiaWell are protected intellectual property owned by DiaWell Health.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            TermsSection(
                title = "5. Limitation of Liability",
                body = "DiaWell Health shall not be liable for health decisions, outcomes, or actions taken based on general dietary suggestions or logging data.",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By using DiaWell, you acknowledge that you have read and agree to these terms.\nDiaWell Health • Version 1.0.0",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
fun TermsSection(
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
                lineHeight = 17.sp,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsOfUseScreenPreview() {
    Diawell123Theme {
        TermsOfUseScreen()
    }
}
