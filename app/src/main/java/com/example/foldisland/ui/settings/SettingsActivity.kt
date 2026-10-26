package com.example.foldisland.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.foldisland.core.overlay.IslandOverlayService
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.CalendarEventsManager
import com.example.foldisland.managers.ClipboardStore
import com.example.foldisland.managers.HUDManager
import com.example.foldisland.managers.MediaManager

class SettingsActivity : ComponentActivity() {

    private val requestCalendarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                CalendarEventsManager.loadDeviceCalendar(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClipboardStore.initialize(this)

        setContent {
            SettingsScreen(
                onOpenOverlaySettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                },
                onOpenNotificationSettings = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    startActivity(intent)
                },
                onRequestCalendarPermission = {
                    requestCalendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                },
                onStartOverlayService = {
                    IslandOverlayService.startService(this)
                },
                onStopOverlayService = {
                    IslandOverlayService.stopService(this)
                }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onStartOverlayService: () -> Unit,
    onStopOverlayService: () -> Unit
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasCalendarPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        hasCalendarPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCalendarPermission) {
            CalendarEventsManager.loadDeviceCalendar(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslandColors.PureBlack)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Title Banner
        Column(modifier = Modifier.padding(top = 24.dp)) {
            Text(
                text = "🏝️ Dynamic Island",
                color = IslandColors.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Samsung Galaxy Fold 7 Çentik ve Çoklu Görev Yöneticisi",
                color = IslandColors.TextSecondary,
                fontSize = 13.sp
            )
        }

        // Service Master Controls
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IslandColors.GlassSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Servis Durumu",
                    color = IslandColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartOverlayService,
                        colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Servisi Başlat", color = IslandColors.PureBlack, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onStopOverlayService,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IslandColors.NeonRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Durdur")
                    }
                }
            }
        }

        // Permissions Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IslandColors.GlassSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gerekli İzinler",
                    color = IslandColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                PermissionItem(
                    title = "Diğer Uygulamaların Üstünde Gösterim",
                    desc = "Çentiğin her ekranda görünmesi için zorunludur",
                    isGranted = hasOverlayPermission,
                    onGrantClick = onOpenOverlaySettings
                )

                PermissionItem(
                    title = "Medya ve Bildirim Erişimi",
                    desc = "Spotify ve müzik oynatıcı kontrolleri için",
                    isGranted = false,
                    onGrantClick = onOpenNotificationSettings
                )

                PermissionItem(
                    title = "Google Takvim İzni",
                    desc = "Kalan günler ve geri sayımları çentiğe sabitlemek için",
                    isGranted = hasCalendarPermission,
                    onGrantClick = onRequestCalendarPermission
                )
            }
        }

        // HUD Interactive Test Controls
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IslandColors.GlassSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Canlı HUD Test Simülatörü",
                    color = IslandColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aşağıdaki butonlara basarak ada animasyonlarını test edebilirsiniz:",
                    color = IslandColors.TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { HUDManager.showVolume(level = 65) },
                        colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = IslandColors.PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ses HUD", color = IslandColors.PureBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { HUDManager.showBrightness(level = 80) },
                        colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonYellow),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Brightness6, contentDescription = null, tint = IslandColors.PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Parlaklık", color = IslandColors.PureBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { HUDManager.showCharging(percentage = 92) },
                        colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = IslandColors.PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Şarj HUD", color = IslandColors.PureBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { HUDManager.showPinnedNotice("🏖️", "Ege Tatili: 12 gün kaldı") },
                        colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonOrange),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = IslandColors.PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("İğnele HUD", color = IslandColors.PureBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        MediaManager.updateMediaInfo(
                            title = "Blinding Lights",
                            artist = "The Weeknd",
                            isPlaying = !MediaManager.currentTrack.value.isPlaying
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonPurple),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = IslandColors.TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Medya & Ekolayzer Simüle Et", color = IslandColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun PermissionItem(
    title: String,
    desc: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IslandColors.GlassSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isGranted) IslandColors.NeonGreen else IslandColors.GlassBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isGranted) IslandColors.PureBlack else IslandColors.TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = IslandColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                color = IslandColors.TextSecondary,
                fontSize = 11.sp
            )
        }

        if (!isGranted) {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = IslandColors.NeonBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("İzin Ver", fontSize = 11.sp, color = IslandColors.PureBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}
