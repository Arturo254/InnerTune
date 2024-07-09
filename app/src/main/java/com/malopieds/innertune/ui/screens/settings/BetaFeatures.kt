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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.malopieds.innertune.BuildConfig
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeatures(
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
            painter = painterResource(R.drawable.funbeta),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
            modifier = Modifier
                .size(100.dp)
                .clickable { }
        )

        Spacer(Modifier.height(40.dp))



        // Cards

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .shadow(8.dp, RoundedCornerShape(26.dp)), // Sombra y bordes redondeados
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(26.dp) // Bordes redondeados
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally // Alineación horizontal al centro
                ) {
                    Text(
                        text = stringResource(R.string.IAFeatures),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 19.sp, // Tamaño del título
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 4.dp) // Espacio inferior
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.TextIAFeatures),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp, // Tamaño del texto
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .shadow(8.dp, RoundedCornerShape(26.dp)), // Sombra y bordes redondeados
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(26.dp) // Bordes redondeados
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally // Alineación horizontal al centro
            ) {
                Text(
                    text = stringResource(R.string.APIFeatures),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 19.sp, // Tamaño del título
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 4.dp) // Espacio inferior
                )
                Spacer(Modifier.height(4.dp))
                Text(
                        text = stringResource(R.string.TextAPIFeatures),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp, // Tamaño del texto
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        // Fin Cards

        }










    TopAppBar(
        title = { Text(stringResource(R.string.BetaFeatures)) },
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