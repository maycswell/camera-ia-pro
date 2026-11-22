
package br.com.cameraia.pro

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var statusText: TextView
    private lateinit var captureButton: Button
    private lateinit var flashButton: Button

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private var lensFacing =
        CameraSelector.LENS_FACING_BACK

    private var flashEnabled = false
    private var currentMode = "TERRA"

    companion object {
        private const val REQUEST_CAMERA = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        }
    }

    private fun createInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 13, 24))
            setPadding(16, 12, 16, 16)
        }

        val title = TextView(this).apply {
            text = "CÂMERA IA PRO"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 8)
        }

        previewView = PreviewView(this).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        statusText = TextView(this).apply {
            text = "Modo TERRA"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 12)
        }

        val earthButton = createButton("🌎 TERRA") {
            setMode("TERRA")
        }

        val spaceButton = createButton("🌌 ESPAÇO") {
            setMode("ESPAÇO")
        }

        flashButton = createButton("FLASH: OFF") {
            toggleFlash()
        }

        val flipButton = createButton("INVERTER CÂMERA") {
            switchCamera()
        }

        val galleryButton = createButton("GALERIA") {
            openGallery()
        }

        captureButton = Button(this).apply {
            text = "● TIRAR FOTO"
            textSize = 19f
            setTextColor(Color.BLACK)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(69, 214, 197)
                )
            )
            setOnClickListener {
                takePhoto()
            }
        }

        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(earthButton, weightedParams())
            addView(spaceButton, weightedParams())
        }

        val toolsRow = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(flashButton)
            addView(flipButton)
            addView(galleryButton)
        }

        root.addView(title)

        root.addView(
            previewView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(statusText)
        root.addView(modeRow)
        root.addView(toolsRow)
        root.addView(captureButton)

        setContentView(root)
    }

    private fun weightedParams() =
        LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

    private fun createButton(
        label: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener {
                action()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {

        val future =
            ProcessCameraProvider.getInstance(this)

        future.addListener({

            val provider = future.get()

            val preview = Preview.Builder().build()

            preview.setSurfaceProvider(
                previewView.surfaceProvider
            )

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(
                    ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                )
                .setFlashMode(
                    if (flashEnabled)
                        ImageCapture.FLASH_MODE_ON
                    else
                        ImageCapture.FLASH_MODE_OFF
                )
                .build()

            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {

                provider.unbindAll()

                camera = provider.bindToLifecycle(
                    this,
                    selector,
                    preview,
                    imageCapture
                )

                statusText.text =
                    "Modo $currentMode • Câmera pronta"

            } catch (error: Exception) {

                Toast.makeText(
                    this,
                    "Erro ao iniciar a câmera",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {

        val capture = imageCapture ?: return

        val filename = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date())

        val values = ContentValues().apply {

            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "CameraIAPro_$filename.jpg"
            )

            put(
                MediaStore.MediaColumns.MIME_TYPE,
                "image/jpeg"
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/CameraIAPro"
                )
            }
        }

        val output = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        captureButton.isEnabled = false

        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(this),

            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    result: ImageCapture.OutputFileResults
                ) {

                    captureButton.isEnabled = true

                    Toast.makeText(
                        this@MainActivity,
                        "Foto salva com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    captureButton.isEnabled = true

                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao salvar fotografia",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun toggleFlash() {

        flashEnabled = !flashEnabled

        flashButton.text =
            if (flashEnabled) "FLASH: ON"
            else "FLASH: OFF"

        imageCapture?.flashMode =
            if (flashEnabled)
                ImageCapture.FLASH_MODE_ON
            else
                ImageCapture.FLASH_MODE_OFF
    }

    private fun switchCamera() {

        lensFacing =
            if (lensFacing ==
                CameraSelector.LENS_FACING_BACK
            ) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }

        startCamera()
    }

    private fun setMode(mode: String) {

        currentMode = mode

        statusText.text =
            "Modo $mode • Câmera pronta"

        Toast.makeText(
            this,
            "Modo $mode selecionado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openGallery() {

        val intent = Intent(
            Intent.ACTION_VIEW
        ).apply {

            setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
        }

        try {
            startActivity(intent)
        } catch (error: Exception) {
            Toast.makeText(
                this,
                "Galeria não encontrada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == REQUEST_CAMERA) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissão da câmera necessária",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
