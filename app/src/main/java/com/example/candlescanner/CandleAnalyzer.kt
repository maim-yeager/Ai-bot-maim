package com.example.candlescanner

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Analyze camera frames and extract simplified candle shapes into a snapshot.
 * This is a heuristic-based detector and intended for offline on-device analysis.
 */
class CandleAnalyzer {
    private val recentCandles = ArrayDeque<Candle>(30)

    fun analyze(imageProxy: ImageProxy): CandleSnapshot? {
        val image = imageProxy.image ?: return null
        val rotation = imageProxy.imageInfo.rotationDegrees
        try {
            val bitmap = toBitmap(image)
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

            // Basic quality checks
            if (!isImageGood(mat)) {
                return CandleSnapshot.empty("Chart clear na – scan possible na")
            }

            // Simple edge detection + vertical components search
            Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
            val edges = Mat()
            Imgproc.Canny(mat, edges, 50.0, 150.0)

            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            val candles = ArrayList<Candle>()
            for (c in contours) {
                val r = Imgproc.boundingRect(c)
                // Filter expected vertical slender shapes representing candlesticks
                val aspect = r.height.toDouble() / max(1.0, r.width.toDouble())
                if (aspect > 1.5 && r.height > 20 && r.width > 4) {
                    // sample brightness in rect center to guess color
                    val centerVal = mat.get(r.y + r.height/2, r.x + r.width/2)[0]
                    val bullish = centerVal > 127
                    val body = r.height.toDouble() * 0.6
                    val wickTop = (r.y).toDouble()
                    val wickBottom = (r.y + r.height).toDouble()

                    val candle = Candle(
                        x = r.x + r.width / 2,
                        bodySize = body.toFloat(),
                        wickTop = wickTop.toFloat(),
                        wickBottom = wickBottom.toFloat(),
                        bullish = bullish,
                        area = r.area().toDouble()
                    )
                    candles.add(candle)
                }
            }

            candles.sortBy { it.x }
            if (candles.isNotEmpty()) {
                // Add last candle to recent list
                for (c in candles) {
                    if (recentCandles.isEmpty() || recentCandles.last().x != c.x) {
                        recentCandles.addLast(c)
                        if (recentCandles.size > 30) recentCandles.removeFirst()
                    }
                }
            }

            return CandleSnapshot.fromList(recentCandles.toList())
        } catch (e: Exception) {
            Log.e("CandleAnalyzer", "analyze failed", e)
            return null
        }
    }

    private fun isImageGood(gray: Mat): Boolean {
        // blur detection (variance of Laplacian)
        val lap = Mat()
        Imgproc.Laplacian(gray, lap, CvType.CV_64F)
        val mean = Core.mean(lap).`val`[0]
        val variance = mean
        if (variance < 5.0) return false

        // brightness
        val avg = Core.mean(gray).`val`[0]
        if (avg < 40) return false

        return true
    }

    private fun toBitmap(image: Image): Bitmap {
        // Convert YUV_420_888 to NV21 then to Bitmap using YuvImage
        val planes = image.planes
        val width = image.width
        val height = image.height
        val ySize = planes[0].buffer.remaining()
        val uSize = planes[1].buffer.remaining()
        val vSize = planes[2].buffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)

        planes[0].buffer.get(nv21, 0, ySize)

        val chromaRowStride = planes[1].rowStride
        val chromaPixelStride = planes[1].pixelStride

        val pos = ySize
        if (chromaPixelStride == 1 && chromaRowStride == width / 2) {
            // likely NV21 already
            planes[2].buffer.get(nv21, pos, vSize)
            planes[1].buffer.get(nv21, pos + vSize, uSize)
        } else {
            // Repack U and V
            var offset = ySize
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer
            val rowStride = planes[2].rowStride
            val pixelStride = planes[2].pixelStride
            for (row in 0 until height / 2) {
                for (col in 0 until width / 2) {
                    val vuIndex = row * rowStride + col * pixelStride
                    val v = vBuffer.get(vuIndex)
                    val u = uBuffer.get(vuIndex)
                    nv21[offset++] = v
                    nv21[offset++] = u
                }
            }
        }

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 70, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}

data class Candle(
    val x: Int,
    val bodySize: Float,
    val wickTop: Float,
    val wickBottom: Float,
    val bullish: Boolean,
    val area: Double
)

data class CandleSnapshot(
    val candles: List<Candle>,
    val message: String? = null
) {
    companion object {
        fun fromList(list: List<Candle>) = CandleSnapshot(list, null)
        fun empty(msg: String) = CandleSnapshot(emptyList(), msg)
    }
}
