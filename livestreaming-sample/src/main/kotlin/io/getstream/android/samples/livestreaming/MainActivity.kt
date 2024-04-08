/*
 *  The MIT License (MIT)
 *
 *  Copyright 2024 Stream.IO, Inc. All Rights Reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package io.getstream.android.samples.livestreaming

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.video.VideoRenderer
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.RealtimeConnection
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch

/**
 * This is the livestreaming sample project follows the official livestreaming tutorial:
 *
 * https://getstream.io/video/sdk/android/tutorial/livestreaming/
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val userId = "stream"
    val callId = "stream_call"
    val userToken = StreamVideo.devToken(userId)

    // step1 - create a user.
    val user = User(
      id = userId, // any string
      name = "Tutorial", // name and image are used in the UI
    )

    // step2 - initialize StreamVideo. For a production app we recommend adding
    // the client to your Application class or di module.
    val client = StreamVideoBuilder(
      context = applicationContext,
      apiKey = BuildConfig.STREAM_API_KEY,
      geo = GEO.GlobalEdgeNetwork,
      user = user,
      token = userToken,
    ).build()

    // step3 - create a call.
    val call = client.call("livestream", callId)

    setContent {
      // step4 - join a call.
      LaunchCallPermissions(call) {
        val result = call.join(create = true)
        result.onError {
          Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
        }
      }

      VideoTheme {
        val connection by call.state.connection.collectAsState()
        val totalParticipants by call.state.totalParticipants.collectAsState()
        val backstage by call.state.backstage.collectAsState()
        val localParticipant by call.state.localParticipant.collectAsState()
        val video = localParticipant?.video?.collectAsState()?.value
        val duration by call.state.duration.collectAsState()

        androidx.compose.material.Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(VideoTheme.colors.baseTertiary)
            .padding(6.dp),
          contentColor = VideoTheme.colors.baseTertiary,
          backgroundColor = VideoTheme.colors.baseTertiary,
          topBar = {
            if (connection == RealtimeConnection.Connected) {
              if (!backstage) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                ) {
                  Text(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .background(
                        color = VideoTheme.colors.brandPrimary,
                        shape = RoundedCornerShape(6.dp),
                      )
                      .padding(horizontal = 12.dp, vertical = 4.dp),
                    text = "Live $totalParticipants",
                    color = Color.White,
                  )

                  Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Live for $duration",
                    color = VideoTheme.colors.iconDefault,
                  )
                }
              }
            }
          },
          bottomBar = {
            androidx.compose.material.Button(
              colors = ButtonDefaults.buttonColors(
                contentColor = VideoTheme.colors.brandPrimary,
                backgroundColor = VideoTheme.colors.brandPrimary,
              ),
              onClick = {
                lifecycleScope.launch {
                  if (backstage) call.goLive() else call.stopLive()
                }
              },
            ) {
              Text(
                text = if (backstage) "Go Live" else "Stop Broadcast",
                color = Color.White,
              )
            }
          },
        ) {
          VideoRenderer(
            modifier = Modifier
              .fillMaxSize()
              .padding(it)
              .clip(RoundedCornerShape(6.dp)),
            call = call,
            video = video,
            videoFallbackContent = {
              Text(text = "Video rendering failed")
            },
          )
        }
      }
    }
  }
}
