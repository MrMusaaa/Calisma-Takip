package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WidgetGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kilit Ekranı & Widget Rehberi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                GuideSection(
                    icon = Icons.Default.Lock,
                    title = "1. Kilit Ekranı & Widget Ekleme",
                    description = "Ana ekrana veya kilit ekranınıza boş bir alana basılı tutun > 'Widget'lar' seçeneğinden 'Mesai Takip' widget'ını seçip ekleyin. Kilit ekranınızda veya ana ekranınızda tek dokunuşla Başlat/Bitir yapabilirsiniz."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideSection(
                    icon = Icons.Default.NotificationsActive,
                    title = "2. Canlı Kilit Ekranı Bildirimi",
                    description = "Mesaiye başladığınızda kilit ekranınızda otomatik olarak canlı kronometreli bildirim belirir. Telefon kilidini açmadan doğrudan kilit ekranından 'BİTİR' butonuna basabilirsiniz."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideSection(
                    icon = Icons.Default.Widgets,
                    title = "3. Kilit Ekranından Hızlı Ayarlar Kutucuğu (Tile)",
                    description = "Kilit ekranından parmağınızı yukarıdan aşağı kaydırarak bildirim paneline 'Mesai Takip' kutucuğunu ekleyebilirsiniz. Telefonun kilidini bile açmadan tek dokunuşla başlatıp bitirebilirsiniz."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideSection(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "3. Pil Dostu Mimari (Sıfır Pil Tüketimi)",
                    description = "Uygulama arka planda gereksiz döngüler veya CPU uyandırmaları çalıştırmaz. Sadece başlama butonuna bastığınız andaki saati, ve bitir dediğiniz andaki saati hafızaya kaydeder ve süreyi tam hesaplar."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideSection(
                    icon = Icons.Default.CalendarToday,
                    title = "4. Pazartesi-Pazar Döngüsü & Salı Maaş Günü",
                    description = "Mesaileriniz her hafta Pazartesi 00:00 ile Pazar 23:59 arasında toplanır. Her Salı günü maaş günü olarak hesaplanır ve hakediş tutarınız önünüze gelir."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Anladım")
            }
        }
    )
}

@Composable
private fun GuideSection(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
