package ru.zytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showStartDateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // График работы
            ScheduleSection(
                schedule = state.profile?.workSchedule?.displayName ?: "",
                onClick = { showScheduleDialog = true }
            )
            
            // Дата начала отсчёта
            if (state.profile?.workSchedule != WorkSchedule.FIVE_TWO && 
                state.profile?.workSchedule != WorkSchedule.CUSTOM) {
                StartDateSection(
                    startDate = state.profile?.scheduleStartDate,
                    onClick = { showStartDateDialog = true }
                )
            }
            
            Divider()
            
            // Тема
            ThemeSection(
                currentTheme = state.themeMode,
                onThemeChange = { viewModel.setThemeMode(it) }
            )
            
            Divider()
            
            // Уведомления
            NotificationSection(
                enabled = state.notificationsEnabled,
                onToggle = { viewModel.setNotificationsEnabled(it) }
            )
        }
    }
    
    if (showScheduleDialog) {
        ScheduleDialog(
            currentSchedule = state.profile?.workSchedule ?: WorkSchedule.FIVE_TWO,
            onScheduleSelected = {
                viewModel.updateSchedule(it)
                showScheduleDialog = false
            },
            onDismiss = { showScheduleDialog = false }
        )
    }
    
    if (showStartDateDialog) {
        StartDateDialog(
            currentDate = state.profile?.scheduleStartDate,
            onDateSelected = { date ->
                viewModel.updateScheduleStartDate(date)
                showStartDateDialog = false
            },
            onDismiss = { showStartDateDialog = false }
        )
    }
}

@Composable
fun ScheduleSection(
    schedule: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "График работы",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Изменить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = schedule,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StartDateSection(
    startDate: String?,
    onClick: () -> Unit
) {
    val displayDate = startDate?.let {
        try {
            val date = LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            "Не установлена"
        }
    } ?: "Не установлена"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дата начала отсчёта",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Изменить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayDate,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Первый рабочий день вашего цикла",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDateDialog(
    currentDate: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val initialDate = currentDate?.let {
        try {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }
    } ?: LocalDate.now()
    
    var selectedDate by remember { mutableStateOf(initialDate) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    selectedDate = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(millis),
                        ZoneId.systemDefault()
                    )
                }
                onDateSelected(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ScheduleDialog(
    currentSchedule: WorkSchedule,
    onScheduleSelected: (WorkSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSchedule by remember { mutableStateOf(currentSchedule) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите график работы") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkSchedule.entries.forEach { schedule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSchedule = schedule }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = schedule.displayName,
                                fontSize = 16.sp
                            )
                            Text(
                                text = when (schedule) {
                                    WorkSchedule.FIVE_TWO -> "Пн-Пт рабочие, Сб-Вс выходные"
                                    WorkSchedule.TWO_TWO -> "2 дня работа, 2 дня отдых"
                                    WorkSchedule.THREE_THREE -> "3 дня работа, 3 дня отдых"
                                    WorkSchedule.TWO_TWO_THREE -> "2+2+3 рабочих, 7 дней отдых"
                                    WorkSchedule.CUSTOM -> "Настраиваемый график"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        RadioButton(
                            selected = selectedSchedule == schedule,
                            onClick = { selectedSchedule = schedule }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onScheduleSelected(selectedSchedule) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ThemeSection(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Тема оформления",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            val themes = listOf(
                "system" to "Системная",
                "light" to "Светлая",
                "dark" to "Темная"
            )
            
            themes.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp
                    )
                    
                    RadioButton(
                        selected = currentTheme == value,
                        onClick = { onThemeChange(value) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Уведомления",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Ежедневное напоминание в 20:00",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}
