/*
 * Copyright (C) 2023 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of OpenComicVine.
 *
 * OpenComicVine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenComicVine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenComicVine.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.proninyaroslav.opencomicvine.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.proninyaroslav.opencomicvine.R
import org.proninyaroslav.opencomicvine.ui.components.card.CardWithImage
import org.proninyaroslav.opencomicvine.ui.theme.OpenComicVineTheme

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        AnimatedIcon(isFavorite = isFavorite)
    }
}

@Composable
fun FavoriteFilledTonalActionButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        AnimatedIcon(isFavorite = isFavorite)
    }
}

@Composable
fun FavoriteFilledTonalButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp),
    ) {
        AnimatedIcon(isFavorite = isFavorite)
    }
}

@Composable
private fun AnimatedIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    favoriteTint: Color = MaterialTheme.colorScheme.primary,
) {
    AnimatedContent(targetState = isFavorite) { isFavoriteVal ->
        Icon(
            painterResource(
                if (isFavoriteVal) {
                    R.drawable.ic_favorite_filled_24
                } else {
                    R.drawable.ic_favorite_24
                }
            ),
            tint = if (isFavoriteVal) favoriteTint else LocalContentColor.current,
            contentDescription = stringResource(R.string.add_to_favorite),
            modifier = modifier.offset(y = 2.dp),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoriteSwipeableBox(
    isFavorite: Boolean,
    icon: @Composable () -> Unit,
    actionLabel: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    RevealSwipe(
        directions = setOf(RevealDirection.StartToEnd),
        hiddenContentStart = {
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = modifier.fillMaxSize(),
            ) {
                icon()
            }
        },
        backgroundCardColors = animateBackgroundCardColors(isFavorite),
        maxRevealDp = FavoriteBoxDefaults.maxRevealDp,
        closeOnContentClick = false,
        backgroundStartActionLabel = actionLabel,
        backgroundEndActionLabel = null,
        modifier = modifier.clipToBounds(),
        content = content,
    )
}

@Composable
fun FavoriteBox(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    iconAlignment: Alignment = Alignment.TopEnd,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .align(iconAlignment)
                .padding(4.dp),
        ) {
            icon()
        }
    }
}

@Composable
fun animateBackgroundCardColors(isFavorite: Boolean): CardColors {
    val backgroundContainerColor by animateColorAsState(
        if (isFavorite) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            RevealSwipeDefaults.backgroundCardContainerColor
        }
    )
    val backgroundContentColor by animateColorAsState(
        if (isFavorite) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            RevealSwipeDefaults.backgroundCardContentColor
        }
    )

    return CardDefaults.cardColors(
        containerColor = backgroundContainerColor,
        contentColor = backgroundContentColor,
    )
}

object FavoriteBoxDefaults {
    val maxRevealDp = 48.dp
}

@Preview
@Composable
fun PreviewFavoriteButton() {
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteButton(
            isFavorite = isFavorite,
            onClick = { isFavorite = !isFavorite },
        )
    }
}

@Preview
@Composable
fun PreviewFavoriteFilledTonalActionButton() {
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteFilledTonalActionButton(
            isFavorite = isFavorite,
            onClick = { isFavorite = !isFavorite },
        )
    }
}

@Preview
@Composable
fun PreviewFavoriteFilledTonalButton() {
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteFilledTonalButton(
            isFavorite = isFavorite,
            onClick = { isFavorite = !isFavorite },
        )
    }
}

@Preview
@Composable
fun PreviewFavoriteSwipeableBox() {
    val titleStyle = MaterialTheme.typography.titleMedium
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteSwipeableBox(
            isFavorite = isFavorite,
            icon = {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { isFavorite = !isFavorite },
                )
            },
            actionLabel = stringResource(R.string.add_to_favorite),
        ) {
            CardWithImage(
                imageUrl = "https://dummyimage.com/320",
                imageDescription = "Dummy image",
                placeholder = R.drawable.placeholder_square,
                onClick = {},
            ) {
                Text(
                    "Title",
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewFavoriteSwipeableBox_Dark() {
    val titleStyle = MaterialTheme.typography.titleMedium
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteSwipeableBox(
            isFavorite = isFavorite,
            icon = {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { isFavorite = !isFavorite },
                )
            },
            actionLabel = stringResource(R.string.add_to_favorite),
        ) {
            CardWithImage(
                imageUrl = "https://dummyimage.com/320",
                imageDescription = "Dummy image",
                placeholder = R.drawable.placeholder_square,
                onClick = {},
            ) {
                Text(
                    "Title",
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewFavoriteBox() {
    val titleStyle = MaterialTheme.typography.titleMedium
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteBox(
            icon = {
                FavoriteFilledTonalButton(
                    isFavorite = isFavorite,
                    onClick = { isFavorite = !isFavorite },
                )
            },
        ) {
            CardWithImage(
                imageUrl = "https://dummyimage.com/320",
                imageDescription = "Dummy image",
                placeholder = R.drawable.placeholder_square,
                onClick = {},
            ) {
                Text(
                    "Title",
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewFavoriteBox_Dark() {
    val titleStyle = MaterialTheme.typography.titleMedium
    OpenComicVineTheme {
        var isFavorite by remember { mutableStateOf(false) }
        FavoriteBox(
            icon = {
                FavoriteFilledTonalButton(
                    isFavorite = isFavorite,
                    onClick = { isFavorite = !isFavorite },
                )
            },
        ) {
            CardWithImage(
                imageUrl = "https://dummyimage.com/320",
                imageDescription = "Dummy image",
                placeholder = R.drawable.placeholder_square,
                onClick = {},
            ) {
                Text(
                    "Title",
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
