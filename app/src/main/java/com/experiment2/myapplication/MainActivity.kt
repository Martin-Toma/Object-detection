package com.experiment2.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException

import android.os.SystemClock
import android.provider.DocumentsContract.Root
import android.provider.DocumentsContract.getRootId
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.R
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.experiment2.myapplication.databinding.ActivityMainBinding
//import com.example.mlbasickotlinapp.ml.LiteModelYoloCppe5LiteFp161
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.createFromFileAndOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector.createFromFileAndOptions
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private final lateinit var binding: ActivityMainBinding

    private lateinit var objectDetector: ObjectDetector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var lastTimestamp: Long = 0
    private var valid: Int = 0
    val contextM = this
    private lateinit var detector : Detector
    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamAfterPermissionGranted()
            } else {
                Snackbar.make(
                    binding.root,
                    "The camera permission is required",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    //private lateinit var model : LiteModelEfficientdetLite3DetectionMetadata1//LiteModelYoloCppe5LiteFp161//LiteModelEfficientdetLite3DetectionMetadata1 //LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //model = LiteModelYoloCppe5LiteFp161.newInstance(this)

        //model = LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N.newInstance(this)
        detector = Detector(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        val modelsSelection = arrayOf("Efficientdet0", "Efficientdet3", "Efficientdet4", "Mobile Object Localizer V1")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, modelsSelection)
        binding.models.adapter = adapter

        binding.models.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Toast.makeText(contextM, "Selected ${modelsSelection[p2]}", Toast.LENGTH_LONG).show()
                //lateinit var selected : ModelType
                //changeModel(modelsSelection[p2])
                detector.setModel(modelsSelection[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Toast.makeText(contextM, "Nothing selected", Toast.LENGTH_LONG).show()
            }
        }

        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
    }

    fun startCamAfterPermissionGranted(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider = cameraProvider) //Bind camera provider

        }, ContextCompat.getMainExecutor(this))

        /*val localModel = LocalModel.Builder()
            //.setAssetFilePath("lite-model_object_detection_mobile_object_localizer_v1_1_metadata_2_N.tflite")//
            //.setAssetFilePath("lite-model_efficientdet_lite3_detection_metadata_1.tflite")
            //.setAssetFilePath("object_labeler.tflite")
            .setAssetFilePath("lite-model_yolo-cppe5-lite_fp16_1.tflite")
            //.setAssetFilePath("lite-model_efficientdet_lite4_detection_metadata_2.tflite")
            .build()
        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)//STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.1f)
            .setMaxPerObjectLabelCount(3)
            .build()
*/
        Log.d("OUTLable", "objectdetector created")
        //objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    @SuppressLint("UnsafeOptInUsageError", "SetTextI18n")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(224,224))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            Log.d("Time", "HERE")
            try{
                //model = LiteModelObjectDetectionMobileObjectLocalizerV11Metadata2N.newInstance(this)
/*
            val model = LiteModelYoloV5TfliteTfliteModel1.newInstance(this)

            val bitmap = binding.previewView.bitmap
            if (bitmap != null) {
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, false)

// Normalize the pixel values to the range [0, 1].
                val inputBuffer = ByteBuffer.allocateDirect(4 * 320 * 320 * 3).apply {
                    order(ByteOrder.nativeOrder())
                    val intValues = IntArray(320 * 320)
                    resizedBitmap.getPixels(intValues, 0, 320, 0, 0, 320, 320)
                    for (pixelValue in intValues) {
                        putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
                        putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
                        putFloat(((pixelValue and 0xFF) / 255.0f))
                    }
                    rewind()
                }

// Create a TensorBuffer from the ByteBuffer.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(inputBuffer)

// Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                Log.d("Position", "Seems good")
// Releases model resources if no longer used.
                model.close()
            }
*/
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                val image = imageProxy.image

                // Creates inputs for reference.
                val imageT = TensorImage.fromBitmap(binding.previewView.bitmap)
                // Runs model inference and gets result.
                //val outputs = model.process(imageT)//detector.process(imageT) //
                //val detectionResult = outputs.detectionResultList.get(0)
// Gets result from DetectionResult.
                //val location = detectionResult.locationAsRectF;
                //val category = detectionResult.categoryAsString;
                //val score = detectionResult.scoreAsFloat;
                //Log.d("Time", "location: ${location} category: ${category} score: ${score}")
                val setVals = detector.process(imageT, binding, valid, lastTimestamp)

                /*
                // draw the results
                val outIter = outputs.detectionResultList.iterator()
                while (outIter.hasNext() ) {
                    val i = outIter.next()
                    if (i.scoreAsFloat > 0.2){
                        while (binding.parentLayout.childCount > 1) {
                            binding.parentLayout.removeViewAt(1)
                        }
                        val element = Draw(
                            context = this,
                            rect = i.locationAsRectF,
                            text = i.categoryAsString,
                            imageT.width,
                            imageT.height,
                            binding.previewView.width,
                            binding.previewView.height
                        )
                        Log.d("OUTLable", "${i.categoryAsString}}")
                        binding.parentLayout.addView(element)


                        if (valid != 0){
                            var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                            time_difference = time_difference / 1000000
                            binding.time.setText(time_difference.toString()+"ms")
                            Log.d("Time", "${time_difference}")
                        }

                        valid = 1
                        lastTimestamp =  SystemClock.elapsedRealtimeNanos()
                    }
                    else {
                        valid = 0
                    }
                }*/
                //model.close()
            } catch (e: Exception) {
                Log.e("Error", "An error occurred: ${e.message}")
            }
            imageProxy.close()
            /*
            if (image != null) {
                Log.d("Position", "In process")
                val processImage = InputImage.fromMediaImage(image, rotationDegrees)
                //binding.passedPic.setImageBitmap(binding.previewView.bitmap)
                objectDetector
                    .process(processImage)
                    .addOnSuccessListener { objects ->
                        Log.d("Position", "On success")
                        for (i in objects) {

                            if (binding.parentLayout.childCount > 1) {
                                binding.parentLayout.removeViewAt(1)
                            }
                            Log.d("Position", "${i.boundingBox}")
                            val element = Draw(
                                context = this,
                                rect = i.boundingBox,
                                text = i.labels.firstOrNull()?.text ?: "Undefined"
                            )
                            Log.d("OUTLable", "${i.labels.firstOrNull()?.text}}")
                            binding.parentLayout.addView(element)
                        }
                    }
                    .addOnFailureListener {
                        Log.d("Position", "On failure")
                    }
                    .addOnCompleteListener{
                        imageProxy.close()
                    }
                if (valid != 0){
                    var time_difference: Double = (SystemClock.elapsedRealtimeNanos() - lastTimestamp).toDouble()
                    time_difference = time_difference / 1000000
                    binding.time.setText(time_difference.toString()+"ms")
                    Log.d("Time", "${time_difference}")
                }
                valid = 1
                lastTimestamp =  SystemClock.elapsedRealtimeNanos()
            }
            else {
                valid = 0
            }*/
        }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)

    }

    fun changeModel(type : String){

    }

}