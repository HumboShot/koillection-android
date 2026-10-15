package com.humboshot.koillection

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.humboshot.koillection.theme.KoillectionTheme
import com.humboshot.koillection.ui.common.ScreenshotMode

// The fingerprint prompt needs a FragmentActivity.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ScreenshotMode.enableFrom(intent)
        setContent {
            KoillectionTheme {
                KoillectionApp()
            }
        }
    }
}