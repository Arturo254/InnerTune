package com.malopieds.innertune.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.viewmodels.BackupRestoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupAndRestore(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: BackupRestoreViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var uploadStatus by remember { mutableStateOf<UploadStatus?>(null) }

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            if (uri != null) {
                viewModel.backup(context, uri)
                coroutineScope.launch {
                    uploadStatus = UploadStatus.Uploading
                    val fileUrl = uploadBackupToCloud(context, uri)
                    uploadStatus = if (fileUrl != null) {
                        UploadStatus.Success(fileUrl)
                    } else {
                        UploadStatus.Failure
                    }
                }
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                viewModel.restore(context, uri)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        BackupCard(onClick = {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            backupLauncher.launch("${context.getString(R.string.app_name)}_${LocalDateTime.now().format(formatter)}.backup")
        })

        RestoreCard(onClick = {
            restoreLauncher.launch(arrayOf("application/octet-stream"))
        })

        when (val status = uploadStatus) {
            is UploadStatus.Uploading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Uploading backup to cloud...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            is UploadStatus.Success -> {
                UploadSuccessCard(
                    fileUrl = status.fileUrl,
                    onCopyClick = {
                        copyToClipboard(context, status.fileUrl)
                    }
                )
            }
            is UploadStatus.Failure -> {
                Text(
                    text = "Upload failed. Please try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            null -> { /* No status to show */ }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.backup_restore)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
fun BackupCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.backup),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.backup),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun RestoreCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.restore),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.restore),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun UploadSuccessCard(fileUrl: String, onCopyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.backup_link_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.backup_link_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onCopyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.copy_link))
                    }
                }
            }
        }
    }
}

suspend fun uploadBackupToCloud(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "temp_backup.backup")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val client = OkHttpClient()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", tempFile.name,
                    tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://file.io")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            // Parse the JSON response to get the file URL
            val fileUrl = responseBody?.let { JSONObject(it).getString("link") }

            tempFile.delete() // Clean up the temporary file

            fileUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Backup URL", text)
    clipboard.setPrimaryClip(clip)
}

sealed class UploadStatus {
    data object Uploading : UploadStatus()
    data class Success(val fileUrl: String) : UploadStatus()
    data object Failure : UploadStatus()
}