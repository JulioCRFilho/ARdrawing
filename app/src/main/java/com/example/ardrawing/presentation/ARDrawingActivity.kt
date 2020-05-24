package com.example.ardrawing.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.ardrawing.R
import com.example.ardrawing.databinding.ActivityArdrawingBinding
import com.example.ardrawing.interactor.ARDrawingInterface
import com.example.ardrawing.utils.CameraPermissionHelper
import com.example.ardrawing.viewModel.ARDrawingViewModel
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARDrawingActivity : AppCompatActivity(), ARDrawingInterface {
    private lateinit var cameraPermissionHelper: CameraPermissionHelper
    private lateinit var binding: ActivityArdrawingBinding
    private val viewModel: ARDrawingViewModel by viewModels()
    lateinit var selectedRenderable: Renderable
    lateinit var arFragment: ArFragment
    lateinit var node: Node
    var mSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ardrawing)
        binding.viewModel = viewModel
        viewModel.interactor = this
        initVars()
        requestPermission()
    }

    private fun initVars() {
        cameraPermissionHelper = CameraPermissionHelper()
    }

    private fun initArCore() {
        if (cameraPermissionHelper.hasCameraPermission(this)) {
            arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
            mSession = Session(this)
            selectRenderable()
        } else {
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        verifyArCoreAPK()
    }

    private fun verifyArCoreAPK() {
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, true)) {
                    ArCoreApk.InstallStatus.INSTALLED -> initArCore()
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> ArCoreApk.getInstance()
                        .requestInstall(this, true)
                    null -> onResume()
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
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
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!cameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                cameraPermissionHelper.launchPermissionSettings(this);
            }
        } else {
            verifyArCoreAPK()
        }
    }

    fun selectRenderable() {
        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                selectedRenderable =
                    ShapeFactory.makeSphere(0.1f, Vector3(0f, 0.15f, 0.0f), material)
            }
            .exceptionally { t ->
                Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
                null
            }

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()
            val node = AnchorNode(anchor)
            node.setParent(arFragment.arSceneView.scene)

            renderModel(node)
        }
    }

    override fun startDrawing() {
        arFragment.setOnSessionInitializationListener {
            val anchor = it.createAnchor(Pose.IDENTITY)
            val node = AnchorNode(anchor)
            node.setParent(arFragment.arSceneView.scene)

            renderModel(node)
        }
    }

    private fun renderModel(node: Node) {
        val model = TransformableNode(arFragment.transformationSystem)
        model.setParent(node)
        model.renderable = selectedRenderable
        model.select()
    }
}
