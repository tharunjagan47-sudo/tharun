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
fun PrivacyPolicyScreen(
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
                        text = "Privacy Policy",
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
                text = "Effective Date: October 15, 2026 • DiaWell Health",
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
                    Text(text = "🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your privacy matters. Learn how we handle your data with care and confidentiality.",
                        fontSize = 12.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sections
            PrivacyPolicySection(
                title = "1. Information We Collect",
                body = "We collect personal details (such as name, email, age, height, and weight), glucose readings, meal logs, and medication schedules entered by you to provide tracking features.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrivacyPolicySection(
                title = "2. How We Use Your Data",
                body = "Your information is used solely to calculate health summaries, generate personalized meal suggestions, and display timely medication reminders.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrivacyPolicySection(
                title = "3. Data Storage & Security",
                body = "We implement industry-standard encryption protocols both in transit and at rest to protect your sensitive health logs against unauthorized access.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrivacyPolicySection(
                title = "4. AI Features & Privacy",
                body = "AI-assisted meal and trend suggestions utilize anonymized data parameters. We never sell or share your personal health data with third parties.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrivacyPolicySection(
                title = "5. Your Rights & Choices",
                body = "You have full control over your data. You may export a copy of your records or permanently delete your account at any time from Settings.",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "For privacy inquiries, contact us at privacy@example.com.\nDiaWell Health • Version 1.0.0",
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
fun PrivacyPolicySection(
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
fun PrivacyPolicyScreenPreview() {
    Diawell123Theme {
        PrivacyPolicyScreen()
    }
}
