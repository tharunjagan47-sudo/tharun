package com.simats.diawell123.ui.meds

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

data class AvailableTablet(
    val name: String,
    val dose: String,
    val type: String,
    val icon: String,
)

@Composable
fun SearchMedicationScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSelectMedication: (String, String) -> Unit = { _, _ -> },
) {
    var searchQuery by remember { mutableStateOf("") }

    val allTablets = remember {
        listOf(
            AvailableTablet("Metformin", "500 mg • 1 Tablet", "Diabetes Control", "💊"),
            AvailableTablet("Glimepiride", "2 mg • 1 Tablet", "Blood Glucose Lowering", "💊"),
            AvailableTablet("Januvia (Sitagliptin)", "100 mg • 1 Tablet", "DPP-4 Inhibitor", "💊"),
            AvailableTablet("Jardiance (Empagliflozin)", "10 mg • 1 Tablet", "SGLT2 Inhibitor", "💊"),
            AvailableTablet("Insulin Glargine / Lantus", "10 Units • Subcutaneous", "Basal Insulin", "💉"),
            AvailableTablet("Voglibose", "0.3 mg • 1 Tablet", "Alpha-glucosidase Inhibitor", "💊"),
            AvailableTablet("Multivitamin / B12", "1 Capsule", "Dietary Supplement", "💊"),
            AvailableTablet("Atorvastatin", "10 mg • 1 Tablet", "Cholesterol Management", "💊"),
        )
    }

    val filteredTablets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allTablets
        } else {
            allTablets.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.type.contains(searchQuery, ignoreCase = true)
            }
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
                        text = "Search Available Tablets",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Find your prescribed medication or supplement.",
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
                        text = "Search Metformin, Glimepiride, Insulin...",
                        fontSize = 13.sp,
                        color = Color(0xFFA09BAA),
                    )
                },
                singleLine = true,
                trailingIcon = { Text(text = "🔍", fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Available Medications (${filteredTablets.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Medications List
            filteredTablets.forEach { tablet ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                        .clickable { onSelectMedication(tablet.name, tablet.dose) }
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = tablet.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tablet.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = tablet.dose,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                                Text(
                                    text = tablet.type,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB81D85),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Text(
                            text = "+ Set Reminder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchMedicationScreenPreview() {
    Diawell123Theme {
        SearchMedicationScreen()
    }
}
