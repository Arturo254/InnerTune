package com.malopieds.innertune.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.malopieds.innertune.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    data object Home : Screens(R.string.home, R.drawable.home, "home")

    data object Explore : Screens(R.string.explore, R.drawable.explore, "explore")

    data object Library : Screens(R.string.library, R.drawable.library_music, "library")

    companion object {
        val MainScreens = listOf(Home, Explore, Library)
    }
}
