package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeekHelper
import java.time.LocalDate

@Composable
fun AddSessionDialog(
    defaultHourlyRate: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        breakMinutes: Int,
        hourlyRate: Double,
        note: String
    ) -> Unit
) {
    val initialDate = LocalDate.now()
    var selectedDate by remember { mutableStateOf(initialDate) }

    var dayText by remember { mutableStateOf(selectedDate.dayOfMonth.toString()) }
    var monthText by remember { mutableStateOf(selectedDate.monthValue.toString()) }
    var yearText by remember { mutableStateOf(selectedDate.year.toString()) }

    var startHourText by remember { mutableStateOf("09") }
    var startMinuteText by remember { mutableStateOf("00") }
    var endHourText by remember { mutableStateOf("17") }
    var endMinuteText by remember { mutableStateOf("30") }
    var breakMinutesText by remember { mutableStateOf("30") }
    var rateText by remember { mutableStateOf(defaultHourlyRate.toInt().toString()) }
    var noteText by remember { mutableStateOf("") }

    fun syncDateState(newDate: LocalDate) {
        selectedDate = newDate
        dayText = newDate.dayOfMonth.toString()
        monthText = newDate.monthValue.toString()
        yearText = newDate.year.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manuel Mesai Ekle",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Selected Date Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { syncDateState(selectedDate.minusDays(1)) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Önceki Gün")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedDate.format(WeekHelper.DATE_FORMATTER),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        FilledTonalIconButton(
                            onClick = { syncDateState(selectedDate.plusDays(1)) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sonraki Gün")
                        }
                    }
                }

                // Explicit Date Inputs: Gün / Ay / Yıl
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = {
                            if (it.length <= 2) {
                                dayText = it
                                val d = it.toIntOrNull()
                                if (d != null && d in 1..31) {
                                    try {
                                        selectedDate = selectedDate.withDayOfMonth(d)
                                    } catch (_: Exception) {}
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Gün") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = monthText,
                        onValueChange = {
                            if (it.length <= 2) {
                                monthText = it
                                val m = it.toIntOrNull()
                                if (m != null && m in 1..12) {
                                    try {
                                        selectedDate = selectedDate.withMonth(m)
                                    } catch (_: Exception) {}
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ay") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = yearText,
                        onValueChange = {
                            if (it.length <= 4) {
                                yearText = it
                                val y = it.toIntOrNull()
                                if (y != null && y in 2020..2035) {
                                    try {
                                        selectedDate = selectedDate.withYear(y)
                                    } catch (_: Exception) {}
                                }
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        label = { Text("Yıl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Quick buttons: Bugün / Dün
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedDate == LocalDate.now(),
                        onClick = { syncDateState(LocalDate.now()) },
                        label = { Text("Bugün") }
                    )
                    FilterChip(
                        selected = selectedDate == LocalDate.now().minusDays(1),
                        onClick = { syncDateState(LocalDate.now().minusDays(1)) },
                        label = { Text("Dün") }
                    )
                }

                // Start Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Başlangıç:",
                        modifier = Modifier.width(76.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = startHourText,
                        onValueChange = { if (it.length <= 2) startHourText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Saat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(text = " : ", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = startMinuteText,
                        onValueChange = { if (it.length <= 2) startMinuteText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Dk") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bitiş:",
                        modifier = Modifier.width(76.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = endHourText,
                        onValueChange = { if (it.length <= 2) endHourText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Saat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(text = " : ", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = endMinuteText,
                        onValueChange = { if (it.length <= 2) endMinuteText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Dk") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Break & Hourly rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = breakMinutesText,
                        onValueChange = { breakMinutesText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Mola (dk)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Saat Ücreti ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Optional Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Not (isteğe bağlı)") },
                    placeholder = { Text("Örn: Fazla mesai, vardiya notu") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sH = startHourText.toIntOrNull()?.coerceIn(0, 23) ?: 9
                    val sM = startMinuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val eH = endHourText.toIntOrNull()?.coerceIn(0, 23) ?: 17
                    val eM = endMinuteText.toIntOrNull()?.coerceIn(0, 59) ?: 30
                    val brk = breakMinutesText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val rate = rateText.toDoubleOrNull() ?: defaultHourlyRate

                    onConfirm(selectedDate, sH, sM, eH, eM, brk, rate, noteText)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
