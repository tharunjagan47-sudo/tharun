package com.simats.diawell123.ui.profile
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.ui.theme.AppTheme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("30 years") }
    var diabetesType by remember { mutableStateOf("Type 2") }
    var height by remember { mutableStateOf("165 cm") }
    var weight by remember { mutableStateOf("68 kg") }
    var targetFasting by remember { mutableStateOf("100") }
    var targetPostmeal by remember { mutableStateOf("140") }

    var showSuccessBanner by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showViewPictureDialog by remember { mutableStateOf(false) }

    val currentSessionUserId = remember {
        try { SessionManager.getInstance().getUserId() } catch (_: Exception) { null }
    }

    LaunchedEffect(currentSessionUserId) {
        if (uiState.profile == null || (currentSessionUserId != null && uiState.profile?.userId != currentSessionUserId)) {
            viewModel.loadProfile()
        }
    }

    // Sync from profile data when loaded from backend for the active user
    LaunchedEffect(uiState.profile, currentSessionUserId) {
        uiState.profile?.let { p ->
            if (currentSessionUserId == null || p.userId == currentSessionUserId) {
                fullName = p.fullName
                email = p.email
                p.phone?.let { if (it.isNotBlank()) phone = it }
                p.gender?.let { if (it.isNotBlank()) gender = it }
                p.diabetesType?.let { diabetesType = it }
                p.age?.let { age = "$it years" }
                p.heightCm?.let { height = "${it.toInt()} cm" }
                p.weightKg?.let { weight = "${it.toInt()} kg" }
                p.targetFastingGlucose?.let { targetFasting = it.toString() }
                p.targetPostmealGlucose?.let { targetPostmeal = it.toString() }
            }
        }
    }

    val ageRange = remember { (18..80).map { "$it years" } }
    val heightRange = remember { (140..210).filter { it % 5 == 0 }.map { "$it cm" } }
    val weightRange = remember { (40..140).filter { it % 5 == 0 || it == 68 }.map { "$it kg" } }
    val coroutineScope = rememberCoroutineScope()

    val processAndUploadBitmap: (Bitmap) -> Unit = { rawBitmap ->
        coroutineScope.launch {
            try {
                val sessionManager = try {
                    SessionManager.getInstance()
                } catch (_: Exception) {
                    SessionManager.init(context)
                }
                val userId = sessionManager.getUserId() ?: 0

                // 1. Resize to 256x256, compress to JPEG (quality = 75) on Dispatchers.Default, Base64 encode
                val (scaledBitmap, base64) = withContext(Dispatchers.Default) {
                    val scaled = Bitmap.createScaledBitmap(rawBitmap, 256, 256, true)
                    val stream = java.io.ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                    val bytes = stream.toByteArray()
                    val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    Pair(scaled, b64)
                }

                // 2. Update UI state and save in local cache
                ProfileState.saveProfileBitmap(context, scaledBitmap, userId)
                if (userId > 0) {
                    sessionManager.saveUserPfp(userId, base64, sessionManager.getUserSession()?.email)
                    // 3. Send to update_profile_pic.php with logged-in user_id
                    viewModel.uploadProfilePic(userId, base64) {
                        Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Profile picture saved locally.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error updating photo: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val hasProfilePhoto = ProfileState.profileImageBitmap != null ||
        !uiState.profile?.profilePic.isNullOrBlank() ||
        !uiState.profile?.profilePhotoUrl.isNullOrBlank()

    val removeProfilePhoto: () -> Unit = {
        val sessionManager = try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            SessionManager.init(context)
        }
        val userId = sessionManager.getUserId() ?: 0
        ProfileState.isUploadingAvatar = true
        ProfileState.clearProfilePhoto(context, userId, keepUploadLock = true)
        viewModel.removeProfilePhoto(userId) { removedRemotely, message ->
            Toast.makeText(
                context,
                if (removedRemotely) "Profile photo removed." else message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val handlePickedUri: (Uri?) -> Unit = { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                processAndUploadBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error loading image: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Android Gallery Photo Picker Launcher (Modern Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = handlePickedUri
    )

    // Legacy File/Gallery Picker Launcher Fallback
    val legacyGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = handlePickedUri
    )

    val safeLaunchGallery: () -> Unit = {
        showPhotoOptionsDialog = false
        showViewPictureDialog = false
        try {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (_: Exception) {
            try {
                legacyGalleryLauncher.launch("image/*")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Unable to open photo picker", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Android Camera Photo Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processAndUploadBitmap(bitmap)
        }
    }

    // Camera Runtime Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Unable to launch camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    val safeLaunchCamera: () -> Unit = {
        showPhotoOptionsDialog = false
        showViewPictureDialog = false
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Unable to launch camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    if (showPhotoOptionsDialog) {
        ProfilePhotoOptionsDialog(
            hasProfilePhoto = hasProfilePhoto,
            onDismissRequest = { showPhotoOptionsDialog = false },
            onViewPictureClick = {
                showPhotoOptionsDialog = false
                showViewPictureDialog = true
            },
            onChooseGalleryClick = safeLaunchGallery,
            onTakePhotoClick = safeLaunchCamera,
            onRemovePhotoClick = removeProfilePhoto
        )
    }

    if (showViewPictureDialog) {
        ViewProfilePictureDialog(
            userName = fullName,
            hasProfilePhoto = hasProfilePhoto,
            onDismissRequest = { showViewPictureDialog = false },
            onChooseGalleryClick = safeLaunchGallery,
            onTakePhotoClick = safeLaunchCamera,
            onRemovePhotoClick = removeProfilePhoto
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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

                Text(
                    text = "Edit Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
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
                        .background(AppTheme.colors.successBg)
                        .border(1.5.dp, AppTheme.colors.successBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AppTheme.colors.successText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Changes Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.successText,
                            )
                            Text(
                                text = "Your personal profile details have been updated.",
                                fontSize = 11.sp,
                                color = AppTheme.colors.successText,
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
                        .background(AppTheme.colors.errorBg)
                        .border(1.dp, AppTheme.colors.errorBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to update profile.",
                        fontSize = 12.sp,
                        color = AppTheme.colors.errorText
                    )
                }
            }

            // Profile Avatar & Photo Action Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable {
                        showPhotoOptionsDialog = true
                    },
                ) {
                    ProfileAvatarView(sizeDp = 80.dp, fontSizeSp = 28)

                    // Camera / Options Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7))
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
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = "Edit Photo",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Profile Photo Action Chips (View, Gallery, Camera)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View Photo Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { showViewPictureDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Visibility,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "View Photo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
                    }

                    // Gallery Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { safeLaunchGallery() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Gallery",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
                    }

                    // Camera Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFA855F7))
                            .clickable { safeLaunchCamera() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Take Photo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (hasProfilePhoto) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RemoveProfilePhotoChip(
                        fillWidth = false,
                        onClick = removeProfilePhoto
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )

                Text(
                    text = "User profile • DiaWell care",
                    fontSize = 11.sp,
                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number
            ProfileDetailInputField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Segmented Picker
            VerticalScrollAndSelectPicker(
                label = "Gender",
                items = listOf("Male", "Female", "Other"),
                selectedItem = gender,
                onItemSelected = { gender = it },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Clinical Target Glucose Range
            Text(
                text = "Clinical Target Glucose Ranges",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileDetailInputField(
                    label = "Fasting Target (80-130 mg/dL)",
                    value = targetFasting,
                    onValueChange = { targetFasting = it },
                    modifier = Modifier.weight(1f)
                )
                ProfileDetailInputField(
                    label = "Post-Meal Target (<180 mg/dL)",
                    value = targetPostmeal,
                    onValueChange = { targetPostmeal = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scroll vertically and select your details to update your personal profile.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Changes Button
            IosLiquidGlassButton(
                text = if (uiState.isSaving) "Saving Changes..." else "Save Changes",
                onClick = {
                    val ageNum = age.replace(Regex("[^0-9]"), "").toIntOrNull()
                    val heightNum = height.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val weightNum = weight.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val fastingNum = targetFasting.replace(Regex("[^0-9]"), "").toIntOrNull()
                    val postmealNum = targetPostmeal.replace(Regex("[^0-9]"), "").toIntOrNull()

                    viewModel.updateProfile(
                        fullName = fullName,
                        email = email,
                        diabetesType = diabetesType,
                        age = ageNum,
                        heightCm = heightNum,
                        weightKg = weightNum,
                        phone = phone,
                        gender = gender,
                        targetFastingGlucose = fastingNum,
                        targetPostmealGlucose = postmealNum,
                        onSuccess = {
                            showSuccessBanner = true
                        }
                    )
                },
            )

            Spacer(modifier = Modifier.height(100.dp))
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
            color = AppTheme.colors.textPrimary,
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
                focusedContainerColor = AppTheme.colors.inputBackground,
                unfocusedContainerColor = AppTheme.colors.inputBackground,
                focusedBorderColor = Color(0xFFA855F7),
                unfocusedBorderColor = AppTheme.colors.inputBorder,
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
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
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = selectedItem,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA855F7),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(items, key = { it }) { item ->
                    val isSelected = item == selectedItem
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF)) else Color.Transparent)
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFA855F7) else AppTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun bitmapToFile(context: Context, bitmap: Bitmap): File? {
    return try {
        val tempFile = File.createTempFile("profile_cam_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            output.flush()
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun RemoveProfilePhotoChip(
    fillWidth: Boolean,
    onClick: () -> Unit
) {
    val baseModifier = if (fillWidth) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
    }
    val shape = RoundedCornerShape(if (fillWidth) 12.dp else 16.dp)
    Box(
        modifier = baseModifier
            .clip(shape)
            .background(AppTheme.colors.errorBg)
            .border(1.dp, AppTheme.colors.errorBorder.copy(alpha = 0.65f), shape)
            .clickable { onClick() }
            .padding(
                horizontal = if (fillWidth) 14.dp else 12.dp,
                vertical = if (fillWidth) 11.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Remove profile photo",
                tint = AppTheme.colors.errorText,
                modifier = Modifier.size(if (fillWidth) 16.dp else 14.dp)
            )
            Spacer(modifier = Modifier.width(if (fillWidth) 6.dp else 5.dp))
            Text(
                text = "Remove Photo",
                fontSize = if (fillWidth) 13.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.errorText
            )
        }
    }
}

@Composable
fun ProfilePhotoOptionsDialog(
    hasProfilePhoto: Boolean = false,
    onDismissRequest: () -> Unit,
    onViewPictureClick: () -> Unit,
    onChooseGalleryClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile Picture Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "View or update your profile picture",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: View Picture
                PhotoOptionItem(
                    vectorIcon = Icons.Rounded.Visibility,
                    title = "View Profile Picture",
                    subtitle = "Open photo viewer in full size",
                    onClick = {
                        onDismissRequest()
                        onViewPictureClick()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Gallery
                PhotoOptionItem(
                    vectorIcon = Icons.Rounded.Image,
                    title = "Choose from Gallery",
                    subtitle = "Select a picture from your device photos",
                    onClick = {
                        onDismissRequest()
                        onChooseGalleryClick()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Take Photo
                PhotoOptionItem(
                    vectorIcon = Icons.Rounded.PhotoCamera,
                    title = "Take Photo",
                    subtitle = "Capture a new picture with camera",
                    onClick = {
                        onDismissRequest()
                        onTakePhotoClick()
                    }
                )

                if (hasProfilePhoto) {
                    Spacer(modifier = Modifier.height(10.dp))
                    RemoveProfilePhotoChip(
                        fillWidth = true,
                        onClick = {
                            onDismissRequest()
                            onRemovePhotoClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Cancel Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF3F4F6))
                        .clickable { onDismissRequest() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoOptionItem(
    vectorIcon: ImageVector? = null,
    icon: String = "",
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFA855F7).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (vectorIcon != null) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(20.dp)
                )
            } else if (icon.isNotEmpty()) {
                Text(text = icon, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = AppTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun ViewProfilePictureDialog(
    userName: String,
    hasProfilePhoto: Boolean = false,
    onDismissRequest: () -> Unit,
    onChooseGalleryClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Picture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF3F4F6))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Full Profile Avatar Display
                ProfileAvatarView(sizeDp = 180.dp, fontSizeSp = 60)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )

                Text(
                    text = "DiaWell Patient Profile Picture",
                    fontSize = 12.sp,
                    color = Color(0xFFA855F7)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons: Take Photo & Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFA855F7))
                            .clickable {
                                onDismissRequest()
                                onTakePhotoClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Take Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF))
                            .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                            .clickable {
                                onDismissRequest()
                                onChooseGalleryClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gallery",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
                    }
                }

                if (hasProfilePhoto) {
                    Spacer(modifier = Modifier.height(10.dp))
                    RemoveProfilePhotoChip(
                        fillWidth = true,
                        onClick = {
                            onDismissRequest()
                            onRemovePhotoClick()
                        }
                    )
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
