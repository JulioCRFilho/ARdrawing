package com.example.ardrawing.viewModel

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.ardrawing.interactor.ARDrawingInterface

class ARDrawingViewModel : ViewModel() {
    lateinit var interactor: ARDrawingInterface

    fun startDrawing(view: View) {
        interactor.startDrawing()
    }
}