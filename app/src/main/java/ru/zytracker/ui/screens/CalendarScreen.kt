package ru.zytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zytracker.data.model.Shift
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.util.ScheduleUtils
import ru.zytracker.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ЗЯ трекер") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Месяц навигация
            MonthNavigation(
                currentMonth = state.currentMonth,
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) }
            )
            
            // Календарь
            CalendarGrid(
                currentMonth = state.currentMonth,
                selectedDate = state.selectedDate,
                shifts = state.shifts,
                workDaysMap = state.workDaysMap,
                workSchedule = state.workSchedule,
                scheduleStartDate = state.scheduleStartDate,
                onDateSelected = { viewModel.selectDate(it) }
            )
            
            // Сводка за период (месяц/год)
            SummarySection(
                totalOrders = state.totalOrders,
                totalKilometers = state.totalKilometers,
                workDaysCount = state.workDaysMap.count { it.value == ScheduleUtils.DayType.SCHEDULED_WORK || it.value == ScheduleUtils.DayType.WORKED },
                workedDaysCount = state.workDaysMap.count { it.value == ScheduleUtils.DayType.WORKED }
            )
        }
    }
    
    // Диалог для добавления/редактирования смены
    if (state.showDialog) {
        ShiftDialog(
            selectedDate = state.selectedDate,
            existingShift = state.dialogShift,
            onSave = { orders, km -> viewModel.saveShift(orders, km) },
            onDelete = { viewModel.deleteShift() },
            onDismiss = {viewModel.setShowDialog(false) }
        )
    }
}

@Composable
fun MonthNavigation(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru"))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
        }
        
        Text(
            text = currentMonth.format(monthFormatter).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() 
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    shifts: List<Shift>,
    workDaysMap: Map<LocalDate, ScheduleUtils.DayType>,
    workSchedule: WorkSchedule,
    scheduleStartDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val yearMonth = YearMonth.from(currentMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value
    val emptyCellsBefore = (dayOfWeek + 6) % 7
    
    val daysList = mutableListOf<LocalDate?>()
    
    repeat(emptyCellsBefore) {
        daysList.add(null)
    }
    
    for (day in 1..daysInMonth) {
        daysList.add(yearMonth.atDay(day))
    }
    
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.heightIn(max = 350.dp)
        ) {
            items(daysList) { date ->
                if (date != null) {
                    val hasShift = shifts.any { it.date == date.toString() }
                    val isSelected = date == selectedDate
                    val dayType = workDaysMap[date] ?: if (ScheduleUtils.isWorkDay(date, workSchedule, scheduleStartDate)) {
                        ScheduleUtils.DayType.SCHEDULED_WORK
                    } else {
                        ScheduleUtils.DayType.OFF
                    }

                    DayCell(
                        date = date,
                        hasShift = hasShift,
                        isSelected = isSelected,
                        dayType = dayType,
                        onClick = { onDateSelected(date) }
                    )
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    hasShift: Boolean,
    isSelected: Boolean,
    dayType: ScheduleUtils.DayType,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    val backgroundColor = when (dayType) {
        ScheduleUtils.DayType.WORKED -> MaterialTheme.colorScheme.primary
        ScheduleUtils.DayType.SCHEDULED_WORK -> Color(0xFF8BC34A) // Лаймовый цвет для рабочего дня
        ScheduleUtils.DayType.OFF -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color.Black) // Внешняя обводка
            .padding(if (isSelected) 2.dp else 0.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    dayType == ScheduleUtils.DayType.WORKED -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasShift) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (dayType == ScheduleUtils.DayType.WORKED) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun SummarySection(
    totalOrders: Int,
    totalKilometers: Double,
    workDaysCount: Int,
    workedDaysCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Итого за месяц",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Заказов",
                    value = totalOrders.toString()
                )
                
                SummaryItem(
                    icon = Icons.Default.DirectionsCar,
                    label = "Километров",
                    value = String.format("%.1f км", totalKilometers)
                )
                
                SummaryItem(
                    icon = Icons.Default.EventAvailable,
                    label = "Смен",
                    value = "$workedDaysCount / $workDaysCount"
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShiftDialog(
    selectedDate: LocalDate,
    existingShift: Shift?,
    onSave: (Int, Double) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var orders by remember { mutableStateOf(existingShift?.orders?.toString() ?: "") }
    var kilometers by remember { mutableStateOf(existingShift?.kilometers?.toString() ?: "") }
    var ordersError by remember { mutableStateOf<String?>(null) }
    var kmError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingShift != null) "Редактировать смену" else "Добавить смену",
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = selectedDate.format(dateFormatter),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = orders,
                    onValueChange = { 
                        orders = it
                        ordersError = null
                    },
                    label = { Text("Количество заказов (1-150)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ordersError != null,
                    supportingText = { ordersError?.let { Text(it) } }
                )

                OutlinedTextField(
                    value = kilometers,
                    onValueChange = { 
                        kilometers = it
                        kmError = null
                    },
                    label = { Text("Километры (1-350)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = kmError != null,
                    supportingText = { kmError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (orders.isBlank() && kilometers.isBlank()) {
                        onDismiss()
                        return@Button
                    }
                    
                    var hasError = false
                    
                    if (orders.isNotBlank()) {
                        val ordersIntVal = orders.toIntOrNull()
                        if (ordersIntVal == null || ordersIntVal < 1 || ordersIntVal > 150) {
                            ordersError = "От 1 до 150"
                            hasError = true
                        }
                    }
                    
                    if (kilometers.isNotBlank()) {
                        val kmDoubleVal = kilometers.toDoubleOrNull()
                        if (kmDoubleVal == null || kmDoubleVal < 1.0 || kmDoubleVal > 350.0) {
                            kmError = "От 1 до 350"
                            hasError = true
                        }
                    }
                    
                    if (!hasError) {
                        val ordersVal = orders.toIntOrNull() ?: 0
                        val kmVal = kilometers.toDoubleOrNull() ?: 0.0
                        onSave(ordersVal, kmVal)
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            if (existingShift != null) {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
