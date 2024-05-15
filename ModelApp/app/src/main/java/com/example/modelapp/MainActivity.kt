package com.example.modelapp

import android.R.attr
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modelapp.ml.MobilenetV110224Quant
import com.example.modelapp.ui.theme.ModelAppTheme
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.nio.ByteBuffer


//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModelAppTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                    //Greeting("Android")
                }
            }
        }
    }


    companion object {

        init {
            System.loadLibrary("modelapp")
        }
    }

}
external fun stringFromJNI(): String
external fun processImage(originalBitmap: Bitmap, scaledBitmap: Bitmap)
external fun findbest(arr: FloatArray): String
external fun Button1stringFromJNI(): String
external fun Button2stringFromJNI(): String

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    var result: String by remember { mutableStateOf("") }
    var imageLabel_get by remember { mutableStateOf("") }


    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringFromJNI(),
            fontSize = 17.sp)
        Spacer(modifier = Modifier.height(20.dp))


        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text(text = Button1stringFromJNI(),
                fontSize = 17.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap.value = MediaStore.Images
                    .Media.getBitmap(context.contentResolver,it)

            } else {
                val source = ImageDecoder
                    .createSource(context.contentResolver,it)
                bitmap.value = ImageDecoder.decodeBitmap(source)
            }

            bitmap.value?.let {  btm ->
                Image(bitmap = btm.asImageBitmap(),
                    contentDescription =null,
                    modifier = Modifier.size(400.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {

//            val delegate = GpuDelegate()
//            val options = Interpreter.Options().addDelegate(delegate).setNumThreads(4).setUseNNAPI(true)
//            val model = MobilenetV110224Quant.newInstance(context, options)
            val model = MobilenetV110224Quant.newInstance(context)


            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
            if(bitmap.value==null){
                return@Button
            }

            var bitmap2: Bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
            processImage(bitmap.value?.copy(Bitmap.Config.ARGB_8888, true)!!, bitmap2)

            inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmap2).buffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            result=findbest(outputFeature0.floatArray)
            var  stringList = mutableListOf<String>()
            var cnt=0
            BufferedReader(context.assets.open("labels.txt").reader()).useLines { lines ->
                lines.forEach {
                    stringList.add(it)
                }
            }
            result=stringList[result.toInt()]
            model.close()
            //delegate.close()
        }) {
            Text(
                text = Button2stringFromJNI(),
                fontSize = 17.sp)
        }
        Spacer(modifier = Modifier.height(23.dp))

        Text(
            text = result,
            fontSize = 30.sp,
            color = Color.Blue,
            modifier = Modifier.background(Color.LightGray)
        )
    }
}

//fun findbest2(arr: FloatArray): String {
//    var max = arr[0]
//    var maxIndex = 0
//    for (i in 1 until arr.size) {
//        if (arr[i] > max) {
//            maxIndex = i
//            max = arr[i]
//        }
//    }
//    return maxIndex.toString()
//}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ModelAppTheme {
        Greeting("Android")
    }
}