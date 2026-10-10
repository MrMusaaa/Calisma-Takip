package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ActiveShiftCard
import com.example.ui.components.AddSessionDialog
import com.example.ui.components.HourlyRateDialog
import com.example.ui.components.PaydayCard
import com.example.ui.components.SessionItemCard
import com.example.ui.components.WeeklyBarChart
import com.example.ui.components.WidgetGuideDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkHomeScreen(
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mesai Takip",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Salı Maaş Günü",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRateDialog = true },
                        modifier = Modifier.testTag("rate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Saatlik Ücret",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showGuideDialog = true },
                        modifier = Modifier.testTag("guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Widget Rehberi",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.testTag("add_session_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Mesai Ekle",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Live Active Shift / Stopwatch Card
            item {
                ActiveShiftCard(
                    isWorking = uiState.isWorking,
                    startTimeMillis = uiState.activeStartTimeMillis,
                    elapsedSeconds = uiState.activeElapsedSeconds,
                    hourlyRate = uiState.hourlyRate,
                    currencySymbol = uiState.currencySymbol,
                    onStartWork = { note -> viewModel.startWork(note) },
                    onStopWork = { viewModel.stopWork() },
                    onEditHourlyRate = { showRateDialog = true }
                )
            }

            // 2. Tuesday Payday Card (Salı Maaş Günü)
            item {
                PaydayCard(
                    weekPeriod = uiState.currentWeekPeriod,
                    paydayStatus = uiState.paydayStatus,
                    totalWeekHours = uiState.totalWeekHours,
                    totalWeekEarnings = uiState.totalWeekEarnings,
                    currencySymbol = uiState.currencySymbol,
                    isWeekPaid = uiState.isWeekPaid,
                    onTogglePaid = { viewModel.toggleWeekPaid() }
                )
            }

            // 3. Week Navigation Bar
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                            onClick = { viewModel.previousWeek() },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Önceki Hafta",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.selectedWeekOffset == 0) "Bu Hafta" else "${uiState.selectedWeekOffset} Hafta",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = uiState.currentWeekPeriod.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            if (uiState.selectedWeekOffset != 0) {
                                FilledTonalButton(
                                    onClick = { viewModel.resetToCurrentWeek() },
                                    modifier = Modifier.height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Bugün", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { viewModel.nextWeek() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Sonraki Hafta",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Weekly Bar Chart (Monday to Sunday)
            item {
                WeeklyBarChart(
                    dayBreakdowns = uiState.dayBreakdown,
                    currencySymbol = uiState.currencySymbol
                )
            }

            // 5. Weekly Stats Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avg Hours per shift
                    val shiftCount = uiState.weekSessions.size
                    val avgHours = if (shiftCount > 0) uiState.totalWeekHours / shiftCount else 0.0

                    QuickStatCard(
                        title = "Vardiya Sayısı",
                        value = "$shiftCount gün",
                        subtitle = "Bu hafta",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        title = "Vardiya Ortalaması",
                        value = "${String.format(Locale.US, "%.1f", avgHours)} saat",
                        subtitle = "Gün başına",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. Section Header: Shift Records
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Haftalık Mesai Geçmişi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${uiState.weekSessions.size} Kayıt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7. List of shifts or Empty State
            if (uiState.weekSessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Bu Hafta Henüz Mesai Kaydı Yok",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Yukarıdaki 'Mesaiye Başla' butonuna veya kilit ekranı widget'ına dokunarak ilk mesainizi başlatabilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.weekSessions,
                    key = { it.id }
                ) { session ->
                    SessionItemCard(
                        session = session,
                        currencySymbol = uiState.currencySymbol,
                        onDelete = { viewModel.deleteSession(session) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddSessionDialog(
            defaultHourlyRate = uiState.hourlyRate,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { date, sH, sM, eH, eM, brk, rate, note ->
                viewModel.addManualSession(date, sH, sM, eH, eM, brk, rate, note)
                showAddDialog = false
            }
        )
    }

    if (showRateDialog) {
        HourlyRateDialog(
            currentRate = uiState.hourlyRate,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showRateDialog = false },
            onSave = { newRate ->
                viewModel.updateHourlyRate(newRate)
                showRateDialog = false
            }
        )
    }

    if (showGuideDialog) {
        WidgetGuideDialog(
            onDismiss = { showGuideDialog = false }
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
