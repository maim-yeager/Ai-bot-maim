package com.example.candlescanner

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var decisionText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var reasonText: TextView
    private lateinit var countdownText: TextView
    private lateinit var accuracyBar: android.widget.ProgressBar

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val analyzer = CandleAnalyzer()
    private val engine = DecisionEngine()

    private val scope = CoroutineScope(Dispatchers.Main)

    private var tone: ToneGenerator? = null

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else showPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        decisionText = findViewById(R.id.decisionText)
        accuracyText = findViewById(R.id.accuracyText)
        reasonText = findViewById(R.id.reasonText)
        countdownText = findViewById(R.id/countdownText)
        accuracyBar = findViewById(R.id.accuracyBar)

        tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)

        showDisclaimerIfNeeded()

        reasonText.setOnClickListener {
            showThresholdDialog()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    private fun showPermissionDenied() {
        AlertDialog.Builder(this)
            .setTitle("Permission needed")
            .setMessage("Camera permission is required to scan charts")
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    private fun showDisclaimerIfNeeded() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        if (!prefs.getBoolean("disclaimer_shown", false)) {
            AlertDialog.Builder(this)
                .setTitle("Disclaimer")
                .setMessage(getString(R.string.disclaimer))
                .setPositiveButton("I Understand") { _, _ -> prefs.edit().putBoolean("disclaimer_shown", true).apply() }
                .setCancelable(false)
                .show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor, object : ImageAnalysis.Analyzer {
                override fun analyze(image: ImageProxy) {
                    // Run analysis on background thread
                    analyzer.analyze(image)?.let { snapshot ->
                        // evaluate on main thread
                        scope.launch {
                            processSnapshot(snapshot)
                        }
                    }
                    image.close()
                }
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview, analysis)
            } catch (e: Exception) {
                Log.e("MainActivity", "Camera bind failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processSnapshot(snapshot: CandleSnapshot) {
        // Start the 40-second decision pipeline
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val threshold = prefs.getInt("accuracy_threshold", 85)
        val pipeline = DecisionPipeline(engine, snapshot, threshold)

        pipeline.onUpdate = { stage, secondsLeft ->
            countdownText.text = "${secondsLeft}s"
            // optional: update small stage UI
        }

        pipeline.onResult = { result ->
            decisionText.text = result.decision.name
            accuracyText.text = "Accuracy: ${result.accuracy}%"
            reasonText.text = result.reason
            accuracyBar.progress = result.accuracy
                v.vibrate(100)
            }
        }

        pipeline.start()
    }

    private fun showThresholdDialog() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val current = prefs.getInt("accuracy_threshold", 85)
        val sb = android.widget.SeekBar(this)
        sb.max = 95
        sb.progress = current
        val tv = android.widget.TextView(this)
        tv.text = "Threshold: ${sb.progress}%"
        sb.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                tv.text = "Threshold: $progress%"
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        layout.addView(tv)
        layout.addView(sb)

        AlertDialog.Builder(this)
            .setTitle("Adjust accuracy threshold (Pro)")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putInt("accuracy_threshold", sb.progress).apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        tone?.release()
    }
}
