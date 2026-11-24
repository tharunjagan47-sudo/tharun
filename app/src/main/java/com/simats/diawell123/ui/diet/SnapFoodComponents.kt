package com.simats.diawell123.ui.diet

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.diawell123.data.model.resolveFoodEmoji
import com.simats.diawell123.ui.components.DiaWellAmbientBackground
import com.simats.diawell123.data.model.HealthFoodItem
import com.simats.diawell123.data.model.SnapFoodAnalysis
import com.simats.diawell123.data.model.SnapFoodSubItem
import com.simats.diawell123.data.repository.SnapFoodAnalyzer
import com.simats.diawell123.ui.components.FluidGlassCircleButton
import com.simats.diawell123.ui.components.aiRotatingBorder
import com.simats.diawell123.ui.theme.AppTheme

/**
 * Fullscreen camera snap experience with clean framing viewfinder overlay,
 * shutter button, gallery picker, flash toggle, and quick test sample chips.
 */
@Composable
fun SnapFoodCameraOverlay(
    onDismiss: () -> Unit,
    onPhotoCaptured: (Bitmap?, Uri?, String?) -> Unit
) {
    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Silent model pre-warm on screen open so qwen2.5vl:3b is warm before shutter/picker tap
    LaunchedEffect(Unit) {
        SnapFoodAnalyzer.preWarmModel()
    }

    // System camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isAnalyzing = true
            onPhotoCaptured(bitmap, null, null)
        }
    }

    // Gallery image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isAnalyzing = true
            onPhotoCaptured(null, uri, null)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090611))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button - Fluid Glass Bubble
                    FluidGlassCircleButton(
                        imageVector = Icons.Rounded.Close,
                        onClick = onDismiss,
                        contentDescription = "Close",
                        sizeDp = 44.dp,
                        iconSizeDp = 20.dp,
                        tint = Color.White,
                    )

                    // Header title & tips
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CenterFocusStrong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Snap Food",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "AI Nutrition & Macro Scanner",
                            color = Color(0xFFC084FC),
                            fontSize = 11.5.sp
                        )
                    }

                    // Flash Toggle Button - Fluid Glass Bubble
                    FluidGlassCircleButton(
                        imageVector = Icons.Rounded.FlashOn,
                        onClick = {
                            isFlashOn = !isFlashOn
                            Toast.makeText(context, if (isFlashOn) "Flash Enabled" else "Flash Disabled", Toast.LENGTH_SHORT).show()
                        },
                        contentDescription = "Flash",
                        sizeDp = 44.dp,
                        iconSizeDp = 20.dp,
                        tint = if (isFlashOn) Color(0xFFF0ABFC) else Color.White,
                    )
                }

                // Center Viewfinder Framing Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x1AFFFFFF))
                            .border(2.dp, Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF00E5FF))), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzing) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFA855F7), strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Analyzing Food & Macros...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Align meal within frame",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tap Shutter or Pick from Gallery",
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Bottom Control Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x99000000))
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Sample Meal Chips
                    Text(
                        text = "Or tap a sample to analyze instantly:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Poori Masala & Chole" to "Poori with Aloo Masala and Chole",
                            "Ragi Idli Sambar" to "Ragi Idli with Sambar & Keerai Poriyal",
                            "Brown Rice Lunch" to "Brown Rice with Drumstick Sambar, Keerai Poriyal & Curd",
                            "Moong Sundal" to "Boiled Chickpeas & Moong Sundal with Spiced Neer Mor",
                            "Chapati Kootu" to "Multigrain Chapati with Lauki (Bottle Gourd) Kootu & Dal"
                        ).forEach { (label, query) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF2E1065))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(16.dp))
                                    .clickable {
                                        isAnalyzing = true
                                        onPhotoCaptured(null, null, query)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = label, color = Color(0xFFF3E8FF), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Shutter and Gallery Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Button
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .border(1.2.dp, Color(0xFFA855F7), CircleShape)
                                .clickable {
                                    try {
                                        galleryLauncher.launch("image/*")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to load photo from gallery.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = "Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Shutter Button
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(3.5.dp, Color.White, CircleShape)
                                .padding(5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFF00E5FF)))
                                )
                                .clickable {
                                    try {
                                        cameraLauncher.launch(null)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to capture photo.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }

                        // Close helper spacer
                        Spacer(modifier = Modifier.size(50.dp))
                    }
                }
            }
        }
    }
}

/**
 * Detailed Meal Nutrition Breakdown View with:
 * - Meal selector pill
 * - Serving stepper (- / +)
 * - Total calories readout
 * - 4-column macro cards (Protein, Fat, Carbs, Fiber)
 * - Glycemic & health score banner
 * - Sub-item component breakdown with remove/adjust
 * - Done button that logs directly to today's diet log
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealNutritionBreakdownSheet(
    analysis: SnapFoodAnalysis,
    userGlucose: Float? = null,
    onDismiss: () -> Unit,
    onLogMeal: (
        dishName: String,
        mealType: String,
        servings: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        fiber: Float,
        subItemsSummary: String
    ) -> Unit
) {
    val isDark = AppTheme.colors.isDark
    val context = LocalContext.current

    var selectedMealSlot by remember(analysis) { mutableStateOf(analysis.mealSlot) }
    var servingCount by remember(analysis) { mutableIntStateOf(1) }
    var dishTitle by remember(analysis) { mutableStateOf(analysis.detectedDishName) }
    var dishIcon by remember(analysis) { mutableStateOf(analysis.icon) }
    var activeGlycemicScore by remember(analysis) { mutableFloatStateOf(analysis.glycemicScore) }
    var activeGlycemicExplanation by remember(analysis) { mutableStateOf(analysis.glycemicHealthExplanation) }
    var baseCalories by remember(analysis) { mutableIntStateOf(analysis.baseCalories) }
    var baseCarbs by remember(analysis) { mutableFloatStateOf(analysis.baseCarbs) }
    var baseProtein by remember(analysis) { mutableFloatStateOf(analysis.baseProtein) }
    var baseFat by remember(analysis) { mutableFloatStateOf(analysis.baseFat) }
    var baseFiber by remember(analysis) { mutableFloatStateOf(analysis.baseFiber) }

    // Mutable list of sub-items
    val subItems = remember(analysis) { mutableStateListOf<SnapFoodSubItem>().apply { addAll(analysis.subItems) } }

    // Dynamic calculations scaled by active sub-items and servingCount
    val totalCalories = remember(subItems.size, servingCount, baseCalories) {
        val base = if (subItems.isNotEmpty()) subItems.sumOf { it.calories } else baseCalories
        base * servingCount
    }
    val totalCarbs = remember(subItems.size, servingCount, baseCarbs) {
        val base = if (subItems.isNotEmpty()) subItems.map { it.carbsG }.sum() else baseCarbs
        base * servingCount
    }
    val totalProtein = remember(subItems.size, servingCount, baseProtein) {
        val base = if (subItems.isNotEmpty()) subItems.map { it.proteinG }.sum() else baseProtein
        base * servingCount
    }
    val totalFat = remember(subItems.size, servingCount, baseFat) {
        val base = if (subItems.isNotEmpty()) subItems.map { it.fatG }.sum() else baseFat
        base * servingCount
    }
    val totalFiber = remember(subItems.size, servingCount, baseFiber) {
        val base = if (subItems.isNotEmpty()) subItems.map { it.fiberG }.sum() else baseFiber
        base * servingCount
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF090611) else Color(0xFFF8FAFC))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            DiaWellAmbientBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FluidGlassCircleButton(
                        imageVector = Icons.Rounded.Close,
                        onClick = onDismiss,
                        contentDescription = "Close",
                        sizeDp = 40.dp,
                        iconSizeDp = 18.dp,
                    )

                    Text(
                        text = "Meal Nutrition Breakdown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )

                    // Photo preview icon / emoji
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val foodEmoji = resolveFoodEmoji(dishIcon, dishTitle)
                        if (foodEmoji.isNotBlank() && dishTitle.isNotBlank()) {
                            Text(text = foodEmoji, fontSize = 19.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Captured Image Preview
                analysis.imageBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Captured Food",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 1. Meal Type Selector Pills
                Text(
                    text = "Meal Type",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val slots = listOf(
                        "Breakfast" to Icons.Rounded.WbTwilight,
                        "Lunch" to Icons.Rounded.WbSunny,
                        "Snack" to Icons.Rounded.Coffee,
                        "Dinner" to Icons.Rounded.DarkMode
                    )
                    slots.forEach { (slot, vectorIcon) ->
                        val isSelected = selectedMealSlot.equals(slot, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(Color(0xFF7E22CE), Color(0xFFA855F7)))
                                    } else {
                                        Brush.horizontalGradient(
                                            if (isDark) listOf(Color(0xFF1B1430), Color(0xFF1B1430))
                                            else listOf(Color(0xFFF5F3FF), Color(0xFFF5F3FF))
                                        )
                                    }
                                )
                                .clickable { selectedMealSlot = slot }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = vectorIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = slot,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else AppTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Detected Dish Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detected Dish",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA855F7)
                        )
                        Text(
                            text = if (dishTitle.isNotBlank()) dishTitle else "Analyzing Food...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppTheme.colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Serving Quantity Stepper (- / +)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF1A132C) else Color(0xFFF8FAFC))
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Serving Quantity",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Scales total calories",
                            fontSize = 10.5.sp,
                            color = AppTheme.colors.textMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minus Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2E1C55) else Color(0xFFE2E8F0))
                                .clickable {
                                    if (servingCount > 1) servingCount--
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }

                        Text(
                            text = "$servingCount ${if (servingCount == 1) "serving" else "servings"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA855F7)
                        )

                        // Plus Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA855F7))
                                .clickable {
                                    if (servingCount < 6) servingCount++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Total Calories Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDark) listOf(Color(0xFF2E1452), Color(0xFF1B0F33))
                                else listOf(Color(0xFF7E22CE), Color(0xFFA855F7))
                            )
                        )
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL ENERGY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE9D5FF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalCalories kcal",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scaled for $servingCount ${if (servingCount == 1) "serving" else "servings"}",
                            fontSize = 11.sp,
                            color = Color(0xFFE9D5FF).copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))



                // 6. Glycemic & Health Score Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when {
                                activeGlycemicScore >= 8.5f -> Color(0xFF059669).copy(alpha = 0.12f)
                                activeGlycemicScore >= 7.0f -> Color(0xFFD97706).copy(alpha = 0.12f)
                                else -> Color(0xFFDC2626).copy(alpha = 0.12f)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                activeGlycemicScore >= 8.5f -> Color(0xFF10B981).copy(alpha = 0.4f)
                                activeGlycemicScore >= 7.0f -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WorkspacePremium,
                                    contentDescription = null,
                                    tint = when {
                                        activeGlycemicScore >= 8.5f -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
                                        activeGlycemicScore >= 7.0f -> if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                        else -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Glycemic & Health Score",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        activeGlycemicScore >= 8.5f -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
                                        activeGlycemicScore >= 7.0f -> if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                        else -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                                    }
                                )
                            }

                            Text(
                                text = "$activeGlycemicScore / 10",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    activeGlycemicScore >= 8.5f -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
                                    activeGlycemicScore >= 7.0f -> if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                    else -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activeGlycemicExplanation,
                            fontSize = 11.5.sp,
                            color = AppTheme.colors.textPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. Component / Sub-Item Breakdown List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Component Breakdown (${subItems.size} items)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )

                    Text(
                        text = "Adjust components",
                        fontSize = 10.5.sp,
                        color = AppTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (subItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No sub-components remaining.", fontSize = 12.sp, color = AppTheme.colors.textMuted)
                    }
                } else {
                    subItems.forEachIndexed { index, subItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF140F24) else Color(0xFFF8FAFC))
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subItem.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = subItem.portion,
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${subItem.calories * servingCount} kcal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Remove Item Button
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .clickable {
                                            subItems.removeAt(index)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 8. Primary Done Button: Logs directly to today's diet log
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFF00E5FF))
                            )
                        )
                        .clickable {
                            if (dishTitle.isBlank() || totalCalories <= 0) {
                                Toast.makeText(context, "Please wait for nutrition analysis to complete", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val subSummary = subItems.joinToString(", ") { "${it.name} (${it.calories * servingCount} kcal)" }
                            onLogMeal(
                                dishTitle,
                                selectedMealSlot,
                                servingCount,
                                totalCalories,
                                totalCarbs,
                                totalProtein,
                                totalFat,
                                totalFiber,
                                subSummary
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log to Today's Diet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

/**
 * Animated analyzing overlay shown while Moondream Vision AI processes the captured meal photo.
 */
@Composable
fun SnapFoodAnalyzingDialog() {
    val isDark = AppTheme.colors.isDark
    Dialog(
        onDismissRequest = { /* Modal during analysis */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(22.dp))
                .background(if (isDark) Color(0xFF1B1330) else Color.White)
                .aiRotatingBorder(strokeWidth = 2.dp, shape = RoundedCornerShape(22.dp), durationMillis = 2600)
                .padding(26.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(76.dp),
                        color = Color(0xFFA855F7),
                        strokeWidth = 4.dp
                    )
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Analyzing meal...",
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Estimating ingredients, calories, and diabetic nutrition...",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
