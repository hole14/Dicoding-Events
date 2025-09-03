package com.example.dicodingevent

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.dicodingevent.data.repository.SettingRepository
import com.example.dicodingevent.data.retrofit.ApiConfig
import com.example.dicodingevent.data.room.EventDatabase
import com.example.dicodingevent.data.worker.EventReminderManager
import com.example.dicodingevent.data.worker.NotificationPermissionHelper
import com.example.dicodingevent.data.worker.PreferencesManager
import com.example.dicodingevent.navigation.BottomNavItem
import com.example.dicodingevent.screens.DetailScreen
import com.example.dicodingevent.screens.FavoriteScreen
import com.example.dicodingevent.screens.FinishedScreen
import com.example.dicodingevent.screens.SettingScreen
import com.example.dicodingevent.screens.UpcomingScreen
import com.example.dicodingevent.ui.theme.DicodingEventTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var reminderManager: EventReminderManager
    private lateinit var database: EventDatabase
    private lateinit var settingRepository: SettingRepository

    companion object{
        private const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeNotificationComponents()

        checkNotificationPermission()

        setContent {
            DicodingEventTheme {
                LaunchedEffect(Unit) {
                    initializeNotificationComponents()
                }
                MainScreen()
            }
        }
    }
    private fun initializeNotificationComponents() {
        preferencesManager = PreferencesManager(this)
        reminderManager = EventReminderManager(this)

        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            EventDatabase::class.java,
            "event_database"
        ).build()

        // Initialize repository
        settingRepository = SettingRepository(
            context = this,
            eventDao = database.eventDao(),
            apiService = ApiConfig.getApiService()
        )
    }

    private fun checkNotificationPermission() {
        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this)
        }
    }

    private fun initializeNotificationServices() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Initializing notification services")

                // Menggunakan single switch logic
                if (preferencesManager.getEventNotification()) {
                    Log.d(TAG, "Event notification enabled, starting all services")

                    reminderManager.startAllEventServices()

                    settingRepository.refreshUpcomingEvents()

                    settingRepository.scheduleAllUpcomingReminders()

                    Log.d(TAG, "All notification services started")
                } else {
                    Log.d(TAG, "Event notifications disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing notification services", e)
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        when (requestCode) {
            NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
                    initializeNotificationServices()
                } else {
                    Toast.makeText(
                        this,
                        "Izin notifikasi diperlukan untuk pengingat event",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (NotificationPermissionHelper.hasNotificationPermission(this)) {
            lifecycleScope.launch {
                if (preferencesManager.getEventNotification()) {
                    settingRepository.refreshUpcomingEvents()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun MainScreen(){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute?.startsWith("detail/") != true
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav){
                NavigationBar(containerColor = Color.White, contentColor = Color.Black) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    val items = listOf(
                        BottomNavItem.Upcoming,
                        BottomNavItem.Finished,
                        BottomNavItem.Favorite,
                        BottomNavItem.Setting
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route} == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1976D2),
                                selectedTextColor = Color(0xFF1976D2),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Upcoming.route,
            modifier = if (showBottomNav) Modifier.padding(innerPadding) else Modifier
        ) {
            composable(BottomNavItem.Upcoming.route) {
                UpcomingScreen(
                    onEventClick =  { eventId ->
                        navController.navigate("detail/$eventId")
                    }
                )
            }
            composable(BottomNavItem.Finished.route){
                FinishedScreen(
                    onClickEvent = { eventId ->
                        navController.navigate("detail/$eventId")
                    }
                )
            }
            composable(BottomNavItem.Favorite.route){
                FavoriteScreen(
                    onEventClick = { eventId ->
                        navController.navigate("detail/$eventId")
                    }
                )
            }
            composable(BottomNavItem.Setting.route) {
                SettingScreen()
            }
            composable(
                route = "detail/{eventId}",
                arguments = listOf(navArgument("eventId"){ type = NavType.IntType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(400)
                    ) + fadeOut(animationSpec = tween(400))
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                DetailScreen(
                    eventId = eventId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}