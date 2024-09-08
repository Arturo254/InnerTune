package com.malopieds.innertune.ui.screens.settings


import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.malopieds.innertube.utils.parseCookieString
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.AccountChannelHandleKey
import com.malopieds.innertune.constants.AccountEmailKey
import com.malopieds.innertune.constants.AccountNameKey
import com.malopieds.innertune.constants.ChipSortTypeKey
import com.malopieds.innertune.constants.ContentCountryKey
import com.malopieds.innertune.constants.ContentLanguageKey
import com.malopieds.innertune.constants.CountryCodeToName
import com.malopieds.innertune.constants.HideExplicitKey
import com.malopieds.innertune.constants.HistoryDuration
import com.malopieds.innertune.constants.InnerTubeCookieKey
import com.malopieds.innertune.constants.LanguageCodeToName
import com.malopieds.innertune.constants.LibraryFilter
import com.malopieds.innertune.constants.ProxyEnabledKey
import com.malopieds.innertune.constants.ProxyTypeKey
import com.malopieds.innertune.constants.ProxyUrlKey
import com.malopieds.innertune.constants.QuickPicks
import com.malopieds.innertune.constants.QuickPicksKey
import com.malopieds.innertune.constants.SYSTEM_DEFAULT
import com.malopieds.innertune.constants.TopSize
import com.malopieds.innertune.ui.component.EditTextPreference
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.ListPreference
import com.malopieds.innertune.ui.component.PreferenceEntry
import com.malopieds.innertune.ui.component.PreferenceGroupTitle
import com.malopieds.innertune.ui.component.SliderPreference
import com.malopieds.innertune.ui.component.SwitchPreference
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.utils.rememberEnumPreference
import com.malopieds.innertune.utils.rememberPreference
import java.net.Proxy
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn =
        remember(innerTubeCookie) {
            "SAPISID" in parseCookieString(innerTubeCookie)
        }
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val (hideExplicit, onHideExplicitChange) = rememberPreference(key = HideExplicitKey, defaultValue = false)
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")
    val (lengthTop, onLengthTopChange) = rememberPreference(key = TopSize, defaultValue = "50")
    val (historyDuration, onHistoryDurationChange) = rememberPreference(key = HistoryDuration, defaultValue = 30f)
    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(key = ChipSortTypeKey, defaultValue = LibraryFilter.LIBRARY)
    val (quickPicks, onQuickPicksChange) = rememberEnumPreference(key = QuickPicksKey, defaultValue = QuickPicks.QUICK_PICKS)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceEntry(
            title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
            description =
                if (isLoggedIn) {
                    accountEmail.takeIf { it.isNotEmpty() }
                        ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                } else {
                    null
                },
            icon = { Icon(painterResource(R.drawable.person), null) },
            onClick = { navController.navigate("login") },
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_language)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            selectedValue = contentLanguage,
            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
            valueText = {
                LanguageCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentLanguageChange,
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_country)) },
            icon = { Icon(painterResource(R.drawable.location_on), null) },
            selectedValue = contentCountry,
            values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
            valueText = {
                CountryCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentCountryChange,
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.app_language),
        )

        LanguageSelector()



        SwitchPreference(
            title = { Text(stringResource(R.string.hide_explicit)) },
            icon = { Icon(painterResource(R.drawable.explicit), null) },
            checked = hideExplicit,
            onCheckedChange = onHideExplicitChange,
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.proxy),
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_proxy)) },
            icon = { Icon(painterResource(R.drawable.wifi_proxy), null) },
            checked = proxyEnabled,
            onCheckedChange = onProxyEnabledChange,
        )

        AnimatedVisibility(proxyEnabled) {
            Column {
                ListPreference(
                    title = { Text(stringResource(R.string.proxy_type)) },
                    selectedValue = proxyType,
                    values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                    valueText = { it.name },
                    onValueSelected = onProxyTypeChange,
                )
                EditTextPreference(
                    title = { Text(stringResource(R.string.proxy_url)) },
                    value = proxyUrl,
                    onValueChange = onProxyUrlChange,
                )
            }
        }

        EditTextPreference(
            title = { Text(stringResource(R.string.top_length)) },
            value = lengthTop,
            isInputValid = {
                val number = it.toIntOrNull()
                number != null && it.isNotEmpty() && number > 0
            },
            onValueChange = onLengthTopChange,
        )

        ListPreference(
            title = { Text(stringResource(R.string.default_lib_chips)) },
            selectedValue = defaultChip,
            values =
                listOf(
                    LibraryFilter.LIBRARY,
                    LibraryFilter.PLAYLISTS,
                    LibraryFilter.SONGS,
                    LibraryFilter.ALBUMS,
                    LibraryFilter.ARTISTS,
                ),
            valueText = {
                when (it) {
                    LibraryFilter.SONGS -> stringResource(R.string.songs)
                    LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                    LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                    LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                    LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                }
            },
            onValueSelected = onDefaultChipChange,
        )

        ListPreference(
            title = { Text(stringResource(R.string.set_quick_picks)) },
            selectedValue = quickPicks,
            values = listOf(QuickPicks.QUICK_PICKS, QuickPicks.LAST_LISTEN),
            valueText = {
                when (it) {
                    QuickPicks.QUICK_PICKS -> stringResource(R.string.quick_picks)
                    QuickPicks.LAST_LISTEN -> stringResource(R.string.last_song_listened)
                }
            },
            onValueSelected = onQuickPicksChange,
        )

        SliderPreference(
            title = { Text(stringResource(R.string.history_duration)) },
            value = historyDuration,
            onValueChange = onHistoryDurationChange,
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.content)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}


@Composable
fun LanguageSelector() {
    val context = LocalContext.current
    // List of supported languages and their locale codes
    val languages = listOf(
        "Arabic" to "ar",
        "Belarusian" to "be",
        "Chinese Simplified" to "zh",
        "Czech" to "cs",
        "Dutch" to "nl",
        "English" to "en",
        "French" to "fr",
        "German" to "de",
        "Indonesian" to "id",
        "Italian" to "it",
        "Japanese" to "ja",
        "Korean" to "ko",
        "Portuguese, Brazilian" to "pt-BR",
        "Russian" to "ru",
        "Spanish" to "es",
        "Turkish" to "tr",
        "Ukrainian" to "uk",
        "Vietnamese" to "vi"
    )

    // State to hold the currently selected language
    var selectedLanguage by remember { mutableStateOf(languages[0].second) }
    var expanded by remember { mutableStateOf(false) } // Dropdown expanded state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

    ) {
        Column(modifier = Modifier.padding(16.dp)) {


            // Dropdown button
            FloatingActionButton(
                modifier = Modifier
                    .size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { expanded = true },
            ) {
               Icon(
                   painter = painterResource(R.drawable.translate),
                   contentDescription = null
               )
            }


Box(
    modifier = Modifier.padding(16.dp),
    contentAlignment = Alignment.Center

)
{


        // Dropdown menu for language selection
        DropdownMenu(

            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = language.first) },
                    onClick = {
                        selectedLanguage = language.second
                        expanded = false
                        updateLanguage(context, selectedLanguage)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
}
}


fun updateLanguage(context: Context, languageCode: String) {
    val locale: Locale = if (languageCode.contains("-")) {
        // Handle languages with regions like pt-BR
        val parts = languageCode.split("-")
        Locale(parts[0], parts[1])
    } else {
        Locale(languageCode)
    }

    val config = Configuration(context.resources.configuration)
    config.setLocales(LocaleList(locale))

    // Update the configuration
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    // Optionally, recreate the activity to apply the language change throughout the app
    (context as? androidx.activity.ComponentActivity)?.recreate()
}


