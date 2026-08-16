package com.dhanuk.quickscanpro.ui.design

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dhanuk.quickscanpro.analyzer.BarcodeAnalyzer

private const val TAG = "CameraPreviewBox"

@Composable
fun CameraPreviewBox(
    onScan: (String) -> Unit,
    onCameraReady: (Camera?) -> Unit,
    modifier: Modifier = Modifier,
    textMode: Boolean = false,
    cornerRadius: Dp = 22.dp
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val currentOnScan by rememberUpdatedState(onScan)
    val analyzer = remember(textMode) {
        if (textMode) {
            com.dhanuk.quickscanpro.analyzer.TextAnalyzer { currentOnScan(it) }
        } else {
            BarcodeAnalyzer { currentOnScan(it) }
        }
    }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val currentOnCameraReady by rememberUpdatedState(onCameraReady)

    DisposableEffect(lifecycleOwner, analyzer) {
        var bound = false
        var previewUseCase: Preview? = null
        var analysisUseCase: ImageAnalysis? = null
        val future = ProcessCameraProvider.getInstance(context)

        val bind = { provider: ProcessCameraProvider ->
            try {
                provider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }
                previewUseCase = preview
                analysisUseCase = analysis
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                bound = true
                currentOnCameraReady(camera)
            } catch (e: Exception) {
                Log.e(TAG, "bind failed", e)
            }
        }

        val listener = Runnable {
            val provider = try {
                future.get()
            } catch (e: Exception) {
                Log.e(TAG, "camera provider failed", e)
                return@Runnable
            }
            bind(provider)
        }
        future.addListener(listener, executor)

        // If another camera consumer stole the binding while this screen was in
        // the background, re-own it when the screen comes back to the front.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    if (future.isDone && previewUseCase != null) {
                        val provider = future.get()
                        if (!provider.isBound(analysisUseCase!!)) bind(provider)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "rebind check failed", e)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                when (analyzer) {
                    is BarcodeAnalyzer -> analyzer.close()
                    is com.dhanuk.quickscanpro.analyzer.TextAnalyzer -> analyzer.close()
                }
                if (bound && previewUseCase != null && analysisUseCase != null) {
                    val provider = try {
                        if (future.isDone) future.get() else null
                    } catch (_: Exception) {
                        null
                    }
                    provider?.unbind(previewUseCase!!, analysisUseCase!!)
                    bound = false
                }
                currentOnCameraReady(null)
            } catch (e: Exception) {
                Log.e(TAG, "unbind failed", e)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.inverseSurface)
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        ScanOverlay(Modifier.fillMaxSize())
    }
}

@Composable
fun ScanOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanline")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanline"
    ).value

    val accent = MaterialTheme.colorScheme.primary

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val pad = 36.dp.toPx()
        val stroke = 4.dp.toPx()
        val arm = 44.dp.toPx()
        val r = stroke
        val left = pad
        val top = pad
        val right = size.width - pad
        val bottom = size.height - pad

        fun hBar(x: Float, y: Float) = drawRoundRect(
            color = accent,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(arm, stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
        )
        fun vBar(x: Float, y: Float) = drawRoundRect(
            color = accent,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(stroke, arm),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
        )

        // Top-left corner
        hBar(left, top)
        vBar(left, top)
        // Top-right corner
        hBar(right - arm, top)
        vBar(right - stroke, top)
        // Bottom-left corner
        vBar(left, bottom - arm)
        hBar(left, bottom - stroke)
        // Bottom-right corner
        vBar(right - stroke, bottom - arm)
        hBar(right - arm, bottom - stroke)

        // Animated scan line
        val lineY = top + (bottom - top - 3.dp.toPx()) * progress
        drawRoundRect(
            color = accent.copy(alpha = 0.85f),
            topLeft = androidx.compose.ui.geometry.Offset(left, lineY),
            size = androidx.compose.ui.geometry.Size(right - left, 3.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    }
}
