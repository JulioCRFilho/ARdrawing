package com.example.ardrawing.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.ardrawing.R
import com.example.ardrawing.databinding.ActivityArdrawingBinding
import com.example.ardrawing.interactor.ARDrawingInterface
import com.example.ardrawing.utils.CameraPermissionHelper
import com.example.ardrawing.viewModel.ARDrawingViewModel
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARDrawingActivity : AppCompatActivity(), ARDrawingInterface {
    private lateinit var cameraPermissionHelper: CameraPermissionHelper
    private lateinit var binding: ActivityArdrawingBinding
    private val viewModel: ARDrawingViewModel by viewModels()
    lateinit var selectedRenderable: Renderable
    lateinit var arFragment: ArFragment
    var anchorNode: AnchorNode? = null
    var mSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ardrawing)
        binding.viewModel = viewModel
        viewModel.interactor = this
        cameraPermissionHelper = CameraPermissionHelper()
        requestPermission()
    }

    private fun initArCore() {
        if (cameraPermissionHelper.hasCameraPermission(this)) {
            arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
            mSession = arFragment.arSceneView.session
            arFragment.arSceneView.session?.config?.planeFindingMode =
                Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            arFragment.planeDiscoveryController.hide()
            arFragment.planeDiscoveryController.setInstructionView(null)
            arFragment.arSceneView.planeRenderer.isEnabled = false

            setArFragmentListener()
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
                    null -> verifyArCoreAPK()
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
            cameraPermissionHelper.requestCameraPermission(this)
            return
        } else {
            verifyArCoreAPK()
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

    private fun setArFragmentListener() {
        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                selectedRenderable =
                    ShapeFactory.makeSphere(0.01f, Vector3(0f, 0f, 0.0f), material)
                teste()
            }
            .exceptionally { t ->
                Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun teste() {
        arFragment.arSceneView.setOnTouchListener { _, _ ->
            if (arFragment.arSceneView.arFrame?.camera?.trackingState == TrackingState.TRACKING) {
                try {
                    Log.d("tatata", "clicando")

                    val anchor = mSession?.createAnchor(
                        arFragment.arSceneView.arFrame?.camera?.pose?.compose(
                            Pose.makeTranslation(0f, 0f, -1f)
                        )?.extractTranslation()
                    )

                    if (anchorNode == null) anchorNode = AnchorNode(anchor)

                    anchorNode?.setParent(arFragment.arSceneView.scene)
                    renderModel(anchorNode!!)

                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            }
            true
        }
    }

    private fun renderModel(node: AnchorNode) {
        val model = Node()
        model.setParent(node)
        model.renderable = selectedRenderable
        model.light = null
        model.worldPosition = Vector3(0f, 0f, -0.5f)
    }

    override fun startDrawing() {
        if (arFragment.arSceneView.arFrame?.camera?.trackingState == TrackingState.TRACKING) {
            try {
                Log.d("tatata", "clicando")

                val anchor = mSession?.createAnchor(
                    arFragment.arSceneView.arFrame?.camera?.pose?.compose(
                        Pose.makeTranslation(0f, 0f, -1f)
                    )?.extractTranslation()
                )

                if (anchorNode == null) anchorNode = AnchorNode(anchor)

                anchorNode?.setParent(arFragment.arSceneView.scene)
                renderModel(anchorNode!!)

            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
