package com.dhanuk.quickscanpro.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dhanuk.quickscanpro.ui.design.CameraPreviewBox
import com.dhanuk.quickscanpro.ui.design.previewHeight
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TextScanScreen"

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TextScanScreen(
    onNavigateBack: () -> Unit,
    onTextExtracted: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPerm = granted }
    LaunchedEffect(Unit) { if (!hasPerm) permLauncher.launch(Manifest.permission.CAMERA) }

    var captured by rememberSaveable { mutableStateOf("") }
    var extracting by rememberSaveable { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            extracting = true
            recognizeFromUri(context, uri) { text ->
                extracting = false
                if (text.isNullOrBlank()) {
                    Toast.makeText(context, "No text found in that image", Toast.LENGTH_SHORT).show()
                } else {
                    captured = text
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Read Text (OCR)", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            Text(
                "Point the camera at printed text, or pick a photo from your gallery. Extracted text is processed on-device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (captured.isBlank()) {
                Column {
                    SectionLabel("Live camera")
                    if (hasPerm) {
                        CameraPreviewBox(
                            textMode = true,
                            onCameraReady = {},
                            onScan = { text ->
                                if (captured.isBlank() && text.length > 2) captured = text
                            },
                            modifier = Modifier.fillMaxWidth().height(previewHeight(0.35f, 220.dp, 320.dp))
                        )
                    } else {
                        QsCard {
                            Text(
                                "Camera permission is needed for live text scanning.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            QsOutlinedButton(
                                text = "Grant camera access",
                                onClick = { permLauncher.launch(Manifest.permission.CAMERA) }
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Or pick a photo below — no camera needed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                SectionLabel("Extracted text")
                QsCard {
                    OutlinedTextField(
                        value = captured,
                        onValueChange = { captured = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                TextButton(onClick = { captured = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan again")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QsOutlinedButton(
                        text = "Copy text",
                        icon = Icons.Filled.ContentCopy,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("ocr", captured))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                    QsOutlinedButton(
                        text = "Share text",
                        icon = Icons.Filled.Share,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            runCatching {
                                context.startActivity(Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, captured)
                                    },
                                    "Share text"
                                ))
                            }
                        }
                    )
                }
                QsButton(
                    text = "Save to history",
                    icon = Icons.Filled.TextFields,
                    onClick = { onTextExtracted(captured) }
                )
            }

            if (extracting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(Modifier.height(6.dp))
            QsButton(
                text = "Pick image with text",
                icon = Icons.Filled.PhotoLibrary,
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun recognizeFromUri(context: Context, uri: Uri, onResult: (String?) -> Unit) {
    // Decode + downsample off the main thread to avoid OOM/ANR on large gallery photos.
    CoroutineScope(Dispatchers.IO).launch {
        val bitmap = try {
            decodeSampledBitmap(context, uri, 2048)
        } catch (e: Exception) {
            Log.e(TAG, "OCR decode failed", e)
            null
        }
        if (bitmap == null) {
            withContext(Dispatchers.Main) { onResult(null) }
            return@launch
        }
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { visionText ->
                    recognizer.close()
                    onResult(visionText.text.trim())
                }
                .addOnFailureListener { e ->
                    recognizer.close()
                    Log.e(TAG, "OCR failed", e)
                    onResult(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed", e)
            withContext(Dispatchers.Main) { onResult(null) }
        }
    }
}

/** Decodes a content Uri, sub-sampling so the longest edge stays under [maxEdge] px. */
private fun decodeSampledBitmap(context: Context, uri: Uri, maxEdge: Int): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    while (longest / sample > maxEdge) sample *= 2

    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}
