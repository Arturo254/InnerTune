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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
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
                text = "${BuildConfig.VERSION_NAME} ",
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

        Spacer(Modifier.height(20.dp))
        CardItem(
            icon = R.drawable.verified_user,
            title = stringResource(R.string.privacy_policy),
            subtitle = stringResource(R.string.Privacy),
            onClick = { uriHandler.openUri("https://innertunne.netlify.app/pdp") }
        )

        Spacer(Modifier.height(20.dp))

        CardItem(
            icon = R.drawable.license,
            title = stringResource(R.string.license),
            subtitle = stringResource(R.string.license_text),
            onClick = { uriHandler.openUri("https://raw.githubusercontent.com/Arturo254/InnerTune/master/LICENSE") }
        )

        Spacer(Modifier.height(20.dp))

        CardItem(
            icon = R.drawable.codigo,
            title = stringResource(R.string.code),
            subtitle = stringResource(R.string.code_text),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune") }
        )

        Spacer(Modifier.height(20.dp))

        CardItem(
            icon = R.drawable.bug_report,
            title = stringResource(R.string.bugs),
            subtitle = stringResource(R.string.bugs_text),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/issues") }
        )

        Spacer(Modifier.height(20.dp))

        CardItem(
            icon = R.drawable.help,
            title = stringResource(R.string.help),
            subtitle = stringResource(R.string.help_text),
            onClick = { uriHandler.openUri("https://wa.me/525576847925") }
        )
        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.group),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.contributors),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }


        }



// Users:

        // Arturo254
        Spacer(Modifier.height(20.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
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
                    text = " 亗 Arturo254 :",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "   Lead Developer",
                    style = MaterialTheme.typography.bodyMedium,fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                )


            }


        }
        //Fabito02

        Spacer(Modifier.height(20.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
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
                    text = " \uD81A\uDD10 Fabito02 : ",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el nombre y otros elementos

                Text(
                    text = "  Traductor (PR_BR)",
                    style = MaterialTheme.typography.bodyMedium,fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                )



            }


        }

//        // Alessandro
//        Spacer(Modifier.height(20.dp))
//        ElevatedCard(
//            elevation = CardDefaults.cardElevation(
//                defaultElevation = 6.dp
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(100.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface,
//            ),
//            onClick = { uriHandler.openUri("https://github.com/AlessandroGalvan") }
//        ) {
//            Row(
//                modifier = Modifier.padding(26.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Image(
//                    painter = rememberAsyncImagePainter(
//                        model = "https://avatars.githubusercontent.com/u/40720048?v=4",
//
//                        ),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .background(
//                            MaterialTheme.colorScheme.surfaceColorAtElevation(
//                                NavigationBarDefaults.Elevation
//                            )
//                        )
//                        .clickable { }
//                )
//
//                Text(
//                    text = " 「★」 AlessandroGalvan : ",
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.bodyLarge,fontFamily = FontFamily.Monospace,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Text(
//                    text = "S.B.P",
//                    style = MaterialTheme.typography.bodyMedium,fontFamily = FontFamily.SansSerif,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//
//            }
//
//
//        }


        // Contribution by:

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
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
                    color = MaterialTheme.colorScheme.primary
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

@Composable
fun CardItem(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
