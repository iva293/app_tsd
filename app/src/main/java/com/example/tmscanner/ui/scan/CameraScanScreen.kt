@file:Suppress("OPT_IN_USAGE", "DEPRECATION")

package com.example.tmscanner.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.tmscanner.viewmodel.MainViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.ui.Alignment
import androidx.constraintlayout.compose.*
import androidx.camera.core.ExperimentalGetImage
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import android.util.Rational
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import android.view.WindowManager
import java.util.concurrent.Executors


// ============================
// CAMERA SCAN SCREEN (MAIN COMPOSABLE)
// ============================
@Composable
fun CameraScanScreen(
    nav: NavController,
    vm: MainViewModel
) {

    // ============================
    // CONTEXT / LIFECYCLE
    // ============================
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val view = LocalView.current

    // ============================
    // KEEP SCREEN ON
    // ============================
    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ============================
    // CAMERA PERMISSION
    // ============================
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ============================
    // CAMERA + ML KIT STATE
    // ============================
    var camera by remember { mutableStateOf<Camera?>(null) }

    val scanner = remember { BarcodeScanning.getClient() }
    val isProcessing = remember { AtomicBoolean(false) }

    // bounding box QR
    var qrBox by remember { mutableStateOf<Rect?>(null) }

    // frame dimensions (to scale the frame)
    var imageProxyWidth by remember { mutableFloatStateOf(1f) }
    var imageProxyHeight by remember { mutableFloatStateOf(1f) }

    var lastScannedValue by remember { mutableStateOf("") }

    var lastAnalyzeTime by remember { mutableLongStateOf(0L) }

    // ============================
    // FLASH EFFECT (UI FEEDBACK)
    // ============================
    var flashVisible by remember { mutableStateOf(false) }
    var flashLock by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (flashVisible) 0.45f else 0f,
        animationSpec = tween(90),
        label = "flash"



    )

    fun triggerFlash() {
        if (flashLock) return

        flashLock = true
        flashVisible = true

        scope.launch {
            delay(90)
            flashVisible = false
            delay(120)
            flashLock = false
        }
    }

    // ============================
    // VIBRATION FEEDBACK
    // ============================
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else vibrator.vibrate(70)
    }

    // ============================
    // AUTO FOCUS LOOP
    // ============================
    LaunchedEffect(camera) {
        val cam = camera ?: return@LaunchedEffect
        val control = cam.cameraControl

        while (camera != null) {
            delay(2500)

            if (qrBox != null) {
                continue
            }

            val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
            val point = factory.createPoint(0.5f, 0.5f)

            val action = FocusMeteringAction.Builder(
                point,
                FocusMeteringAction.FLAG_AF
            )
                .setAutoCancelDuration(2, TimeUnit.SECONDS)
                .build()

            control.startFocusAndMetering(action)
        }
    }

    // ============================
    // UI ROOT
    // ============================
    Box(Modifier.fillMaxSize()) {

        ConstraintLayout(Modifier.fillMaxSize()) {

            val (topBar, cameraCard, listTitle, list) = createRefs()

            // TOP BAR (BACK BUTTON)
            Row(
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = WindowInsets.statusBars
                            .asPaddingValues()
                            .calculateTopPadding() + 8.dp
                    ),
                    onClick = { nav.popBackStack() }
                ) {
                    Text("Назад")
                }
            }


            // ============================
            // CAMERA PREVIEW BLOCK
            // ============================
            Card(
                modifier = Modifier.constrainAs(cameraCard) {
                    top.linkTo(topBar.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)

                    height = Dimension.value(340.dp)
                    width = Dimension.fillToConstraints
                }
            ) {

                Box(Modifier.fillMaxSize()) {

                    // CAMERA VIEW (CameraX)
                    val cameraExecutor = remember {
                        Executors.newSingleThreadExecutor()
                    }
                    AndroidView(
                        factory = { ctx ->

                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FIT_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }

                            val cameraProviderFuture =
                                ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({

                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build()

                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                    )
                                    .setTargetResolution(android.util.Size(1280, 720))
                                    .build()

                                cameraProvider.unbindAll()

                                val viewPort = ViewPort.Builder(
                                    Rational(previewView.width, previewView.height),
                                    previewView.display.rotation
                                ).build()

                                val useCaseGroup = UseCaseGroup.Builder()
                                    .setViewPort(viewPort)
                                    .addUseCase(preview)
                                    .addUseCase(analysis)
                                    .build()

                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    useCaseGroup
                                )

                                // torch + zoom
                                cam.cameraControl.enableTorch(true)
                                cam.cameraControl.setZoomRatio(2f)

                                preview.surfaceProvider = previewView.surfaceProvider
                                camera = cam

                                // ML KIT BARCODE ANALYSIS
                                @ExperimentalGetImage
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                                    imageProxyWidth = imageProxy.width.toFloat()
                                    imageProxyHeight = imageProxy.height.toFloat()

                                    if (!isProcessing.compareAndSet(false, true)) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val mediaImage = imageProxy.image ?: run {
                                        isProcessing.set(false)
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val rotation = imageProxy.imageInfo.rotationDegrees

                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        rotation
                                    )

                                    val now = System.currentTimeMillis()

                                    if (now - lastAnalyzeTime < 300) {
                                        isProcessing.set(false)
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    lastAnalyzeTime = now

                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->

                                            val barcode = barcodes.firstOrNull()

                                            if (barcode == null) {
                                                qrBox = null
                                                return@addOnSuccessListener
                                            }

                                            qrBox = barcode.boundingBox

                                            val value = barcode.rawValue
                                                ?: return@addOnSuccessListener

                                            if (value == lastScannedValue) {
                                                return@addOnSuccessListener
                                            }

                                            lastScannedValue = value

                                            vm.handleScan(value)
                                            vibrate()
                                            triggerFlash()

                                            scope.launch {
                                                delay(2000)

                                                lastScannedValue = ""
                                                qrBox = null
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                            isProcessing.set(false)
                                        }
                                }

                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // ============================
                    // QR BOUNDING BOX OVERLAY
                    // ============================
                    qrBox?.let { rect ->

                        Canvas(Modifier.fillMaxSize()) {

                            val scaleX = size.width / imageProxyHeight
                            val scaleY = size.height / imageProxyWidth

                            val left = rect.left * scaleX
                            val top = rect.top * scaleY
                            val width = rect.width() * scaleX
                            val height = rect.height() * scaleY

                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top),
                                size = Size(width, height),
                                style = Stroke(width = 6f)
                            )
                        }
                    }

                    // ============================
                    // FLASH OVERLAY
                    // ============================
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Green.copy(alpha = alpha))
                    )
                }
            }

            // ============================
            // SCANNED ITEMS LIST TITLE
            // ============================
            Text(
                "Список:",
                modifier = Modifier.constrainAs(listTitle) {
                    top.linkTo(cameraCard.bottom, margin = 12.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
            )

            // ============================
            // SCANNED ITEMS LIST
            // ============================
            LazyColumn(
                modifier = Modifier.constrainAs(list) {
                    top.linkTo(listTitle.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    bottom.linkTo(parent.bottom)

                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
            ) {
                items(vm.list) {
                    Card(Modifier.fillMaxWidth().padding(4.dp)) {
                        Text(it, Modifier.padding(12.dp))
                    }
                }
            }
        }

        // ============================
        // PERMISSION OVERLAY
        // ============================
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Text(
                    "Нет доступа к камере",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}