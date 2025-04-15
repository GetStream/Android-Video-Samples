package io.getstream.android.guides.livestreaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.core.StreamVideo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VideoTheme {
              NavHost()
            }
        }
    }
}

data class UserCredentials(
    val id: String,
    val name: String,
    val token: String,
    val isHost: Boolean,
) {
    companion object {
        val host = UserCredentials(
            id = "livestream-host",
            name = "Host",
            token = StreamVideo.devToken("live-host"),
            isHost = true,
        )

        val viewer = UserCredentials(
            id = "livestream-viewer",
            name = "Viewer",
            token = StreamVideo.devToken("live-viewer"),
            isHost = false,
        )
    }
}