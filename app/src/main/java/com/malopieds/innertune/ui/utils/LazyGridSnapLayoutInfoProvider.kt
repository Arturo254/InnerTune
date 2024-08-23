@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.malopieds.innertune.ui.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.util.fastForEach

@ExperimentalFoundationApi
fun SnapLayoutInfoProvider(
    lazyGridState: LazyGridState,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float = { layoutSize, itemSize ->
        (layoutSize / 2f - itemSize / 2f)
    },
): SnapLayoutInfoProvider =
    object : SnapLayoutInfoProvider {
        private val layoutInfo: LazyGridLayoutInfo
            get() = lazyGridState.layoutInfo

        override fun calculateApproachOffset(
            velocity: Float,
            decayOffset: Float,
        ): Float = 0f

        override fun calculateSnapOffset(velocity: Float): Float {
            val bounds = calculateSnappingOffsetBounds()
            return when {
                velocity < 0 -> bounds.start
                velocity > 0 -> bounds.endInclusive
                else -> 0f
            }
        }

        fun calculateSnappingOffsetBounds(): ClosedFloatingPointRange<Float> {
            var lowerBoundOffset = Float.NEGATIVE_INFINITY
            var upperBoundOffset = Float.POSITIVE_INFINITY

            layoutInfo.visibleItemsInfo.fastForEach { item ->
                val offset = calculateDistanceToDesiredSnapPosition(layoutInfo, item, positionInLayout)

                // Find item that is closest to the center
                if (offset <= 0 && offset > lowerBoundOffset) {
                    lowerBoundOffset = offset
                }

                // Find item that is closest to center, but after it
                if (offset >= 0 && offset < upperBoundOffset) {
                    upperBoundOffset = offset
                }
            }

            return lowerBoundOffset.rangeTo(upperBoundOffset)
        }
    }

fun calculateDistanceToDesiredSnapPosition(
    layoutInfo: LazyGridLayoutInfo,
    item: LazyGridItemInfo,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float,
): Float {
    val containerSize = layoutInfo.singleAxisViewportSize - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding

    val desiredDistance = positionInLayout(containerSize.toFloat(), item.size.width.toFloat())
    val itemCurrentPosition = item.offset.x.toFloat()

    return itemCurrentPosition - desiredDistance
}

private val LazyGridLayoutInfo.singleAxisViewportSize: Int
    get() = if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width
