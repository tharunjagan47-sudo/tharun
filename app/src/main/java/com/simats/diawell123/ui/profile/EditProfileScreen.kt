package com.simats.diawell123.ui.profile

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveChangesClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf(ProfileState.userName) }
    var email by remember { mutableStateOf(ProfileState.userEmail) }
    var age by remember { mutableStateOf("30 years") }
    var diabetesType by remember { mutableStateOf("Type 2") }
    var height by remember { mutableStateOf("165 cm") }
    var weight by remember { mutableStateOf("68 kg") }

    var showSuccessBanner by remember { mutableStateOf(false) }

    // Sync from profile data when loaded from backend
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { p ->
            fullName = p.fullName
            email = p.email
            p.diabetesType?.let { diabetesType = it }
            p.age?.let { age = "$it years" }
            p.heightCm?.let { height = "${it.toInt()} cm" }
            p.weightKg?.let { weight = "${it.toInt()} kg" }
        }
    }

    val ageRange = remember { (18..80).map { "$it years" } }
    val heightRange = remember { (140..210).filter { it % 5 == 0 }.map { "$it cm" } }
    val weightRange = remember { (40..140).filter { it % 5 == 0 || it == 68 }.map { "$it kg" } }

    // Android Gallery Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                ProfileState.profileImageBitmap = bitmap.asImageBitmap()

                // Upload to server
                val tempFile = uriToFile(context, uri)
                if (tempFile != null) {
                    viewModel.uploadProfilePhoto(tempFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            ProfileState.userName = fullName
            ProfileState.userEmail = email
            delay(1500)
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
                StyledBackButton(onBackClick = onBackClick)

                Spacer(modifier = Modifier.width(14.dp))

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

            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to update profile.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // Profile Avatar & Gallery Camera Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                ) {
                    ProfileAvatarView(sizeDp = 72.dp, fontSizeSp = 24)

                    // Gallery Picker Camera Badge
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB81D85))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isUploadingPhoto) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(text = "📷", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📷 Choose Profile Photo from Gallery",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                    modifier = Modifier.clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Text(
                    text = "User profile • DiaWell care",
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
                text = if (uiState.isSaving) "Saving Changes..." else "Save Changes",
                onClick = {
                    val ageNum = age.replace(Regex("[^0-9]"), "").toIntOrNull()
                    val heightNum = height.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val weightNum = weight.replace(Regex("[^0-9.]"), "").toFloatOrNull()

                    viewModel.updateProfile(
                        fullName = fullName,
                        email = email,
                        diabetesType = diabetesType,
                        age = ageNum,
                        heightCm = heightNum,
                        weightKg = weightNum,
                        onSuccess = {
                            showSuccessBanner = true
                        }
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sharing details assists your personalized accounting. Review this profile before saving. Logs are for tracking, not medical advice.",
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

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("profile_upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        tempFile
    } catch (e: Exception) {
        null
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )
            Text(
                text = selectedItem,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color.Transparent)
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                        )
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
