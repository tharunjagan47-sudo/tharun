package com.simats.diawell123.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
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
fun PatientProfileScreen(
    modifier: Modifier = Modifier,
    onContinueClick: () -> Unit = {},
) {
    var age by remember { mutableStateOf("30 years") }
    var diabetesType by remember { mutableStateOf("Type 2") }
    var height by remember { mutableStateOf("165 cm") }
    var weight by remember { mutableStateOf("68 kg") }
    var foodPreferences by remember { mutableStateOf("Non-vegetarian") }
    var foodAllergies by remember { mutableStateOf("None reported") }
    var activityLevel by remember { mutableStateOf("Moderately active") }

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
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Make it personal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Text(
                text = "A few details to shape your daily plan.",
                fontSize = 13.sp,
                color = Color(0xFF6E6877),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Progress
            Text(
                text = "PATIENT PROFILE • STEP 1 OF 2",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
                letterSpacing = 0.5.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { 0.5f },
                color = Color(0xFFB81D85),
                trackColor = Color(0xFFF3E2F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Demo Profile Tag
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

            // Age & Diabetes Type Row
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileInputField(
                    label = "Age",
                    value = age,
                    onValueChange = { age = it },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                ProfileInputField(
                    label = "Diabetes type",
                    value = diabetesType,
                    onValueChange = { diabetesType = it },
                    trailingText = "▼",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Height & Weight Row
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileInputField(
                    label = "Height",
                    value = height,
                    onValueChange = { height = it },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                ProfileInputField(
                    label = "Weight",
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Food Preferences
            ProfileInputField(
                label = "Food preferences",
                value = foodPreferences,
                onValueChange = { foodPreferences = it },
                trailingText = "▼",
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Food Allergies
            ProfileInputField(
                label = "Food allergies",
                value = foodAllergies,
                onValueChange = { foodAllergies = it },
                trailingText = "▼",
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Activity Level
            ProfileInputField(
                label = "Activity level",
                value = activityLevel,
                onValueChange = { activityLevel = it },
                trailingText = "▼",
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Walking or light exercise 3-5 days a week.\nYou can update these details anytime.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
                lineHeight = 15.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button
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
                    .clickable { onContinueClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "→  Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Our suggestions support your outline, not a diagnosis.\nFollow your healthcare professional's advice.",
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
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
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
            trailingIcon = trailingText?.let { text ->
                {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        color = Color(0xFF8A8595),
                        modifier = Modifier.padding(end = 12.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PatientProfileScreenPreview() {
    Diawell123Theme {
        PatientProfileScreen()
    }
}
