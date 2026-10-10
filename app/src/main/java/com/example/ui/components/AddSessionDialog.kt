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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
    var selectedDateOffset by remember { mutableStateOf(0) } // 0 = today, -1 = yesterday
    var startHourText by remember { mutableStateOf("09") }
    var startMinuteText by remember { mutableStateOf("00") }
    var endHourText by remember { mutableStateOf("17") }
    var endMinuteText by remember { mutableStateOf("30") }
    var breakMinutesText by remember { mutableStateOf("30") }
    var rateText by remember { mutableStateOf(defaultHourlyRate.toInt().toString()) }
    var noteText by remember { mutableStateOf("") }

    val targetDate = LocalDate.now().plusDays(selectedDateOffset.toLong())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manuel Mesai Ekle",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date selection chips
                Text(
                    text = "Tarih Seçimi:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedDateOffset == 0,
                        onClick = { selectedDateOffset = 0 },
                        label = { Text("Bugün") }
                    )
                    FilterChip(
                        selected = selectedDateOffset == -1,
                        onClick = { selectedDateOffset = -1 },
                        label = { Text("Dün") }
                    )
                    FilterChip(
                        selected = selectedDateOffset == -2,
                        onClick = { selectedDateOffset = -2 },
                        label = { Text("2 Gün Önce") }
                    )
                }

                // Start Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Başlangıç:",
                        modifier = Modifier.width(80.dp),
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
                        label = { Text("Dakika") },
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
                        modifier = Modifier.width(80.dp),
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
                        label = { Text("Dakika") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Break & Rate row
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
                    placeholder = { Text("Örn: Fazla mesai, hafta sonu vardiyası") },
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

                    onConfirm(targetDate, sH, sM, eH, eM, brk, rate, noteText)
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
