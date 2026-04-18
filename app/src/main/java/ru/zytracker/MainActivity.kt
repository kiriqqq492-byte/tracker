package ru.zytracker

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import ru.zytracker.data.AppDatabase
import ru.zytracker.data.model.CourierProfile
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.data.preferences.SettingsRepository
import ru.zytracker.data.repository.CourierProfileRepository
import ru.zytracker.data.repository.ShiftRepository
import ru.zytracker.ui.screens.CalendarScreen
import ru.zytracker.ui.screens.SettingsScreen
import ru.zytracker.ui.screens.WelcomeScreen
import ru.zytracker.ui.theme.ZYTrackerTheme
import ru.zytracker.viewmodel.CalendarViewModel
import ru.zytracker.viewmodel.SettingsViewModel
import ru.zytracker.viewmodel.WelcomeViewModel
import ru.zytracker.viewmodel.WelcomeViewModelFactory
import ru.zytracker.viewmodel.CalendarViewModelFactory
import ru.zytracker.viewmodel.SettingsViewModelFactory
import ru.zytracker.navigation.Screen

import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleNotification(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(application)
        val shiftRepository = ShiftRepository(database.shiftDao())
        val profileRepository = CourierProfileRepository(database.courierProfileDao())
        val settingsRepository = SettingsRepository(application)
        
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")
            
            ZYTrackerTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val hasProfile by profileRepository.hasProfileFlow().collectAsState(initial = false)

                LaunchedEffect(hasProfile) {
                    if (!hasProfile) {
                        val defaultProfile = CourierProfile(
                            name = "",
                            workSchedule = WorkSchedule.FIVE_TWO,
                            scheduleStartDate = null
                        )
                        profileRepository.insertProfile(defaultProfile)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        navController = navController,
                        shiftRepository = shiftRepository,
                        profileRepository = profileRepository,
                        settingsRepository = settingsRepository
                    )
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            scheduleNotification(this)
        }
    }
}

@Composable
fun WelcomeScreenApp(
    profileRepository: CourierProfileRepository,
    onCompleted: () -> Unit
) {
    val viewModel: WelcomeViewModel = viewModel(factory = WelcomeViewModelFactory(profileRepository))

    WelcomeScreen(
        viewModel = viewModel,
        onCompleted = onCompleted
    )
}

@Composable
fun MainApp(
    navController: NavHostController,
    shiftRepository: ShiftRepository,
    profileRepository: CourierProfileRepository,
    settingsRepository: SettingsRepository
) {
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(shiftRepository, profileRepository, settingsRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(profileRepository, settingsRepository)
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Settings.route) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("Календарь", Icons.Default.Home),
            BottomNavItem("Настройки", Icons.Default.Settings)
        )
        
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false,
                onClick = {
                    when (item.label) {
                        "Календарь" -> navController.navigate(Screen.Calendar.route)
                        "Настройки" -> navController.navigate(Screen.Settings.route)
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

fun scheduleNotification(context: Context) {
    val workManager = androidx.work.WorkManager.getInstance(context)
    val workRequest = androidx.work.PeriodicWorkRequestBuilder<ru.zytracker.worker.NotificationWorker>(
        1, java.util.concurrent.TimeUnit.DAYS
    )
        .setInitialDelay(calculateInitialDelay(), java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
    
    workManager.enqueueUniquePeriodicWork(
        "daily_notification",
        androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
}

fun calculateInitialDelay(): Long {
    val now = java.util.Calendar.getInstance()
    val targetHour = 20
    val targetMinute = 0
    
    val target = java.util.Calendar.getInstance()
    target.set(java.util.Calendar.HOUR_OF_DAY, targetHour)
    target.set(java.util.Calendar.MINUTE, targetMinute)
    target.set(java.util.Calendar.SECOND, 0)
    
    if (target.before(now)) {
        target.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    
    return target.timeInMillis - now.timeInMillis
}
