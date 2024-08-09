package com.malopieds.innertune.ui.screens.settings

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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.malopieds.innertube.utils.parseCookieString
import com.malopieds.innertune.BuildConfig
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.AccountNameKey
import com.malopieds.innertune.constants.InnerTubeCookieKey
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.PreferenceEntry
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


@Composable
fun UpdateCard(uriHandler: UriHandler) {
    var showUpdateCard by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val newVersion = checkForUpdates()
        if (newVersion != null && isNewerVersion(newVersion, BuildConfig.VERSION_NAME)) {
            showUpdateCard = true
            latestVersion = newVersion
        } else {
            showUpdateCard = false
        }
    }

    if (showUpdateCard) {
        Spacer(Modifier.height(25.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            onClick = {
                uriHandler.openUri("https://github.com/Arturo254/InnerTune/releases/latest")
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "¡Nueva versión disponible: $latestVersion!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

suspend fun checkForUpdates(): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/Arturo254/InnerTune/releases/latest")
        val connection = url.openConnection()
        connection.connect()
        val json = connection.getInputStream().bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        return@withContext jsonObject.getString("tag_name")
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
    val remote = remoteVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val current = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

    for (i in 0 until maxOf(remote.size, current.size)) {
        val r = remote.getOrNull(i) ?: 0
        val c = current.getOrNull(i) ?: 0
        if (r > c) return true
        if (r < c) return false
    }
    return false
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    latestVersion: Long,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {


    val uriHandler = LocalUriHandler.current

//    var isBetaFunEnabled by remember { mutableStateOf(false) }


    val backgroundImages = listOf(

        R.drawable.cardbg,
        R.drawable.cardbg2,
        R.drawable.cardbg3,
        R.drawable.cardbg4,
        R.drawable.cardbg6,
        R.drawable.cardbg7,
        R.drawable.cardbg8,
        R.drawable.cardbg9,
        R.drawable.cardbg11,
        R.drawable.cardbg12,
        R.drawable.cardbg13,
        R.drawable.cardbg14,
        R.drawable.cardbg15,
        R.drawable.cardbg16,
        R.drawable.cardbg17,
        R.drawable.cardbg18,
        R.drawable.cardbg19,
        R.drawable.cardbg20,
        R.drawable.cardbg22,
        R.drawable.cardbg23,
        R.drawable.cardbg24,
        R.drawable.cardbg25,
        R.drawable.cardbg26,
        R.drawable.cardbg27,
        R.drawable.cardbg28,
        R.drawable.cardbg29,


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
                .clip(RoundedCornerShape(28.dp))
                .background(color = Color.Transparent)
                .clickable { changeBackgroundImage() } // change background image on click


        ) {
            Image(
                painter = painterResource(id = backgroundImages[currentImageIndex]),
                contentDescription = "background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
               .blur(0.5.dp)

            )

            val accountName by rememberPreference(AccountNameKey, "")

            val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
            val isLoggedIn =
                remember(innerTubeCookie) {
                    "SAPISID" in parseCookieString(innerTubeCookie)
                }
            PreferenceEntry(
                title = {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        if (isLoggedIn) {
                            Text(
                                stringResource(R.string.Hi),
                                color = Color.White,
                                fontSize = 20.sp,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                accountName.replace("@", ""),
                                color = Color.White,
                                fontSize = 20.sp,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.launcher_monochrome),
                                contentDescription = null,
                                tint = Color.White,

                                )
                            Text(
                                text = "InnerTune",
                                color = Color.White,
                                fontSize = 26.sp,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace

                                )

                        }
                    }
                },
                description = null,
                onClick = { changeBackgroundImage() },
            )
            }

        Spacer(Modifier.height(25.dp))

        PreferenceEntry(
            title = { Text(stringResource(R.string.appearance)) },
            icon = { Icon(painterResource(R.drawable.palette), null) },
            onClick = { navController.navigate("settings/appearance") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.content)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            onClick = { navController.navigate("settings/content") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.player_and_audio)) },
            icon = { Icon(painterResource(R.drawable.play), null) },
            onClick = { navController.navigate("settings/player") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.storage)) },
            icon = { Icon(painterResource(R.drawable.storage), null) },
            onClick = { navController.navigate("settings/storage") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.privacy)) },
            icon = { Icon(painterResource(R.drawable.security), null) },
            onClick = { navController.navigate("settings/privacy") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.backup_restore)) },
            icon = { Icon(painterResource(R.drawable.restore), null) },
            onClick = { navController.navigate("settings/backup_restore") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.about)) },
            icon = { Icon(painterResource(R.drawable.info), null) },
            onClick = { navController.navigate("settings/about") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.Donate)) },
            icon = { Icon(painterResource(R.drawable.donate), null) },
            onClick = { uriHandler.openUri("https://buymeacoffee.com/arturocervantes") }
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.Telegramchanel)) },
            icon = { Icon(painterResource(R.drawable.telegram), null) },
            onClick = { uriHandler.openUri("https://t.me/+NZXjVj6lETxkYTNh") }
        )

//        PreferenceEntry(
//            title = { Text(stringResource(R.string.betafun)) },
//            icon = { Icon(painterResource(R.drawable.funbeta), null) },
//
//            trailingContent = {
//
//                Switch(
//                    checked = isBetaFunEnabled,
//                    onCheckedChange = { isBetaFunEnabled = it },
//                    modifier = Modifier.padding(end = 8.dp)
//                )
//
//            },
//            onClick = {
//
//
//            }
//        )
//
        if (latestVersion > BuildConfig.VERSION_CODE) {
            PreferenceEntry(
                title = {
                    Text(
                        text = stringResource(R.string.new_version_available),
                    )
                },
                icon = {
                    BadgedBox(
                        badge = { Badge() },
                    ) {
                        Icon(painterResource(R.drawable.deployed_code_update), null)
                    }
                },
                onClick = {
                    uriHandler.openUri("https://github.com/Arturo254/InnerTune/releases/latest")
                },
            )
        }


//        Card(
//            modifier = Modifier
//
//                .fillMaxWidth()
//                .height(130.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.background,
//            ),
//            border = BorderStroke(1.dp, Color.White),
//
//            ) {
//            Column(
//                modifier = Modifier.padding(17.dp),
//                verticalArrangement = Arrangement.Center
//            ) {
//
//                Spacer(Modifier.height(3.dp))
//                Text(stringResource(R.string.BetaDescription))
//            }
//
//        }

        UpdateCard(uriHandler)
        Spacer(Modifier.height(25.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(38.dp))
                .fillMaxWidth()
                .height(85.dp),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,

                ),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/releases/latest") }

        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(38.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center

            ) {



                Spacer(Modifier.height(3.dp))
                Text(
                    text = " Version : ${BuildConfig.VERSION_NAME} \n  "  ,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),

                    )

            }
        }
        Spacer(Modifier.height(25.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,


                ),


            ) {
            Column(
                modifier = Modifier

                    .padding(20.dp),
                verticalArrangement = Arrangement.Center

            ) {



                Spacer(Modifier.height(3.dp))
                Text(
                    text = (stringResource(R.string.Betatext))  ,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier


                )


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
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior

    )
}