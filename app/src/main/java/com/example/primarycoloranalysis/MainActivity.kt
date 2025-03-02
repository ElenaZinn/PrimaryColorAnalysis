package com.example.primarycoloranalysis

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.primarycoloranalysis.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentBitmap: Bitmap? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.uploadButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.imageView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentBitmap?.let { bitmap ->
                        val color = getColorAtPoint(event.x, event.y, bitmap)
                        updateColorInfo(color)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    currentBitmap = resource
                    binding.imageView.setImageBitmap(resource)
                    binding.placeholderImage.visibility = View.GONE
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun getColorAtPoint(x: Float, y: Float, bitmap: Bitmap): Int {
        // 转换触摸坐标为bitmap坐标
        val imageView = binding.imageView
        val bitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val matrix = Matrix()
        imageView.imageMatrix.invert(matrix)

        val points = floatArrayOf(x, y)
        matrix.mapPoints(points)

        val xBitmap = points[0].coerceIn(0f, bitmap.width - 1f).toInt()
        val yBitmap = points[1].coerceIn(0f, bitmap.height - 1f).toInt()

        return bitmap.getPixel(xBitmap, yBitmap)
    }

    private fun updateColorInfo(color: Int) {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val redPercent = (red / 255f * 100).roundToInt()
        val greenPercent = (green / 255f * 100).roundToInt()
        val bluePercent = (blue / 255f * 100).roundToInt()

        binding.colorPreview.setBackgroundColor(color)
        binding.colorInfo.text = """
            RGB: ($red, $green, $blue)
            红: $redPercent%
            绿: $greenPercent%
            蓝: $bluePercent%
        """.trimIndent()
    }
}
