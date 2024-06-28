package com.google.mediapipe.examples.facelandmarker

/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    // Set the landmark data on this class private variables, so that they are painted on the next draw() call
    fun setResults(faceLandmarkerResults: FaceLandmarkerResult, imageHeight: Int, imageWidth: Int, ) {
        this.results = faceLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if(results == null || results!!.faceLandmarks().isEmpty()) {
            clear()
            return
        }

        results?.let { faceLandmarkerResult ->
            val faceLandmarks = faceLandmarkerResult.faceLandmarks()[0]

            // Draw  face landmark points
            for(normalizedLandmark in faceLandmarks) {
                canvas.drawPoint(normalizedLandmark.x() * imageWidth * scaleFactor, normalizedLandmark.y() * imageHeight * scaleFactor, pointPaint)
            }
            // Draw face landmark lines
            FaceLandmarker.FACE_LANDMARKS_CONNECTORS.forEach {
                canvas.drawLine(
                    faceLandmarks[it.start()].x() * imageWidth * scaleFactor,
                    faceLandmarks[it.start()].y() * imageHeight * scaleFactor,
                    faceLandmarks[it.end()].x() * imageWidth * scaleFactor,
                    faceLandmarks[it.end()].y() * imageHeight * scaleFactor,
                    linePaint)
            }
        }
    }

    // Set painting parameters
    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = 8F
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 8F
        pointPaint.style = Paint.Style.FILL
    }

    // Reset painting parameters
    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

}
