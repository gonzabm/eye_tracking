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

package com.google.mediapipe.examples.facelandmarker.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.facelandmarker.databinding.FaceBlendshapesResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import java.io.BufferedInputStream
import java.lang.Thread.sleep
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import kotlin.concurrent.thread
import kotlin.math.sqrt
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class FaceBlendshapesResultAdapter : RecyclerView.Adapter<FaceBlendshapesResultAdapter.ViewHolder>() {
    companion object {
        private val RIGHT_EYE_OUTLINE: List<Int> = listOf(263, 387, 385, 362, 380, 373)
        private val LEFT_EYE_OUTLINE: List<Int> = listOf(33, 160, 158, 133, 153, 144)
        private val RIGHT_EYE_IRIS: List<Int> = listOf(362, 473, 263)
        private val LEFT_EYE_IRIS: List<Int> = listOf(33, 468, 133)

        private val closeEyeThreshold: Float  = 0.1f
        private val lookLeftThreshold: Float  = 0.5f
        private val lookRightThreshold: Float = -0.5f

        private var rEAR: Float = 1f
        private var lEAR: Float = 1f
        private var eyeDIR: Float = 0f
    }

    init {
        thread {
            sendToESP32("brakes", 1)

            var timer1: Long? = null
            var timer2: Long? = null
            var brakesActivated = true

            while (true) {
                sleep(1000)
                val leftEyeBlink = lEAR
                val rightEyeBlink = rEAR
                val eyeDirection = eyeDIR

                if (leftEyeBlink < closeEyeThreshold && rightEyeBlink < closeEyeThreshold) {
                    timer2 = null
                    if (timer1 == null) {
                        timer1 = System.currentTimeMillis()
                        continue
                    }
                    if (System.currentTimeMillis() - timer1 > 2000) {
                        sendToESP32("brakes", if (brakesActivated) 0 else 1)
                        sendToESP32("lMotor", 0)
                        sendToESP32("rMotor", 0)
                        brakesActivated = !brakesActivated
                        timer1 = null
                        continue
                    }
                    else {
                        continue
                    }
                }
                else {
                    timer1 = null
                }


                if (leftEyeBlink < closeEyeThreshold) {
                    timer1 = null
                    if (timer2 == null) {
                        timer2 = System.currentTimeMillis()
                        continue
                    }
                    if (System.currentTimeMillis() - timer2 > 300) {
                        sendToESP32("lMotor", 1)
                        sendToESP32("rMotor", 1)
                        timer2 = null
                        continue
                    }
                    else {
                        continue
                    }
                }
                else {
                    timer2 = null
                }

                if (eyeDirection > lookLeftThreshold) {
                    sendToESP32("lMotor", 1)
                    sendToESP32("rMotor", 0)
                }
                else if (eyeDirection < lookRightThreshold) {
                    sendToESP32("lMotor", 0)
                    sendToESP32("rMotor", 1)
                }
                else {
                    sendToESP32("lMotor", 0)
                    sendToESP32("rMotor", 0)
                }


            }
        }
    }

    private var categories: MutableList<Category?> = MutableList(3) { null }

    fun updateResults(faceLandmarkerResult: FaceLandmarkerResult?) {
        if (faceLandmarkerResult != null && faceLandmarkerResult.faceLandmarks().isNotEmpty()) {
            val faceLandmarks = faceLandmarkerResult.faceLandmarks()[0]
            rEAR = ear(getCoords(faceLandmarks, RIGHT_EYE_OUTLINE))
            lEAR = ear(getCoords(faceLandmarks, LEFT_EYE_OUTLINE))
            eyeDIR = dir(getCoords(faceLandmarks, LEFT_EYE_IRIS), getCoords(faceLandmarks, RIGHT_EYE_IRIS) )
        }
        else {
            rEAR = 1f
            lEAR = 1f
            eyeDIR = 0f
        }
    }

    private fun sendToESP32(device: String, state: Int) {
        //val ip = "192.168.203.21"
        val ip = "192.168.203.17"

        val url = URL("http://${ip}/update?device=${device}&state=$state")
        try {
            val inputStream = url.openConnection().getInputStream()
            inputStream.bufferedReader().readText()
            inputStream.bufferedReader().close()
            inputStream.close()
        } catch (e: java.net.ConnectException) {
            e.printStackTrace()
        }

    }

    private fun getCoords(faceLandmark: List<NormalizedLandmark>, region: List<Int>): List<NormalizedLandmark> {
        val coords: ArrayList<NormalizedLandmark> = ArrayList()
        for (index in region) {
            coords.add(faceLandmark[index])
        }
        return coords
    }

    private fun ear(coords: List<NormalizedLandmark>): Float {
        fun distance(a: NormalizedLandmark, b: NormalizedLandmark): Float {
            val aux1 = a.x() - b.x()
            val aux2 = a.y() - b.y()
            return sqrt(aux1*aux1 + aux2*aux2)
        }
        val d1: Float = distance(coords[1], coords[5])
        val d2: Float = distance(coords[2], coords[4])
        val d3: Float = distance(coords[0], coords[3])
        return (d1 + d2) / (2 * d3)
    }

    private fun dir(coords1: List<NormalizedLandmark>, coords2: List<NormalizedLandmark>): Float {
        fun distance(a: NormalizedLandmark, b: NormalizedLandmark): Float {
            val aux1 = a.x() - b.x()
            val aux2 = a.y() - b.y()
            return sqrt(aux1*aux1 + aux2*aux2)
        }
        var d1: Float
        var d2: Float
        var lDIR: Float
        var rDIR: Float

        d1 = distance(coords1[0], coords1[1])
        d2 = distance(coords1[0], coords1[2])
        lDIR = 2 * d1 / d2 - 1

        d1 = distance(coords2[0], coords2[1])
        d2 = distance(coords2[0], coords2[2])
        rDIR = 2 * d1 / d2 - 1

        if (lDIR > 0f && rDIR > 0f) {
            return -1f * maxOf(lDIR, rDIR)
        }
        else if (lDIR < 0f && rDIR < 0f) {
            return -1f * minOf(lDIR, rDIR)
        }
        else {
            return -1f * (lDIR + rDIR) / 2f
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FaceBlendshapesResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.bind("rEAR", rEAR)
        }
        else if(position == 1) {
            holder.bind("lEAR", lEAR)
        }
        else if(position == 2) {
            holder.bind("eyeDIR", eyeDIR)
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: FaceBlendshapesResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: String?, score: Float?) {
            with(binding) {
                tvLabel.text = label ?: "--"
                tvScore.text = if (score != null) String.format("%.2f", score) else "--"
            }
        }
    }


}
