package com.simats.diawell123.ui.profile

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun HealthPreferencesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSavePreferencesClick: () -> Unit = {},
) {
    var selectedDiet by remember { mutableStateOf("Non-Vegetarian") }
    var allergies by remember { mutableStateOf("None reported") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var activityLevel by remember { mutableStateOf("Moderately active") }
    var selectedGoal by remember { mutableStateOf("Balanced meals") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
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
                    color = Color(0xFF1E1E22),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(4.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Health preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Meal ideas that fit your everyday routine.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Demo profile • Maya Johnson",
                    fontSize = 12.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Food Preferences Segmented Cards
            Text(
                text = "Food preferences",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DietTypeCard(
                    title = "Vegetarian",
                    iconText = "🥦",
                    isSelected = selectedDiet == "Vegetarian",
                    onClick = { selectedDiet = "Vegetarian" },
                    modifier = Modifier.weight(1f),
                )
                DietTypeCard(
                    title = "Non-Vegetarian",
                    iconText = "🍗",
                    isSelected = selectedDiet == "Non-Vegetarian",
                    onClick = { selectedDiet = "Non-Vegetarian" },
                    modifier = Modifier.weight(1f),
                )
                DietTypeCard(
                    title = "Vegan",
                    iconText = "🌱",
                    isSelected = selectedDiet == "Vegan",
                    onClick = { selectedDiet = "Vegan" },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Food Allergies Input
            Text(
                text = "Food allergies",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = "▼",
                        fontSize = 10.sp,
                        color = Color(0xFF8A8595),
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                    focusedTextColor = Color(0xFF1E1E22),
                    unfocusedTextColor = Color(0xFF1E1E22),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preferred Meal Types
            Text(
                text = "Preferred meal types",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Breakfast", "Lunch", "Snacks", "Dinner").forEach { type ->
                    val isSelected = selectedMealType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedMealType = type }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = type,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Activity Level Dropdown
            Text(
                text = "Activity level",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = activityLevel,
                onValueChange = { activityLevel = it },
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = "▼",
                        fontSize = 10.sp,
                        color = Color(0xFF8A8595),
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                    focusedTextColor = Color(0xFF1E1E22),
                    unfocusedTextColor = Color(0xFF1E1E22),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Walking or light exercise 3-5 days a week.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dietary Goals (optional)
            Text(
                text = "Dietary goals (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Balanced meals", "More whole foods").forEach { goal ->
                    val isSelected = selectedGoal == goal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedGoal = goal }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = goal,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Banner Callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(12.dp),
            ) {
                Text(
                    text = "Your preferences inform personalized meal suggestions. You can update them anytime.",
                    fontSize = 12.sp,
                    color = Color(0xFF811978),
                    lineHeight = 17.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Preferences Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7A1C7D),
                                Color(0xFFBD1E85),
                                Color(0xFFFF5722),
                            ),
                        ),
                    )
                    .clickable { onSavePreferencesClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Save Preferences",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "General meal ideas, not a medical diet prescription.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
fun DietTypeCard(
    title: String,
    iconText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = iconText, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFF232226),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HealthPreferencesScreenPreview() {
    Diawell123Theme {
        HealthPreferencesScreen()
    }
}
