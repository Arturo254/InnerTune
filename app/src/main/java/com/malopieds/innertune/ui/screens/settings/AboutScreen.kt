package com.malopieds.innertune.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.malopieds.innertune.BuildConfig
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,

    ) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))

        Image(
            painter = painterResource(R.drawable.launcher_monochrome),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
                .clickable { }
        )


        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "InnerTune",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = " ${BuildConfig.VERSION_NAME } ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = BuildConfig.FLAVOR.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(
                        horizontal = 6.dp,
                        vertical = 2.dp
                    )
            )

            if (BuildConfig.DEBUG) {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = "DEBUG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "\uD835\uDE73\uD835\uDE8E\uD835\uDE9F \uD835\uDE71\uD835\uDEA2 \uD835\uDE70\uD835\uDE9B\uD835\uDE9D\uD835\uDE9E\uD835\uDE9B\uD835\uDE98 \uD835\uDE72\uD835\uDE8E\uD835\uDE9B\uD835\uDE9F\uD835\uDE8A\uD835\uDE97\uD835\uDE9D\uD835\uDE8E\uD835\uDE9C 亗",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(8.dp))

        Row {
            IconButton(
                onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune") }
            ) {
                Icon(

                    painter = painterResource(R.drawable.github),
                    contentDescription = null

                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://innertunne.netlify.app/") }
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.liberapay))
            }

            IconButton(
                onClick = { uriHandler.openUri("https://www.paypal.com/donate?hosted_button_id=LPK2LT9SY5MBY") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.buymeacoffee),
                    contentDescription = null
                )
            }


        }
        Spacer(Modifier.height(70.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://innertunne.netlify.app/pdp") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.secure))
                Text(
                    text = (stringResource(R.string.privacy_policy)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.Privacy)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://raw.githubusercontent.com/Arturo254/InnerTune/master/LICENSE") } // Reemplaza con el enlace correcto de WhatsApp si deseas
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.licencia))
                Text(
                    text = (stringResource(R.string.license)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.license_text)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune") } // Reemplaza con el enlace correcto de WhatsApp si deseas
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.codigo))
                Text(
                    text = (stringResource(R.string.code)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.code_text)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/issues") } // Reemplaza con el enlace correcto de WhatsApp si deseas
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.bug))
                Text(
                    text = (stringResource(R.string.bugs)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.bugs_text)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://wa.me/525576847925") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.ayuda))
                Text(
                    text = (stringResource(R.string.help)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.help_text)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }


        }
        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = (stringResource(R.string.contributors)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )


        }
// Users:

        // Arturo254
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Arturo254") }
        ) {
            Row(
                modifier = Modifier.padding(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = "https://avatars.githubusercontent.com/u/87346871?v=4",

                        ),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                NavigationBarDefaults.Elevation
                            )
                        )
                        .clickable { }
                )

                Text(
                    text = "  \uD835\uDE08\uD835\uDE33\uD835\uDE35\uD835\uDE36\uD835\uDE33\uD835\uDE30254:",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "   \uD835\uDE47\uD835\uDE5A\uD835\uDE56\uD835\uDE59 \uD835\uDE3F\uD835\uDE5A\uD835\uDE6B\uD835\uDE5A\uD835\uDE61\uD835\uDE64\uD835\uDE65\uD835\uDE5A\uD835\uDE67",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )


            }


        }


        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Fabito02/") }
        ) {
            Row(
                modifier = Modifier.padding(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = "https://avatars.githubusercontent.com/u/138934847?v=4",

                        ),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
                        .clickable { }
                )

                Text(
                    text = "  \uD835\uDE0D\uD835\uDE22\uD835\uDE23\uD835\uDE2A\uD835\uDE35\uD835\uDE3002",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el nombre y otros elementos

                Text(
                    text = "  \uD835\uDE4F\uD835\uDE67\uD835\uDE56\uD835\uDE59\uD835\uDE6A\uD835\uDE58\uD835\uDE69\uD835\uDE64\uD835\uDE67 (\uD835\uDE4B\uD835\uDE67-\uD835\uDE3D\uD835\uDE4D)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )



            }


        }

        // Contribution by:

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/new/master") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = (stringResource(R.string.contribution)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = (stringResource(R.string.contribution_text)),
                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onSurface
                )

            }
        }




    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
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