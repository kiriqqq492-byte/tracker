package ru.zytracker.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zytracker.data.model.WorkSchedule
import ru.zytracker.viewmodel.WelcomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api as ExperimentalMaterial3Api1

@OptIn(ExperimentalMaterial3Api1::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onCompleted: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (state.isCompleted) {
        LaunchedEffect(Unit) {
            onCompleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ЗЯ трекер",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выберите график работы",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "График работы",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        WorkScheduleSelector(
            selectedSchedule = state.selectedSchedule,
            onScheduleSelected = { viewModel.updateSchedule(it) }
        )

        if (state.selectedSchedule != null && state.selectedSchedule != WorkSchedule.FIVE_TWO) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                val dateText = state.scheduleStartDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Выбрать дату начала"
                Text(dateText)
            }
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading && state.selectedSchedule != null
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Продолжить", fontSize = 16.sp)
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                viewModel.updateScheduleStartDate(date)
                showDatePicker = false
            },
            initialDate = state.scheduleStartDate ?: LocalDate.now()
        )
    }
}

@OptIn(ExperimentalMaterial3Api1::class)
@Composable
fun WorkScheduleSelector(
    selectedSchedule: WorkSchedule?,
    onScheduleSelected: (WorkSchedule) -> Unit
) {
    val schedules = WorkSchedule.entries.toTypedArray()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        schedules.forEach { schedule ->
            FilterChip(
                selected = selectedSchedule == schedule,
                onClick = { onScheduleSelected(schedule) },
                label = { Text(schedule.displayName) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api1::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Выберите дату начала") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
}
