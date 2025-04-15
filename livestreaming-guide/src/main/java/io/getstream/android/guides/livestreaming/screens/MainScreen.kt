package io.getstream.android.guides.livestreaming.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.getstream.android.guides.livestreaming.Screens

@Composable
fun MainScreen(navController: NavController) {
    val callId = "Tk8abhUdsV325675_aa"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CallActionButton(
          modifier = Modifier
            .width(300.dp)
            .height(64.dp),
          text = "Host",
          onClick = {
            navController.navigate(Screens.Livestream.route(callId, isHost = true))
          },
        )

      CallActionButton(
        modifier = Modifier
          .width(300.dp)
          .height(64.dp),
        text = "Viewer",
        onClick = {
          navController.navigate(Screens.Livestream.route(callId, isHost = false))
        },
      )
    }
}