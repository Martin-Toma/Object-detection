package com.experiment2.myapplication

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.experiment2.myapplication.databinding.ActivityMainBinding
import com.experiment2.myapplication.ml.LiteModelEfficientdetLite0DetectionMetadata1
import com.experiment2.myapplication.ml.LiteModelEfficientdetLite3DetectionMetadata1
import com.experiment2.myapplication.ml.LiteModelEfficientdetLite4DetectionMetadata2
import com.experiment2.myapplication.ml.LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N
//import com.example.mlbasickotlinapp.ml.LiteModelQatMobilenetV2Retinanet2561
import org.tensorflow.lite.support.image.TensorImage

class Detector(contextn : Context) {

    private var threshold = 0.4
    private var modelNum : Int = 0
    private lateinit var model0 : LiteModelEfficientdetLite0DetectionMetadata1
    private lateinit var model1 : LiteModelEfficientdetLite3DetectionMetadata1
    private lateinit var model2 : LiteModelEfficientdetLite4DetectionMetadata2
    private lateinit var model3 : LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N
    //private var model4 : LiteModelQatMobilenetV2Retinanet2561
    private val context : Context

    private var values : MutableList<Double> = ArrayList()

  //  private var justInferenceTime : Long = 0

    init {
        //model4 = LiteModelQatMobilenetV2Retinanet2561.newInstance(contextn)
        model0 = LiteModelEfficientdetLite0DetectionMetadata1.newInstance(contextn)
        context = contextn
    }

    fun setModel(type : String){
        Log.d("CHANGE", type)
        try{
            model0.close()
        }
        catch (_: Exception){
            Log.d("CHANGE", "Didn't close model 0")
        }
        try{
            model1.close()
        }
        catch (e: Exception){
            Log.d("CHANGE", "Didn't close model 1")
        }
        try{
            model2.close()
        }
        catch (e: Exception){
            Log.d("CHANGE", "Didn't close model 2")
        }
        try{
            model3.close()
        }
        catch (e: Exception){
            Log.d("CHANGE", "Didn't close model 3")
        }

        values.clear()

        when(type){
            "Efficientdet0" -> {
                modelNum = 0
                model0 = LiteModelEfficientdetLite0DetectionMetadata1.newInstance(context)
            }
            "Efficientdet3" -> {
                modelNum = 1
                model1 = LiteModelEfficientdetLite3DetectionMetadata1.newInstance(context)
            }
            "Efficientdet4" -> {
                modelNum = 2
                model2 = LiteModelEfficientdetLite4DetectionMetadata2.newInstance(context)
            }
            "Mobile Object Localizer V1" ->  {
                modelNum = 3
                model3 = LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N.newInstance(context)
            }

            else -> {
                modelNum = 0
            }
        }
    }

    fun process(imageT : TensorImage, binding: ActivityMainBinding, validGot: Int, lastTimestampGot: Long) : Output {
        var valid = validGot
        var lastTimestamp = lastTimestampGot
        val bitmap = binding.previewView.bitmap
        var bitW = imageT.width
        var bitH = imageT.height
        /*
        if (bitmap != null){
            bitW = bitmap.width
            bitH = bitmap.height
        }*/
        when(modelNum){
            1 -> {
                val timeBefore = SystemClock.elapsedRealtimeNanos()
                val outputs = model1.process(imageT)
                val timeInfer: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.inferenceTime.setText("Inference "+timeInfer.toString()+"ms")
                values.add(timeInfer)
                Log.d("Time", "${values}")
                var sum : Double = 0.0
                if (values.lastIndex > 18){
                    val iter = values.iterator()
                    while(iter.hasNext()){
                       sum += iter.next()
                    }

                    values.removeAt(0)
                }
                sum = sum / 20
                binding.avgInferenceTime.setText("Avg.infer. "+sum.toString()+"ms")
                val outIter = outputs.detectionResultList.iterator()
                while (binding.parentLayout.childCount > 1) {
                    binding.parentLayout.removeViewAt(1)
                }
                while (outIter.hasNext() ) {
                    val i = outIter.next()
                    if (i.scoreAsFloat > threshold){
                        val element = Draw(
                            context = context,
                            rect = i.locationAsRectF,
                            text = i.categoryAsString,
                            bitW,
                            bitH,
                            binding.previewView.width,
                            binding.previewView.height
                        )
                        Log.d("OUTLable", "${i.categoryAsString}}")
                        binding.parentLayout.addView(element)


                        if (valid != 0){
                            var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                            time_difference = time_difference / 1000000
                            binding.time.setText("Draw "+time_difference.toString()+"ms")
                            Log.d("Time", "${time_difference}")
                        }

                        valid = 1
                        lastTimestamp =  SystemClock.elapsedRealtimeNanos()
                    }
                    else {
                        valid = 0
                    }
                }
                val timeWhole: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.wholeTime.setText("1 cycle "+timeWhole.toString()+"ms")

                val out = Output(valid, lastTimestamp)
                return out
            }
            2 -> {
                val timeBefore = SystemClock.elapsedRealtimeNanos()
                val outputs = model2.process(imageT)
                val timeInfer: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.inferenceTime.setText("Inference "+timeInfer.toString()+"ms")
                values.add(timeInfer)
                Log.d("Time", "${values}")
                var sum : Double = 0.0
                if (values.lastIndex > 18){
                    val iter = values.iterator()
                    while(iter.hasNext()){
                        sum += iter.next()
                    }

                    values.removeAt(0)
                }
                sum = sum / 20
                binding.avgInferenceTime.setText("Avg.infer. "+sum.toString()+"ms")
                val outIter = outputs.detectionResultList.iterator()
                while (binding.parentLayout.childCount > 1) {
                    binding.parentLayout.removeViewAt(1)
                }
                while (outIter.hasNext() ) {
                    val i = outIter.next()
                    if (i.scoreAsFloat > threshold){
                        val element = Draw(
                            context = context,
                            rect = i.locationAsRectF,
                            text = i.categoryAsString,
                            bitW,
                            bitH,
                            binding.previewView.width,
                            binding.previewView.height
                        )
                        Log.d("OUTLable", "${i.categoryAsString}}")
                        binding.parentLayout.addView(element)


                        if (valid != 0){
                            var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                            time_difference = time_difference / 1000000
                            binding.time.setText("Draw "+time_difference.toString()+"ms")
                            Log.d("Time", "${time_difference}")
                        }

                        valid = 1
                        lastTimestamp =  SystemClock.elapsedRealtimeNanos()
                    }
                    else {
                        valid = 0
                    }
                }
                val timeWhole: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.wholeTime.setText("1 cycle "+timeWhole.toString()+"ms")

                val out = Output(valid, lastTimestamp)
                return out
            }
            3 ->  {
                val timeBefore = SystemClock.elapsedRealtimeNanos()
                val outputs = model3.process(imageT)
                val timeInfer: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.inferenceTime.setText("Inference "+timeInfer.toString()+"ms")
                values.add(timeInfer)
                Log.d("Time", "${values}")
                var sum : Double = 0.0
                if (values.lastIndex > 18){
                    val iter = values.iterator()
                    while(iter.hasNext()){
                        sum += iter.next()
                    }

                    values.removeAt(0)
                }
                sum = sum / 20
                binding.avgInferenceTime.setText("Avg.infer. "+sum.toString()+"ms")
                val outIter = outputs.detectionResultList.iterator()
                while (binding.parentLayout.childCount > 1) {
                    binding.parentLayout.removeViewAt(1)
                }
                while (outIter.hasNext() ) {
                    val i = outIter.next()
                    if (i.scoreAsFloat > threshold){
                        val element = Draw(
                            context = context,
                            rect = i.locationAsRectF,
                            text = i.categoryAsString,
                            bitW,
                            bitH,
                            binding.previewView.width,
                            binding.previewView.height
                        )
                        Log.d("OUTLable", "${i.categoryAsString}}")
                        binding.parentLayout.addView(element)


                        if (valid != 0){
                            var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                            time_difference = time_difference / 1000000
                            binding.time.setText("Draw "+time_difference.toString()+"ms")
                            Log.d("Time", "${time_difference}")
                        }

                        valid = 1
                        lastTimestamp =  SystemClock.elapsedRealtimeNanos()
                    }
                    else {
                        valid = 0
                    }
                }
                val timeWhole: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.wholeTime.setText("1 cycle "+timeWhole.toString()+"ms")

                val out = Output(valid, lastTimestamp)
                return out
            }
            else -> {
                val timeBefore = SystemClock.elapsedRealtimeNanos()
                val outputs = model0.process(imageT)
                val timeInfer: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.inferenceTime.setText("Inference "+timeInfer.toString()+"ms")
                values.add(timeInfer)
                Log.d("Time", "${values}")
                var sum : Double = 0.0
                if (values.lastIndex > 18){
                    val iter = values.iterator()
                    while(iter.hasNext()){
                        sum += iter.next()
                    }

                    values.removeAt(0)
                }
                sum = sum / 20
                binding.avgInferenceTime.setText("Avg.infer. "+sum.toString()+"ms")
                val outIter = outputs.detectionResultList.iterator()
                while (binding.parentLayout.childCount > 1) {
                    binding.parentLayout.removeViewAt(1)
                }
                while (outIter.hasNext() ) {
                    val i = outIter.next()
                    if (i.scoreAsFloat > threshold){
                        val element = Draw(
                            context = context,
                            rect = i.locationAsRectF,
                            text = i.categoryAsString,
                            bitW,
                            bitH,
                            binding.previewView.width,
                            binding.previewView.height
                        )
                        Log.d("OUTLable", "${i.categoryAsString}}")
                        binding.parentLayout.addView(element)


                        if (valid != 0){
                            var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                            time_difference = time_difference / 1000000
                            binding.time.setText("Draw "+time_difference.toString()+"ms")
                            Log.d("Time", "${time_difference}")
                        }

                        valid = 1
                        lastTimestamp =  SystemClock.elapsedRealtimeNanos()
                    }
                    else {
                        valid = 0
                    }
                }
                val timeWhole: Double = ((SystemClock.elapsedRealtimeNanos() - timeBefore).toDouble()) / 1000000
                binding.wholeTime.setText("1 cycle "+timeWhole.toString()+"ms")

                val out = Output(valid, lastTimestamp)
                return out
            }
        }
    }

    fun init_model(){
        model0 = LiteModelEfficientdetLite0DetectionMetadata1.newInstance(context)
        model1 = LiteModelEfficientdetLite3DetectionMetadata1.newInstance(context)
        model2 = LiteModelEfficientdetLite4DetectionMetadata2.newInstance(context)
        model3 = LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N.newInstance(context)
    }
}