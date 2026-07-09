package com.videosdkconnectionservice.activities

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.videosdkconnectionservice.utils.ApiClient
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.videosdkconnectionservice.R
import com.google.gson.annotations.SerializedName
import com.videosdkconnectionservice.viewmodel.ZoomSessionViewModel
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.io.Serializable
import io.github.cdimascio.dotenv.dotenv
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.getValue

data class JWTOptions(
    @SerializedName("sessionName") val sessionName: String,
    @SerializedName("role") val role: Int,
    @SerializedName("userIdentity") val userIdentity: String,
    @SerializedName("sessionkey") val sessionkey: String,
    @SerializedName("geo_regions") val geo_regions: String,
    @SerializedName("cloud_recording_option") val cloud_recording_option: Int,
    @SerializedName("cloud_recording_election") val cloud_recording_election: Int,
    @SerializedName("telemetry_tracking_id") val telemetry_tracking_id: String,
    @SerializedName("video_webrtc_mode") val video_webrtc_mode: Int,
    @SerializedName("audio_webrtc_mode") val audio_webrtc_mode: Int,
)
data class Signature(val signature: String)
data class Config(val sessionName: String, val userName: String, val password: String?, val jwt: String ): Serializable

class JoinSession : AppCompatActivity() {
    val context: Context = this
    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env"
    }

    private val endpointURL: String = dotenv["ENDPOINT_URL"]

    private val zoomSessionViewModel by viewModels<ZoomSessionViewModel>()
    private lateinit var sessionNameTextField: TextInputLayout
    private lateinit var usernameTextField: TextInputLayout
    private lateinit var passwordTextField: TextInputLayout
    private lateinit var jwtTokenTextField: TextInputLayout
    private lateinit var joinSessionButton: Button
    private lateinit var waitingProgressBar: android.widget.ProgressBar
    private lateinit var cancelScheduleBtn: Button
    private lateinit var sessionName: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var jwtToken: String
    private var recordAudioGranted: Boolean = false

    private val permissions: Array<String> = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
    )
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            permissionsMap.forEach { (permission, isGranted) ->
                when (permission) {
                    "android.permission.RECORD_AUDIO" -> recordAudioGranted = isGranted
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_join_session)

        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allPermissionsGranted) requestMultiplePermissionsLauncher.launch(permissions)

        sessionNameTextField = findViewById(R.id.sessionNameTextField)
        usernameTextField = findViewById(R.id.usernameTextField)
        passwordTextField = findViewById(R.id.passwordTextField)
        jwtTokenTextField = findViewById(R.id.jwtTokenTextField)
        joinSessionButton = findViewById(R.id.joinsessionBtn)
        waitingProgressBar = findViewById(R.id.waitingProgressBar)
        cancelScheduleBtn = findViewById(R.id.cancelScheduleBtn)
        sessionName = findViewById<TextInputEditText>(R.id.sessionNameTextEditField).text.toString()
        username = findViewById<TextInputEditText>(R.id.usernameTextEditField).text.toString()
        password = findViewById<TextInputEditText>(R.id.passwordTextEditField).text.toString()
        jwtToken = findViewById<TextInputEditText>(R.id.jwtTokenTextEditField).text.toString()

        sessionNameTextField.editText?.doOnTextChanged { sessionName : CharSequence?, _:Int, _:Int, _:Int ->
            this.sessionName = sessionName.toString()
        }
        usernameTextField.editText?.doOnTextChanged { username: CharSequence?, _:Int, _:Int, _:Int ->
            this.username = username.toString()
        }
        passwordTextField.editText?.doOnTextChanged { password: CharSequence?, _:Int, _:Int, _:Int ->
            this.password = password.toString()
        }
        jwtTokenTextField.editText?.doOnTextChanged { jwtToken: CharSequence?, _:Int, _:Int, _:Int ->
            this.jwtToken = jwtToken.toString()
        }

        joinSessionButton.setOnClickListener {
            val body = JWTOptions(
                sessionName = sessionName,
                role = 1,
                userIdentity = null.toString(),
                sessionkey = null.toString(),
                geo_regions = null.toString(),
                cloud_recording_option = 0,
                cloud_recording_election = 0,
                telemetry_tracking_id = "internal-dev5",
                video_webrtc_mode = 0,
                audio_webrtc_mode = 0
            )

            // Show scheduled UI state and start background keepalive
            android.widget.Toast.makeText(this, "Session Scheduled waiting for Push Notification", android.widget.Toast.LENGTH_LONG).show()
            // hide register button and show cancel in its place
            joinSessionButton.visibility = android.view.View.GONE
            cancelScheduleBtn.visibility = android.view.View.VISIBLE
            waitingProgressBar.visibility = android.view.View.VISIBLE
            // start a foreground service to allow background processing while screen locked
            androidx.core.content.ContextCompat.startForegroundService(this, android.content.Intent(this, com.videosdkconnectionservice.services.KeepAliveService::class.java))

            if (endpointURL.isEmpty()) {
                println("JWT from local " + jwtToken)
                val config = Config(sessionName, username, password, jwtToken)
                zoomSessionViewModel.initZoomSDK(config)
            } else {
                lifecycleScope.launch {
                    val response =
                        ApiClient.apiService.getJWT(sessionName, username, password, body)
                            .awaitResponse()
                    if (response.isSuccessful) {
                        val jwt = Gson().fromJson(response.body(), Signature::class.java)
                        val config = Config(sessionName, username, password, jwt.signature)
                        zoomSessionViewModel.initZoomSDK(config)
                    } else {
                        println("error")
                    }
                }
            }
        }

        cancelScheduleBtn.setOnClickListener {
            // Cancel scheduled session: clear fields, hide loader, stop keepalive
            sessionNameTextField.editText?.setText("")
            usernameTextField.editText?.setText("")
            passwordTextField.editText?.setText("")
            jwtTokenTextField.editText?.setText("")
            waitingProgressBar.visibility = android.view.View.GONE
            cancelScheduleBtn.visibility = android.view.View.GONE
            // restore register button visibility
            joinSessionButton.visibility = android.view.View.VISIBLE
            // stop foreground service if running
            stopService(android.content.Intent(this, com.videosdkconnectionservice.services.KeepAliveService::class.java))
        }
    }
}