package com.example.ardrawing.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.ardrawing.R
import com.example.ardrawing.utils.CameraPermissionHelper
import com.example.ardrawing.viewModel.ARDrawingViewModel
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.ArSceneView

class ARDrawingActivity : AppCompatActivity() {
    val viewModel: ARDrawingViewModel by viewModels()
    private lateinit var cameraPermissionHelper: CameraPermissionHelper
    lateinit var mSession: Session
    lateinit var arSceneView: ArSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ardrawing)
        initVars()
        requestPermission()
    }

    private fun initVars() {
        arSceneView = ArSceneView(this).findViewById(R.id.arFragmentView)
        mSession = Session(this)
        cameraPermissionHelper = CameraPermissionHelper()
    }

    override fun onResume() {
        super.onResume()
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, true)) {
                    ArCoreApk.InstallStatus.INSTALLED -> mSession = Session(this)
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> ArCoreApk.getInstance().requestInstall(this, true)
                    null -> return
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, "TODO: handle exception $e", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "TODO: handle exception ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun requestPermission() {
        if (!cameraPermissionHelper.hasCameraPermission(this)) {
            cameraPermissionHelper.requestCameraPermission(this);
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!cameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show()
            if (!cameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                cameraPermissionHelper.launchPermissionSettings(this);
            }
        }
    }
}
