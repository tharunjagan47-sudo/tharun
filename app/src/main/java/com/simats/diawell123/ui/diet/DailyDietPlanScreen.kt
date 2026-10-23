package com.simats.diawell123.ui.diet

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun DailyDietPlanScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMealDetailsClick: (String) -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Diet") }
    var selectedDateText by remember { mutableStateOf("Thu, 15 Oct 2026") }
    var showDatePickerDialog by remember { mutableStateOf(value = false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

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
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar & Unified Search Bar (Pops Up)
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Diet & Nutrition",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "Balanced Indian meals & food search in one place.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Unified Explore Food Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "🔍 Search foods, recipes or ingredients...",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA09BAA),
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFFBD1E85),
                                unfocusedBorderColor = Color(0xFFEFECEF),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Filter Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            listOf("All", "Breakfast", "Lunch", "Snack", "Dinner").forEach { category ->
                                val isSelected = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFFB81D85) else Color(0xFFF8F7FA))
                                        .clickable { selectedCategory = category }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF6E6877),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Date Selector Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                                .clickable { showDatePickerDialog = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "‹", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                                Text(
                                    text = "📅  $selectedDateText",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(text = "›", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Breakfast Meal Card (Pops Up)
                if (selectedCategory == "All" || selectedCategory == "Breakfast") {
                    ElementPopUpContainer(delayMillis = 60) {
                        DietPlanMealCard(
                            category = "BREAKFAST",
                            name = "Idli Sambar & Coconut Chutney",
                            serving = "2 idlis + 1 bowl sambar • 280 g",
                            macros = "280 kcal • C:44g • P:10g • F:5g",
                            icon = "🥣",
                            onClick = { onMealDetailsClick("breakfast") },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Lunch Meal Card (Pops Up)
                if (selectedCategory == "All" || selectedCategory == "Lunch") {
                    ElementPopUpContainer(delayMillis = 120) {
                        DietPlanMealCard(
                            category = "LUNCH",
                            name = "Multigrain Roti, Dal & Bhindi Sabzi",
                            serving = "2 rotis + 1 bowl dal + sabzi • 380 g",
                            macros = "420 kcal • C:52g • P:18g • F:10g",
                            icon = "🫓",
                            onClick = { onMealDetailsClick("lunch") },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Snack Meal Card (Pops Up)
                if (selectedCategory == "All" || selectedCategory == "Snack") {
                    ElementPopUpContainer(delayMillis = 180) {
                        DietPlanMealCard(
                            category = "SNACK",
                            name = "Sprouts Chaat & Green Tea",
                            serving = "1 bowl sprouts • 180 g",
                            macros = "150 kcal • C:22g • P:11g • F:2g",
                            icon = "🥗",
                            onClick = { onMealDetailsClick("snack") },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Dinner Meal Card (Pops Up)
                if (selectedCategory == "All" || selectedCategory == "Dinner") {
                    ElementPopUpContainer(delayMillis = 240) {
                        DietPlanMealCard(
                            category = "DINNER",
                            name = "Palak Paneer & Brown Rice",
                            serving = "1 plate • 350 g",
                            macros = "380 kcal • C:38g • P:22g • F:14g",
                            icon = "🍲",
                            onClick = { onMealDetailsClick("dinner") },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Authentic Indian meal ideas, low glycemic index.\nFollow with your doctor's or nutritionist's advice.",
                    fontSize = 11.sp,
                    color = Color(0xFF8A8595),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )
            }
        }

        // Calendar Date Picker Modal Dialog
        if (showDatePickerDialog) {
            CalendarDatePickerDialog(
                selectedDateText = selectedDateText,
                onDateSelected = { newDate ->
                    selectedDateText = "Thu, $newDate"
                },
                onDismissRequest = { showDatePickerDialog = false },
            )
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = onHomeTabClick,
                onDietTabClick = { selectedTab = "Diet" },
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun CalendarDatePickerDialog(
    selectedDateText: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedDay by remember { mutableIntStateOf(15) }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFEFECEF), RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📅 Select Diet Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8A8595),
                        modifier = Modifier
                            .clickable { onDismissRequest() }
                            .padding(4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Navigator Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "‹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                    Text(
                        text = "October 2026",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                    Text(text = "›", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of Week Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A8595),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(32.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid (1 to 31)
                val daysList = (1..31).toList()
                val rows = daysList.chunked(7)

                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        week.forEach { dayNumber ->
                            val isSelected = dayNumber == selectedDay
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFB81D85) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = if (isSelected) Color.Transparent else Color(0xFFEFECEF),
                                        shape = CircleShape,
                                    )
                                    .clickable { selectedDay = dayNumber }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E1E22),
                                )
                            }
                        }

                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F7FA))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6E6877),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                ),
                            )
                            .clickable {
                                onDateSelected("$selectedDay Oct 2026")
                                onDismissRequest()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Select Date",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DietPlanMealCard(
    category: String,
    name: String,
    serving: String,
    macros: String,
    icon: String,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = serving,
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = macros,
                        fontSize = 11.sp,
                        color = Color(0xFF8A8595),
                    )
                }
            }

            Text(
                text = "View Details →",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DailyDietPlanScreenPreview() {
    Diawell123Theme {
        DailyDietPlanScreen()
    }
}
