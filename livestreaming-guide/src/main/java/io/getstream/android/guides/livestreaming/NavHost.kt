package io.getstream.android.guides.livestreaming

import android.content.Context
import android.media.AudioAttributes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.getstream.android.guides.livestreaming.screens.LivestreamScreen
import io.getstream.android.guides.livestreaming.screens.MainScreen
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.call.CallType
import io.getstream.video.android.core.notifications.NotificationConfig
import io.getstream.video.android.core.notifications.internal.service.CallServiceConfigRegistry
import io.getstream.video.android.core.notifications.internal.service.DefaultCallConfigurations
import io.getstream.video.android.model.User

@Composable
fun NavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screens.Main.route,
) {
    val context = LocalContext.current

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screens.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screens.Livestream.route, Screens.Livestream.args) {
            val isHost = Screens.Livestream.isHost(it)
            val userCredentials = if (isHost) UserCredentials.host else UserCredentials.viewer

            LivestreamScreen(
                navController = navController,
                callId = Screens.Livestream.getCallId(it),
                streamVideo = initStreamVideo(
                    context,
                    userCredentials
                ),
                isHost = isHost
            )
        }
    }
}

sealed class Screens(val route: String) {
    data object Main : Screens("main")

    data object Livestream : Screens("livestream/{call_id}/{is_host}") {
        private val argCallId = "call_id"
        private val argIsHost = "is_host"

        val args = listOf(
            navArgument(argCallId) { type = NavType.StringType },
            navArgument(argIsHost) { type = NavType.BoolType }
        )

        fun getCallId(backStackEntry: NavBackStackEntry): String {
            return backStackEntry.arguments?.getString(argCallId) ?: error("Call ID not found")
        }

        fun isHost(backStackEntry: NavBackStackEntry): Boolean {
            return backStackEntry.arguments?.getBoolean(argIsHost) ?: false
        }

        fun route(callId: String, isHost: Boolean): String {
            return "livestream/$callId/$isHost"
        }
    }
}

private fun initStreamVideo(context: Context, userCredentials: UserCredentials): StreamVideo {
    return StreamVideo.instanceOrNull() ?: StreamVideoBuilder(
        context = context,
        apiKey = "k436tyde94hj",
        user = User(id = userCredentials.id, name = userCredentials.name),
        token = userCredentials.token,
        notificationConfig = NotificationConfig(
            enableCallNotificationUpdates = false,
        ),
        callServiceConfigRegistry = CallServiceConfigRegistry().apply {
            if (userCredentials.isHost) {
                register(DefaultCallConfigurations.getLivestreamCallServiceConfig())
            } else {
                register(CallType.Livestream.name) {
                    DefaultCallConfigurations.livestreamGuestCall.copy(
                        runCallServiceInForeground = false,
                        audioUsage = AudioAttributes.USAGE_MEDIA,
                    )
                }
            }
        },
    ).build()
}
