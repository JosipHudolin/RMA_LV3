package rma.lv1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import rma.lv1.ui.theme.LV1Theme
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            LV1Theme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "main screen") {
                        composable("main screen") {
                            MainScreen(navController = navController)
                        }
                        composable("step_counter") {
                            StepCounter(navController = navController)
                        }
                    }
                    BackgroundImage(modifier = Modifier)
                }
            }
        }
    }

    @Composable
    fun MainScreen(navController: NavController) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment =
            Alignment.Center
        ) {
            BackgroundImage(modifier = Modifier.fillMaxSize())
            UserPreview()
            // Button to navigate to StepCounter
            Button(
                onClick = {
                    // Navigate to OtherScreen when button clicked
                    navController.navigate("step_counter")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(text = "Step Counter")
            }
        }
    }

    @Composable
    fun StepCounter(navController: NavController) {

        var steps by remember { mutableStateOf(0) }

        val sensorManager =
            (LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as
                    SensorManager)

        val sensor = remember {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        val db = Firebase.firestore

        DisposableEffect(Unit) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())

                    if (acceleration > 25) {
                        steps++
                        updateStepCount(steps)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundImage(modifier = Modifier.fillMaxSize())
            Column {
                Text(
                    text = "Step Count: $steps",
                    fontSize = 20.sp
                )
            }
            // Back button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text("User Info")
            }
        }
    }

    fun updateStepCount(stepCount: Int) {
        val db =
            FirebaseFirestore.getInstance()
        val docRef = db.collection("BMI")
            .document("JWXDu5WtQzdSKMnEeawt")

        docRef.update("Koraci", stepCount)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error updating step count: $e")
            }
    }

    @Composable
    fun UserPreview() {
        val name = "Miljenko"
        val visina = 1.91f
        val tezina = 1000f
        val bmi = tezina / (visina * visina)
        val formattedBmi = String.format("%.2f", bmi)
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pozdrav $name!",
                fontSize = 20.sp,
                lineHeight = 56.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Tvoj BMI je:",
                fontSize = 55.sp,
                lineHeight = 61.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = formattedBmi,
                fontSize = 70.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    @Composable
    fun BackgroundImage(modifier: Modifier) {

        Box(modifier) {
            Image(
                painter = painterResource(id = R.drawable.fitness),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.1F
            )
        }

    }
}


