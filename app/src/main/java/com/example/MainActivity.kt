package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.DjViewModel
import com.example.ui.DjViewModelFactory
import com.example.ui.PulsarDjMain
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch application dependencies
        val djApp = application as DjApplication
        val viewModel = ViewModelProvider(
            this,
            DjViewModelFactory(djApp, djApp.repository)
        )[DjViewModel::class.java]

        setContent {
            MyApplicationTheme {
                PulsarDjMain(viewModel = viewModel)
            }
        }
    }
}
