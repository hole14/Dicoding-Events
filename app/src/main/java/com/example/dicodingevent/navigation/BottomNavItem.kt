package com.example.dicodingevent.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem (
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Upcoming: BottomNavItem(
        route = "upcoming",
        title = "Upcoming",
        icon = Icons.Default.Event
    )
    object Finished: BottomNavItem(
        route = "finished",
        title = "Finished",
        icon = Icons.Default.EventAvailable
    )
    object Favorite: BottomNavItem(
        route = "favorite",
        title = "Favorite",
        icon = Icons.Default.Favorite
    )
}