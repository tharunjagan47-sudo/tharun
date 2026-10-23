package com.simats.diawell123.ui.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.ai.IronmanDoctorAvatar
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.profile.ProfileAvatarView
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogGlucoseClick: () -> Unit = {},
    onTrackFoodClick: () -> Unit = {},
    onActivityTrackingClick: () -> Unit = {},
    onDietChatbotClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Home") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Premium Hero Header Banner Card (Pops Up)
                ElementPopUpContainer(delayMillis = 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6B156E),
                                        Color(0xFFB81D85),
                                        Color(0xFFFF5722),
                                    ),
                                ),
                            )
                            .padding(18.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "Good morning, ${ProfileState.userName} 👋",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = "Daily Wellness • 94% On Track",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }

                                // Profile Avatar Circle
                                Box(
                                    modifier = Modifier.clickable { onProfileClick() },
                                ) {
                                    ProfileAvatarView(
                                        sizeDp = 42.dp,
                                        fontSizeSp = 15,
                                        bgCircleColor = Color.White.copy(alpha = 0.25f),
                                        textColor = Color.White,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    Text(
                                        text = "🔥 7 Day Logging Streak",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    Text(
                                        text = "🎯 Fasting Target Met",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title: Key Health Metrics
                Text(
                    text = "Key Health Metrics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Colorful Metrics Grid (Pops Up)
                ElementPopUpContainer(delayMillis = 80) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Glucose Metric Card
                            MetricCard(
                                icon = "🩸",
                                title = "Glucose",
                                value = "128 mg/dL",
                                subtitle = "Fasting • 7:15 AM",
                                statusText = "In Target Range",
                                statusBg = Color(0xFFE8F5E9),
                                statusColor = Color(0xFF2E7D32),
                                cardBg = Color(0xFFFDEEF7),
                                borderColor = Color(0xFFF3D5E9),
                                actionText = "+ Log",
                                onActionClick = onLogGlucoseClick,
                                modifier = Modifier.weight(1f),
                            )

                            // Today's Diet Card
                            MetricCard(
                                icon = "🥗",
                                title = "Nutrition",
                                value = "1,450 kcal",
                                subtitle = "3 of 4 meals logged",
                                statusText = "C:160g • P:75g",
                                statusBg = Color(0xFFE8F5E9),
                                statusColor = Color(0xFF2E7D32),
                                cardBg = Color(0xFFEBF7F0),
                                borderColor = Color(0xFFD4EFE0),
                                actionText = "Explore",
                                onActionClick = onTrackFoodClick,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Medication Card
                            MetricCard(
                                icon = "💊",
                                title = "Medication",
                                value = "Metformin",
                                subtitle = "500mg • 8:00 PM",
                                statusText = "Upcoming Dose",
                                statusBg = Color(0xFFFFF3E0),
                                statusColor = Color(0xFFE65100),
                                cardBg = Color(0xFFFFF8EE),
                                borderColor = Color(0xFFFFECC8),
                                actionText = "View",
                                onActionClick = onMedsTabClick,
                                modifier = Modifier.weight(1f),
                            )

                            // Weekly Progress Card
                            MetricCard(
                                icon = "📊",
                                title = "Weekly Report",
                                value = "7/7 Days",
                                subtitle = "Glucose & Diet Logged",
                                statusText = "Excellent Routine",
                                statusBg = Color(0xFFE3F2FD),
                                statusColor = Color(0xFF1565C0),
                                cardBg = Color(0xFFF0F7FF),
                                borderColor = Color(0xFFD0E4FC),
                                actionText = "Reports",
                                onActionClick = onReportsTabClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Featured Dr. JARVIS AI Daily Tip Card (Pops Up)
                ElementPopUpContainer(delayMillis = 140) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFDEEF7),
                                        Color(0xFFFFF0F5),
                                    ),
                                ),
                            )
                            .border(1.5.dp, Color(0xFFF3D5E9), RoundedCornerShape(18.dp))
                            .clickable { onDietChatbotClick() }
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IronmanDoctorAvatar(sizeDp = 44)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Dr. JARVIS Tip Of The Day ⚡",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB81D85),
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A 10-minute light walk after meals helps smooth out blood sugar levels effectively!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF232226),
                                    lineHeight = 17.sp,
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Ask →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Today's Routine Timeline Check-In (Pops Up)
                ElementPopUpContainer(delayMillis = 200) {
                    Column {
                        Text(
                            text = "Today's Check-In Routine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RoutineCheckItem(icon = "🥣", label = "Breakfast", isDone = true)
                                RoutineCheckItem(icon = "🩸", label = "Glucose", isDone = true)
                                RoutineCheckItem(icon = "🥗", label = "Lunch", isDone = true)
                                RoutineCheckItem(icon = "💊", label = "Dinner/Meds", isDone = false)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Vibrant Quick Actions (Pops Up)
                ElementPopUpContainer(delayMillis = 260) {
                    Column {
                        Text(
                            text = "Quick Actions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            VibrantQuickActionButton(
                                title = "Log Glucose",
                                icon = "🩸",
                                gradientColors = listOf(Color(0xFFE53935), Color(0xFFFF5722)),
                                onClick = onLogGlucoseClick,
                                modifier = Modifier.weight(1f),
                            )
                            VibrantQuickActionButton(
                                title = "Activity & Workout",
                                icon = "🏃‍♂️",
                                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF00838F)),
                                onClick = onActivityTrackingClick,
                                modifier = Modifier.weight(1f),
                            )
                            VibrantQuickActionButton(
                                title = "JARVIS AI",
                                icon = "💬",
                                gradientColors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                onClick = onDietChatbotClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = { selectedTab = "Home" },
                onDietTabClick = onDietTabClick,
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun IosLiquidGlassBottomNav(
    selectedTab: String,
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Floating iOS Liquid Glass Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color(0xFF811978).copy(alpha = 0.20f),
                    spotColor = Color(0xFF811978).copy(alpha = 0.25f),
                )
                .clip(RoundedCornerShape(36.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.94f),
                            Color(0xFFF5EDF6).copy(alpha = 0.90f),
                        ),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.5f),
                        ),
                    ),
                    shape = RoundedCornerShape(36.dp),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(
                    "Home" to "🏠",
                    "Diet" to "🥗",
                    "Meds" to "💊",
                    "Reports" to "📊",
                    "Settings" to "⚙️",
                ).forEach { (name, icon) ->
                    val isSelected = selectedTab == name

                    if (isSelected) {
                        // Selected Liquid Glass Active Pill
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color(0xFFB81D85).copy(alpha = 0.3f),
                                    spotColor = Color(0xFFB81D85).copy(alpha = 0.4f),
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFD81B60),
                                            Color(0xFF8E24AA),
                                        ),
                                    ),
                                )
                                .border(
                                    width = 1.2.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.8f),
                                            Color.White.copy(alpha = 0.2f),
                                        ),
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                )
                                .clickable {
                                    when (name) {
                                        "Home" -> onHomeTabClick()
                                        "Diet" -> onDietTabClick()
                                        "Meds" -> onMedsTabClick()
                                        "Reports" -> onReportsTabClick()
                                        "Settings" -> onSettingsTabClick()
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    } else {
                        // Unselected Tab Item
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    when (name) {
                                        "Home" -> onHomeTabClick()
                                        "Diet" -> onDietTabClick()
                                        "Meds" -> onMedsTabClick()
                                        "Reports" -> onReportsTabClick()
                                        "Settings" -> onSettingsTabClick()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(text = icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    statusText: String,
    statusBg: Color,
    statusColor: Color,
    cardBg: Color,
    borderColor: Color,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onActionClick() }
            .padding(14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = icon, fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6E6877),
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF8A8595),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "$actionText →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                )
            }
        }
    }
}

@Composable
fun RoutineCheckItem(
    icon: String,
    label: String,
    isDone: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isDone) Color(0xFFE8F5E9) else Color(0xFFF8F7FA))
                .border(
                    width = 1.5.dp,
                    color = if (isDone) Color(0xFF2E7D32) else Color(0xFFEFECEF),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = if (isDone) "✓" else icon, fontSize = 16.sp, color = if (isDone) Color(0xFF2E7D32) else Color(0xFF1E1E22))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
            color = if (isDone) Color(0xFF2E7D32) else Color(0xFF6E6877),
        )
    }
}

@Composable
fun VibrantQuickActionButton(
    title: String,
    icon: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(colors = gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Diawell123Theme {
        HomeScreen()
    }
}
