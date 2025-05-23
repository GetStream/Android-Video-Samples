package io.getstream.android.guides.livestreaming.screens

import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.getstream.android.video.generated.models.BackstageSettingsRequest
import io.getstream.android.video.generated.models.CallRecording
import io.getstream.android.video.generated.models.CallSettingsRequest
import io.getstream.android.video.generated.models.MemberRequest
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.compose.ui.components.video.VideoRenderer
import io.getstream.video.android.compose.ui.components.video.VideoScalingType
import io.getstream.video.android.compose.ui.components.video.config.videoRenderConfig
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.ParticipantState
import io.getstream.video.android.core.RealtimeConnection
import io.getstream.video.android.core.RealtimeConnection.Failed
import io.getstream.video.android.core.StreamVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun LivestreamScreen(navController: NavController, callId: String, streamVideo: StreamVideo, isHost: Boolean) {
    val call = streamVideo.call("livestream", callId)
    var isLoading by remember { mutableStateOf(true) }

    if (isHost) {
        LaunchCallPermissions(call = call, onAllPermissionsGranted = {
            call.create(
                members = listOf(
                    MemberRequest(userId = "live-host", role = "host"),
                ),
                settings = CallSettingsRequest(
                    backstage = BackstageSettingsRequest(
                        enabled = true,
                        joinAheadTimeSeconds = 120,
                    ),
                ),
                startsAt = OffsetDateTime.now().plusMinutes(3),
            )

            withContext(Dispatchers.IO) { call.join() }
            isLoading = false
        })
    } else {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { call.join() }
            isLoading = false
        }
    }

    LivestreamScreenContent(call, isLoading, isHost)

    BackHandler {
        call.leave()
        navController.popBackStack()
    }
}

@Composable
fun LivestreamScreenContent(call: Call, isJoinInProgress: Boolean, isHost: Boolean) {
    val isCallInBackstage by call.state.backstage.collectAsStateWithLifecycle()
    val endedAt by call.state.endedAt.collectAsStateWithLifecycle()

    Box(contentAlignment = Alignment.Center) {
        ConnectionMonitor(call) {
            if (isJoinInProgress) {
                LoadingContent()
            } else if (endedAt != null) {
                CallEndedContent(call)
            } else if (isCallInBackstage) {
                Backstage(call, isHost)
            } else {
                CallLiveContent(call, isHost)
            }
        }
    }
}

@Composable
private fun ConnectionMonitor(
    call: Call,
    contents: @Composable () -> Unit,
) {
    val connection by call.state.connection.collectAsStateWithLifecycle()
    var prevConnection by remember { mutableStateOf<RealtimeConnection?>(null) }

    LaunchedEffect(connection) {
        if (connection != prevConnection) {
            prevConnection = connection
        }
    }

    val shouldShowMessage = connection is RealtimeConnection.Reconnecting ||
            connection is Failed ||
            (connection is RealtimeConnection.Disconnected && prevConnection is RealtimeConnection.Reconnecting)


    if (shouldShowMessage) {
        ConnectionMessage(
            text = when (connection) {
                is RealtimeConnection.Reconnecting -> "Reconnecting, please wait"
                is RealtimeConnection.Disconnected -> "You are disconnected"
                is Failed -> "Cannot join livestream. Try again later"
                else -> "A connection error occurred"
            }
        )
    } else {
        contents()
    }
}

@Composable
private fun ConnectionMessage(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text, style = VideoTheme.typography.subtitleM)
    }
}

@Composable
private fun LoadingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = VideoTheme.colors.brandSecondary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Joining", style = VideoTheme.typography.bodyM)
    }
}

@Composable
private fun CallEndedContent(call: Call) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Livestream ended", style = VideoTheme.typography.subtitleM)
        Spacer(Modifier.height(24.dp))
        Recordings(call)
    }
}

@Composable
private fun Recordings(call: Call) {
    var recordings by remember { mutableStateOf(emptyList<CallRecording>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        call.listRecordings()
            .onSuccess { recordings = it.recordings }
            .onError { recordings = emptyList<CallRecording>() }
    }

    if (recordings.isEmpty()) {
        Text("No recordings available", style = VideoTheme.typography.subtitleS)
    } else {
        Text("Recordings available", style = VideoTheme.typography.subtitleS)
        Spacer(Modifier.height(8.dp))

        recordings.forEach {
            Text(
                text = it.filename,
                style = VideoTheme.typography.bodyM,
                modifier = Modifier.clickable {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, it.url.toUri()))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening recording: $e")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Backstage(call: Call, isHost: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val startsAt by call.state.startsAt.collectAsStateWithLifecycle()
        val formattedStartsAt = startsAt?.format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))

        if (formattedStartsAt != null) {
            Text("Livestream will start at $formattedStartsAt", style = VideoTheme.typography.subtitleM)
        } else {
            Text("Livestream will start soon", style = VideoTheme.typography.subtitleM)
        }

        val waitingCount by call.state.session.map {
            it?.participants?.count { it.role != "host" }
        }.collectAsStateWithLifecycle(null)

        waitingCount?.let {
            Spacer(Modifier.height(16.dp))
            Text("$it users waiting", style = VideoTheme.typography.bodyS)
        }

        if (isHost) {
            val localParticipant by call.state.localParticipant.collectAsStateWithLifecycle()
            val track = localParticipant?.video?.collectAsStateWithLifecycle()?.value

            Spacer(Modifier.height(8.dp))
            CallControls(call)
            Spacer(Modifier.height(16.dp))
            Text("Camera preview", style = VideoTheme.typography.subtitleS)
            Spacer(Modifier.height(16.dp))
            VideoFeed(call, track, isHost = true)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VideoFeed(
    call: Call,
    videoTrack: ParticipantState.Video?,
    isHost: Boolean = false,
    modifier: Modifier = Modifier
) {
    val videoFallbackContent = @Composable {
        Text("No video available", style = VideoTheme.typography.subtitleS)
    }

    VideoRenderer(
        modifier = modifier.then(
            if (isHost) {
                Modifier.height(320.dp)
            } else {
                Modifier.fillMaxSize()
            }
        ),
        call = call,
        video = videoTrack,
        videoRendererConfig = videoRenderConfig {
            this.videoScalingType = if (isHost) VideoScalingType.SCALE_ASPECT_FIT else VideoScalingType.SCALE_ASPECT_FILL
            this.fallbackContent = { videoFallbackContent }
        },
    )
}

@Composable
private fun CallControls(call: Call) {
    val scope = rememberCoroutineScope()
    val backstage by call.state.backstage.collectAsStateWithLifecycle()
    val isCameraEnabled by call.camera.isEnabled.collectAsStateWithLifecycle()
    val isMicrophoneEnabled by call.microphone.isEnabled.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CallActionButton(
            text = if (backstage) "Go Live" else "Stop Live",
            onClick = {
                scope.launch {
                    if (backstage) {
                        call.goLive()
                    } else {
                        call.stopLive()
                    }
                }
            }
        )

        CallActionButton(
            text = "End Call",
            onClick = {
                scope.launch {
                    call.end()
                }
            }
        )

        ToggleCameraAction(isCameraEnabled = isCameraEnabled) {
            call.camera.setEnabled(it.isEnabled)
        }

        ToggleMicrophoneAction(isMicrophoneEnabled = isMicrophoneEnabled) {
            call.microphone.setEnabled(it.isEnabled)
        }
    }
}

@Composable
fun CallActionButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            contentColor = VideoTheme.colors.brandPrimary,
            containerColor = VideoTheme.colors.brandPrimary,
        ),
        onClick = onClick,
    ) {
        Text(text = text, color = VideoTheme.colors.basePrimary)
    }
}

@Composable
private fun CallLiveContent(call: Call, isHost: Boolean) {
    val members by call.state.members.collectAsStateWithLifecycle()
    val hostIds = members.filter { it.role == "host" }.map { it.user.id }
    val participants by call.state.participants.collectAsStateWithLifecycle()
    val host = participants.firstOrNull { it.userId.value in hostIds }
    val track = if (isHost) {
        val localParticipant by call.state.localParticipant.collectAsStateWithLifecycle()
        localParticipant?.video?.collectAsStateWithLifecycle()?.value
    } else {
        host?.video?.collectAsStateWithLifecycle()?.value
    }
    val totalParticipants by call.state.totalParticipants.collectAsStateWithLifecycle()
    val viewers = max(0, totalParticipants - 1)
    val duration by call.state.duration.collectAsStateWithLifecycle()

    Column {
        Box(modifier = Modifier.weight(1f)) {
            VideoFeed(call, track)
            InfoLabel(modifier = Modifier.align(Alignment.TopStart), "${duration ?: "0m 0s"}")
            InfoLabel(modifier = Modifier.align(Alignment.TopEnd), "Viewers: $viewers")
        }

        if (isHost) {
            CallControls(call)
        }
    }
}

@Composable
private fun InfoLabel(modifier: Modifier = Modifier, value: String) {
    Text(
        modifier = modifier.then(
            Modifier
                .widthIn(min = 100.dp)
                .padding(all = 16.dp)
                .background(
                    color = VideoTheme.colors.brandPrimary.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(all = 8.dp)
        ),
        text = value,
        textAlign = TextAlign.Center,
        color = Color.White,
    )
}

private const val TAG = "LivestreamingGuide"
