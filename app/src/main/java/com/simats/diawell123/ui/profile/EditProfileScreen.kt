package com.simats.diawell123.ui.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveChangesClick: () -> Unit = {},
) {
    var fullName by remember { mutableStateOf("Maya Johnson") }
    var email by remember { mutableStateOf("maya.johnson@example.com") }
    var age by remember { mutableStateOf("30 years") }
    var diabetesType by remember { mutableStateOf("Type 2") }
    var height by remember { mutableStateOf("165 cm") }
    var weight by remember { mutableStateOf("68 kg") }

    var showSuccessBanner by remember { mutableStateOf(value = false) }

    val ageRange = remember { (18..80).map { "$it years" } }
    val heightRange = remember { (140..210).filter { it % 5 == 0 }.map { "$it cm" } }
    val weightRange = remember { (40..140).filter { it % 5 == 0 || it == 68 }.map { "$it kg" } }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1800)
            showSuccessBanner = false
            onSaveChangesClick()
        }
    }

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
                        text = "Personal Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Successfully Saved Animated Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓", fontSize = 22.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Changes Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your personal profile details have been updated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Profile Avatar & Name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E2F0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "MJ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Text(
                    text = "Demo profile • Sample account",
                    fontSize = 11.sp,
                    color = Color(0xFF811978),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Full Name
            ProfileDetailInputField(label = "Full Name", value = fullName, onValueChange = { fullName = it })

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            ProfileDetailInputField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                trailingIconRes = R.drawable.ic_email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Diabetes Type Vertical Scroll Picker
            VerticalScrollAndSelectPicker(
                label = "Diabetes Type",
                items = listOf("Type 1", "Type 2", "Gestational", "Pre-diabetes", "LADA / Other"),
                selectedItem = diabetesType,
                onItemSelected = { diabetesType = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Age Vertical Scroll Picker
            VerticalScrollAndSelectPicker(
                label = "Age",
                items = ageRange,
                selectedItem = if (age.contains("years")) age else "$age years",
                onItemSelected = { age = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Height Vertical Scroll Picker
            VerticalScrollAndSelectPicker(
                label = "Height",
                items = heightRange,
                selectedItem = if (height.contains("cm")) height else "$height cm",
                onItemSelected = { height = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Weight Vertical Scroll Picker
            VerticalScrollAndSelectPicker(
                label = "Weight",
                items = weightRange,
                selectedItem = if (weight.contains("kg")) weight else "$weight kg",
                onItemSelected = { weight = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scroll vertically and select your details to update your personal profile.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Changes Button
            IosLiquidGlassButton(
                text = "Save Changes",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sharing details assists your personalized accounting. Review this demo profile before saving. Demo logs are not a real diagnosis or medical advice.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
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
fun ProfileDetailInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIconRes: Int? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF232226),
            modifier = Modifier.padding(bottom = 4.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            trailingIcon = {
                if (trailingIconRes != null) {
                    Icon(
                        painter = painterResource(id = trailingIconRes),
                        contentDescription = null,
                        tint = Color(0xFF9A96A4),
                        modifier = Modifier.size(18.dp),
                    )
                }
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
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun VerticalScrollAndSelectPicker(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(value = false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Text(
                text = "Selected: $selectedItem",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8F7FA))
                .border(1.dp, if (isExpanded) Color(0xFFB81D85) else Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = if (isExpanded) "▲ Scroll Selection" else "▼ Tap to Scroll & Select",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB81D85),
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFB81D85), RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp, horizontal = 6.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(items) { item ->
                        val isSelected = item == selectedItem
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFDEEF7) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFB81D85) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .clickable {
                                    onItemSelected(item)
                                    isExpanded = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
                                )

                                if (isSelected) {
                                    Text(
                                        text = "✓ Selected",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB81D85),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
    Diawell123Theme {
        EditProfileScreen()
    }
}
