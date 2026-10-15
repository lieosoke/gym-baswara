package com.gymbaswara.app.feature.backup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleCrimson
import com.gymbaswara.app.core.ui.theme.AppleCyan
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val cloudInfo by viewModel.cloudInfo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.let { stream ->
                    viewModel.exportToLocalFile(stream)
                }
            } catch (e: Exception) {
                // Handled in viewModel
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.let { stream ->
                    viewModel.importFromLocalFile(stream)
                }
            } catch (e: Exception) {
                // Handled in viewModel
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cadangkan & Pulihkan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (cloudInfo.isConnected) {
                        IconButton(
                            onClick = { viewModel.refreshCloudMetadata() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Perbarui status cadangan",
                                tint = AppleVolt
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Notifikasi Sukses
                uiState.successMessage?.let { msg ->
                    Surface(
                        color = AppleVolt.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AppleVolt.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppleVolt,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearMessages() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = AppleVolt,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Notifikasi Error
                uiState.errorMessage?.let { err ->
                    Surface(
                        color = AppleCrimson.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AppleCrimson.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppleCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearMessages() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = AppleCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // KARTU 1: GOOGLE DRIVE BACKUP (Apple iCloud Style)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(AppleGrayDarkControl, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = AppleVolt,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Google Drive Cloud",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (cloudInfo.isConnected) cloudInfo.connectedEmail.orEmpty() else "Belum terhubung",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppleGraySubtext
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (cloudInfo.isConnected) AppleVolt.copy(alpha = 0.15f) else AppleGrayDarkControl
                            ) {
                                Text(
                                    text = if (cloudInfo.isConnected) "Aktif" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cloudInfo.isConnected) AppleVolt else AppleGraySubtext,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Cadangkan seluruh riwayat latihan dan rutinitas ke akun Google Drive Anda. Data tersimpan aman di folder aplikasi dan dapat dipulihkan kapan saja.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleGraySubtext,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (cloudInfo.isConnected) {
                            // Status Cadangan Cloud Inset Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AppleGrayDarkControl,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    val lastTime = cloudInfo.remoteFile?.modifiedTimeMillis
                                        ?: cloudInfo.localLastBackupAt
                                    val size = cloudInfo.remoteFile?.sizeBytes
                                        ?: cloudInfo.localLastBackupSize

                                    Text(
                                        text = "CADANGAN TERAKHIR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleVolt
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lastTime > 0) formatTimestamp(lastTime) else "Belum ada file cadangan di Google Drive",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    if (size > 0) {
                                        Text(
                                            text = "Ukuran file: ${size / 1024} KB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppleGraySubtext
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.backupToDrive() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppleVolt,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Cadangkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.requestRestoreFromDrive() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, AppleGrayDarkControl),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Pulihkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { viewModel.disconnectDrive() },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = "Putuskan Akun Google Drive",
                                    color = AppleCrimson,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Button(
                                onClick = { googleSignInLauncher.launch(viewModel.getSignInIntent()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppleVolt,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Hubungkan Akun Google Drive", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                // KARTU 2: CADANGAN FILE LOKAL (OFFLINE 100%)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(AppleGrayDarkControl, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = AppleCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Cadangan File Lokal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mode Mandiri (100% Offline)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleGraySubtext
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Simpan data latihan ke file cadangan di memori perangkat Anda. File cadangan dapat disimpan di folder Unduhan atau dipindahkan antar perangkat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleGraySubtext,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    createDocumentLauncher.launch("gymbaswara_backup_$dateStr.gz")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, AppleGrayDarkControl),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Ekspor File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    openDocumentLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, AppleGrayDarkControl),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Impor File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dialog Loading
            if (uiState.isLoading) {
                Surface(
                    color = AppleGrayDarkCard.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AppleGrayDarkControl),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp, color = AppleVolt)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.loadingMessage ?: "Memproses...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Dialog Konfirmasi Pemulihan
            if (uiState.showRestoreConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissRestoreDialog() },
                    containerColor = AppleGrayDarkCard,
                    title = {
                        Text(
                            text = "Konfirmasi Pemulihan Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "Seluruh data latihan di Google Drive akan disinkronkan ke database aplikasi ini. Pastikan koneksi internet stabil saat proses berlangsung.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleGraySubtext
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.confirmRestoreFromDrive() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppleVolt,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Ya, Pulihkan", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissRestoreDialog() },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Batal", color = AppleGraySubtext, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0) return "-"
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(millis))
}
