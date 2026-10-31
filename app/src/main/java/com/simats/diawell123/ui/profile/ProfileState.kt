package com.simats.diawell123.ui.profile
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap

object ProfileState {
    var userName by mutableStateOf("Mary Jane")
    var userEmail by mutableStateOf("mary.jane@example.com")
    var profileImageBitmap by mutableStateOf<ImageBitmap?>(null)
}
