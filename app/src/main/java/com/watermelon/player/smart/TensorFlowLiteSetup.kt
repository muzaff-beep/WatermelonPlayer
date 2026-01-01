package com.watermelon.player.smart

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer

class TensorFlowLiteSetup(private val context: Context) {
    fun loadModel(modelPath: String): Interpreter {
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, modelPath)
        return Interpreter(model)
    }

    fun preprocessImage(tensorImage: TensorImage): TensorBuffer {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        val processedImage = imageProcessor.process(tensorImage)
        return processedImage.buffer
    }

    fun postprocessOutput(outputBuffer: TensorBuffer, labels: List<String>): Map<String, Float> {
        val tensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0f, 255f))
            .build()
        val processedOutput = tensorProcessor.process(outputBuffer)
        val labeler = TensorLabel(processedOutput, labels)
        return labeler.mapWithFloatValue
    }
}
