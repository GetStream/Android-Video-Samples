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

package io.getstream.android.samples.videocall

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.call.state.FlipCamera
import io.getstream.video.android.core.call.state.LeaveCall
import io.getstream.video.android.core.call.state.ToggleCamera
import io.getstream.video.android.core.call.state.ToggleMicrophone
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch

/**
 * This is the video call sample project follows the official video calling tutorial:
 *
 * https://getstream.io/video/sdk/android/tutorial/video-calling/
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
    val call = client.call("default", callId)

    setContent {
      // step4 - join a call.
      LaunchCallPermissions(call) {
        val result = call.join(create = true)
        result.onError {
          Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
        }
      }

      val scope = rememberCoroutineScope()

      VideoTheme {
        // step5 - render the pre-built call content.
        CallContent(
          modifier = Modifier.background(color = VideoTheme.colors.baseTertiary),
          call = call,
          onBackPressed = { finish() },
          onCallAction = { callAction ->
            when (callAction) {
              is ToggleCamera -> call.camera.setEnabled(callAction.isEnabled)

              is ToggleMicrophone -> call.microphone.setEnabled(callAction.isEnabled)

              is FlipCamera -> call.camera.flip()

              LeaveCall -> {
                scope.launch {
                  call.leave()
                  finish()
                }
              }

              else -> Unit
            }
          },
        )
      }
    }
  }
}
