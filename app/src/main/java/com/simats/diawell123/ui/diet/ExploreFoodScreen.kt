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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun ExploreFoodScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMealDetailsClick: () -> Unit = {},
) {
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
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                            text = "Explore food",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "Find everyday meals and your favorites.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "🔍 Search foods or ingredients...",
                            fontSize = 13.sp,
                            color = Color(0xFFA09BAA),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F7FA),
                        unfocusedContainerColor = Color(0xFFF8F7FA),
                        focusedBorderColor = Color(0xFFBD1E85),
                        unfocusedBorderColor = Color(0xFFEFECEF),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    listOf("All", "Breakfast", "Bowls", "Snacks").forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFB81D85) else Color(0xFFF8F7FA))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Preference Tag
                Text(
                    text = "🍗 Non-vegetarian • your preference",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Explore Meals Header
                Text(
                    text = "Explore meals",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExploreMealCard(
                    icon = "🥣",
                    category = "BREAKFAST",
                    name = "Berry & almond oats",
                    serving = "1 bowl • 290 g",
                    macros = "270 kcal • C:42g • P:9g • F:8g",
                    onClick = onMealDetailsClick,
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExploreMealCard(
                    icon = "🥗",
                    category = "LUNCH",
                    name = "Grilled chicken bowl",
                    serving = "1 bowl • 350 g",
                    macros = "450 kcal • C:48g • P:38g • F:12g",
                    onClick = onMealDetailsClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AI Suggested Callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(12.dp),
                ) {
                    Column {
                        Text(
                            text = "✨ AI Suggested",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF811978),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "For your non-vegetarian preference, try the grilled chicken bowl with vegetables and whole grains.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                            lineHeight = 16.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Alternative Callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(12.dp),
                ) {
                    Column {
                        Text(
                            text = "💡 AI Alternative",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF811978),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Greek yogurt & berries.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ExploreMealCard(
    icon: String,
    category: String,
    name: String,
    serving: String,
    macros: String,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = serving,
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                    )
                    Text(
                        text = macros,
                        fontSize = 10.sp,
                        color = Color(0xFF8A8595),
                    )
                }
            }

            Text(
                text = "+ Add",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreFoodScreenPreview() {
    Diawell123Theme {
        ExploreFoodScreen()
    }
}
