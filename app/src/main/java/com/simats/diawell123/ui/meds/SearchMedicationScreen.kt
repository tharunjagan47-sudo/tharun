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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.CatalogMedicationItem
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SearchMedicationScreen(
    modifier: Modifier = Modifier,
    viewModel: MedsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSelectMedication: (String, String) -> Unit = { _, _ -> },
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()

    val fallbackList = remember {
        listOf(
            CatalogMedicationItem(
                name = "Glycomet 500 Tablet",
                dose = "500mg • Tablet",
                primaryIngredient = "Metformin",
                dosageForm = "Tablet",
                category = "Antidiabetic",
                manufacturer = "USV Ltd",
                packaging = "strip of 10 tablets",
                priceInr = "₹18.70",
                timing = "With Meal",
                info = "First-line oral antidiabetic improving insulin sensitivity.",
                icon = "💊"
            ),
            CatalogMedicationItem(
                name = "Amaryl 2mg Tablet",
                dose = "2mg • Tablet",
                primaryIngredient = "Glimepiride",
                dosageForm = "Tablet",
                category = "Antidiabetic",
                manufacturer = "Sanofi India Ltd",
                packaging = "strip of 15 tablets",
                priceInr = "₹182.50",
                timing = "Before Breakfast",
                info = "Sulfonylurea stimulating pancreatic beta cells to secrete insulin.",
                icon = "💊"
            ),
            CatalogMedicationItem(
                name = "Januvia 100mg Tablet",
                dose = "100mg • Tablet",
                primaryIngredient = "Sitagliptin",
                dosageForm = "Tablet",
                category = "Antidiabetic",
                manufacturer = "MSD Pharmaceuticals Pvt Ltd",
                packaging = "strip of 14 tablets",
                priceInr = "₹385.00",
                timing = "Once Daily",
                info = "DPP-4 inhibitor enhancing natural incretin hormones.",
                icon = "💊"
            ),
            CatalogMedicationItem(
                name = "Jardiance 10mg Tablet",
                dose = "10mg • Tablet",
                primaryIngredient = "Empagliflozin",
                dosageForm = "Tablet",
                category = "Antidiabetic",
                manufacturer = "Boehringer Ingelheim",
                packaging = "strip of 10 tablets",
                priceInr = "₹540.00",
                timing = "In the Morning",
                info = "SGLT2 inhibitor removing excess glucose via urine.",
                icon = "💊"
            ),
            CatalogMedicationItem(
                name = "Lantus 100IU/ml Cartridge",
                dose = "100IU/ml • Injection",
                primaryIngredient = "Insulin Glargine",
                dosageForm = "Injection",
                category = "Antidiabetic",
                manufacturer = "Sanofi India Ltd",
                packaging = "packet of 5 cartridges",
                priceInr = "₹1,450.00",
                timing = "At Bedtime",
                info = "Long-acting recombinant human basal insulin.",
                icon = "💉"
            ),
            CatalogMedicationItem(
                name = "Atorva 10 Tablet",
                dose = "10mg • Tablet",
                primaryIngredient = "Atorvastatin",
                dosageForm = "Tablet",
                category = "Cardiovascular",
                manufacturer = "Zydus Cadila",
                packaging = "strip of 15 tablets",
                priceInr = "₹95.00",
                timing = "At Bedtime",
                info = "Lipid-lowering agent for diabetic cardiovascular health.",
                icon = "💊"
            )
        )
    }

    val displayList: List<CatalogMedicationItem> = if (searchState.results.isNotEmpty()) {
        searchState.results
    } else {
        if (searchQuery.isBlank()) {
            fallbackList
        } else {
            fallbackList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.primaryIngredient.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
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
                        text = "Search Medicine Catalog",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Over 250,000+ Indian medicines & diabetic therapies.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchMedications(it)
                },
                placeholder = {
                    Text(
                        text = "Search Glycomet, Amaryl, Januvia, Insulin...",
                        fontSize = 13.sp,
                        color = Color(0xFFA09BAA),
                    )
                },
                singleLine = true,
                trailingIcon = {
                    if (searchState.isSearching) {
                        CircularProgressIndicator(
                            color = Color(0xFFB81D85),
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                    } else {
                        Text(text = "🔍", fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Medicine Option if user typed something not matching
            if (searchQuery.isNotBlank() && displayList.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFB81D85), RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.selectMedicineForReminder(searchQuery.trim(), "1 Tablet / Dose")
                            onSelectMedication(searchQuery.trim(), "1 Tablet / Dose")
                        }
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "+ Add '${searchQuery.trim()}'",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85)
                            )
                            Text(
                                text = "Use this custom medication name",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877)
                            )
                        }
                        Text(
                            text = "Select ›",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = if (searchQuery.isBlank()) "Popular Medicines & Therapies" else "Search Results (${displayList.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Medications List
            displayList.forEach { tablet ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.selectMedicineFromDataset(tablet)
                            onSelectMedication(tablet.name, tablet.dose)
                        }
                        .padding(14.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = tablet.icon.ifBlank { "💊" },
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFB81D85),
                                    )
                                    if (tablet.primaryIngredient.isNotBlank()) {
                                        Text(
                                            text = "Active: ${tablet.primaryIngredient}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6E6877),
                                        )
                                    }
                                }
                            }

                            // Category badge
                            if (tablet.category.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (tablet.category.contains("diabet", ignoreCase = true))
                                                Color(0xFFE8F5E9)
                                            else
                                                Color(0xFFFDEEF7)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = tablet.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tablet.category.contains("diabet", ignoreCase = true))
                                            Color(0xFF2E7D32)
                                        else
                                            Color(0xFFB81D85),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Manufacturer, Packaging & Recommended Timing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (tablet.manufacturer.isNotBlank() || tablet.packaging.isNotBlank()) {
                                    val subText = listOf(tablet.manufacturer, tablet.packaging)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")
                                    Text(
                                        text = subText,
                                        fontSize = 10.sp,
                                        color = Color(0xFF8A8595),
                                    )
                                }
                                if (tablet.priceInr.isNotBlank()) {
                                    Text(
                                        text = "MRP: ${tablet.priceInr}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                            }

                            Text(
                                text = "+ Set Reminder ›",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }
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
