package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.simats.diawell123.ui.profile.ProfileState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.ai.IronmanDoctorAvatar
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun HelpSupportScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
    onAiChatbotClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var expandedFaq by remember { mutableIntStateOf(0) }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    val essentialFaqs = remember {
        listOf(
            "How do I log glucose?" to "Open Glucose from Quick Actions or the tab bar, enter your reading and time, select Fasting or Post-meal, and save. Follow the routine agreed with your healthcare team.",
            "How do I manage reminders?" to "Go to Settings > Notification Preferences to turn reminders on or off and customize alert sounds and quiet hours.",
            "How are food suggestions generated?" to "Meal ideas are tailored by DIVA (DiaWell Intelligent Virtual Assistant) based on your preferred diet, food allergies, and daily glucose levels set in your profile.",
            "Is my data secure and private?" to "Yes, DiaWell encrypts all health data in transit and on server storage. You can delete your account or change data sharing preferences anytime."
        )
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onContactSupportClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StyledBackButton(onBackClick = onBackClick)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Help & Support",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1-Second Success Confirmation Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.successBg)
                        .border(1.5.dp, AppTheme.colors.successBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = AppTheme.colors.successText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Support Message Sent Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.successText,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Our team will reply to your registered email shortly.",
                                fontSize = 11.sp,
                                color = AppTheme.colors.successText,
                            )
                        }
                    }
                }
            }

            // Help Header Banner (Centered visuals & text)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Help,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "How can we help?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Common guides & care support",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // FAQ Section Header
            Text(
                text = "Frequently asked questions (${essentialFaqs.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(12.dp))

            essentialFaqs.forEachIndexed { index, (q, a) ->
                FaqAccordionItem(
                    title = q,
                    answer = a,
                    isExpanded = expandedFaq == index,
                    onClick = { expandedFaq = if (expandedFaq == index) -1 else index },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Medical Helpline Card (1-Tap Dialer to 108)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.errorBg)
                    .border(1.5.dp, AppTheme.colors.errorBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                        try {
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Call Emergency Services: 108", Toast.LENGTH_LONG).show()
                        }
                    }
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) Color(0xFF7F1D1D).copy(alpha = 0.55f) else Color(0xFFFFCDD2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Emergency,
                            contentDescription = null,
                            tint = AppTheme.colors.errorText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Medical Helpline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.errorText,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to call 108 Ambulance & Emergency Support",
                            fontSize = 11.sp,
                            color = AppTheme.colors.errorText,
                            lineHeight = 15.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD32F2F))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "108",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dr. Jarvis AI Assistant Shortcut Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) Color(0xFF1E293B) else Color(0xFFF3E8FF))
                    .border(1.5.dp, Color(0xFF9333EA), RoundedCornerShape(14.dp))
                    .clickable { onAiChatbotClick() }
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IronmanDoctorAvatar(sizeDp = 42)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ask DIVA (DiaWell Virtual Assistant)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (AppTheme.colors.isDark) Color(0xFFC084FC) else Color(0xFF7E22CE),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instant 24/7 answers to diabetes & lifestyle queries",
                            fontSize = 11.sp,
                            color = if (AppTheme.colors.isDark) Color(0xFFCBD5E1) else Color(0xFF581C87),
                            lineHeight = 15.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (AppTheme.colors.isDark) Color(0xFF7E22CE) else Color(0xFF9333EA))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CHAT ›",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Support Email Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                    .clickable {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@diawell.com")
                            putExtra(Intent.EXTRA_SUBJECT, "DiaWell Question - ${ProfileState.userName}")
                        }
                        try {
                            context.startActivity(emailIntent)
                            Toast.makeText(context, "Opening email to support@diawell.com", Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {
                            Toast.makeText(context, "Support: support@diawell.com", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.secondaryBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "support@diawell.com",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Official Support • Tap to compose message",
                            fontSize = 11.sp,
                            color = AppTheme.colors.textSecondary,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Support Button
            IosLiquidGlassButton(
                text = "Contact Support",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@diawell.com")
                        putExtra(Intent.EXTRA_SUBJECT, "DiaWell Support Request - ${ProfileState.userName}")
                        putExtra(Intent.EXTRA_TEXT, "Hello DiaWell Support,\n\nI need assistance with:\n")
                    }
                    try {
                        context.startActivity(emailIntent)
                    } catch (_: Exception) {}
                    Toast.makeText(context, "Opening support email draft...", Toast.LENGTH_SHORT).show()
                    showSuccessBanner = true
                },
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun FaqAccordionItem(
    title: String,
    answer: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelpSupportScreenPreview() {
    Diawell123Theme {
        HelpSupportScreen()
    }
}
