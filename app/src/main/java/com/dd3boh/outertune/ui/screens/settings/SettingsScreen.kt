package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val uriHandler = LocalUriHandler.current
    var isBetaFunEnabled by remember { mutableStateOf(false) }
    val backgroundImages = listOf(

        R.drawable.cardbg1,
        R.drawable.cardbg2,
        R.drawable.cardbg3,
        R.drawable.cardbg4,
        R.drawable.cardbg5,
        R.drawable.cardbg6,
        R.drawable.cardbg7,
        R.drawable.cardbg8,
        R.drawable.cardbg9,
        R.drawable.cardbgb,
        R.drawable.cardbgc,
        R.drawable.cardbgd,
        R.drawable.cardbge,
        R.drawable.cardbgf,
        R.drawable.cardbgg,
        R.drawable.cardbgh,
        R.drawable.cardbgj,
        R.drawable.cardbgi,
        R.drawable.cardbgk,
        R.drawable.cardbgm,





        )

    var currentImageIndex by remember { mutableIntStateOf((0..backgroundImages.lastIndex).random()) }






    fun changeBackgroundImage() {
        currentImageIndex = (currentImageIndex + 1) % backgroundImages.size
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color = Color.Transparent)
                .clickable { changeBackgroundImage() } // Cambiar a la siguiente imagen al hacer clic


        ) {
            Image(
                painter = painterResource(id = backgroundImages[currentImageIndex]),
                contentDescription = "Imagen de fondo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                        // Aplica un efecto de desenfoque al fondo : .blur( 1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.launcher_monochrome),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding( 1.dp, 20.dp, 12.dp),

                )

            Text(
                text = "InnerTune",
                color = Color.White,
                fontSize = 26.sp,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleSmall,

                )

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
    }


    }
        PreferenceEntry(
            title = { Text(stringResource(R.string.appearance)) },
            icon = { Icon(Icons.Rounded.Palette, null) },
            onClick = { navController.navigate("settings/appearance") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.content)) },
            icon = { Icon(Icons.Rounded.Language, null) },
            onClick = { navController.navigate("settings/content") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.player_and_audio)) },
            icon = { Icon(Icons.Rounded.PlayArrow, null) },
            onClick = { navController.navigate("settings/player") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.local_player_settings_title)) },
            icon = { Icon(Icons.Rounded.SdCard, null) },
            onClick = { navController.navigate("settings/local") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.storage)) },
            icon = { Icon(Icons.Rounded.Storage, null) },
            onClick = { navController.navigate("settings/storage") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.privacy)) },
            icon = { Icon(Icons.Rounded.Security, null) },
            onClick = { navController.navigate("settings/privacy") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.backup_restore)) },
            icon = { Icon(Icons.Rounded.Restore, null) },
            onClick = { navController.navigate("settings/backup_restore") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.experimental_settings_title)) },
            icon = { Icon(Icons.Rounded.WarningAmber, null) },
            onClick = { navController.navigate("settings/experimental") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.about)) },
            icon = { Icon(Icons.Rounded.Info, null) },
            onClick = { navController.navigate("settings/about") }
        )



        Spacer(Modifier.height(25.dp))
        Card(

            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,

                ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/releases/latest") }

        ) {
            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(Modifier.height(3.dp))
                Text(
                    text = "\uD835\uDE85\uD835\uDE8E\uD835\uDE9B\uD835\uDE9C\uD835\uDE92\uD835\uDE98\uD835\uDE97 : ${BuildConfig.VERSION_NAME} \n" +
                            "(\uD835\uDE24\uD835\uDE2D\uD835\uDE2A\uD835\uDE24\uD835\uDE2C \uD835\uDE35\uD835\uDE30 \uD835\uDE36\uD835\uDE31\uD835\uDE25\uD835\uDE22\uD835\uDE35\uD835\uDE26)",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                    color = MaterialTheme.colorScheme.secondary,
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://t.me/+NZXjVj6lETxkYTNh") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.telegram))
                Text(stringResource(R.string.Telegramchanel))
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.TelegramDescription)
                    , color = MaterialTheme.colorScheme.error,)

            }
        }
    }


    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
