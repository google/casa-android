/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.catalog.framework.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.catalog.framework.ui.CatalogCardAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardItem(
    modifier: Modifier = Modifier,
    label: String,
    appearance: CatalogCardAppearance,
    description: String = "",
    tags: List<String> = emptyList(),
    owners: List<String> = emptyList(),
    minSDK: Int = 0,
    selected: Boolean,
    onItemClick: () -> Unit
) {
    val enabled = Build.VERSION.SDK_INT >= minSDK
    val interactionSource = remember { MutableInteractionSource() }
    val interactionModifier = if (isExpandedScreen()) {
        Modifier.selectable(
            selected = selected,
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = { onItemClick() }
        )
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = { onItemClick() }
        )
    }

    ElevatedCard(
        modifier = modifier.then(interactionModifier),
        enabled = enabled,
        colors = if (selected) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.elevatedCardColors()
        },
        onClick = onItemClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                Icon(Icons.Rounded.KeyboardArrowRight, "Forward")
            }
            if (appearance.description > 0) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    text = description,
                    maxLines = appearance.description,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!enabled) {
                    item {
                        Text(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                .padding(6.dp),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            text = "minSDK=$minSDK",
                        )
                    }
                }
                if (appearance.tags) {
                    items(tags) { tag ->
                        Text(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            text = tag
                        )
                    }
                }
                if (appearance.owners) {
                    items(owners) { owner ->
                        Text(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            text = owner
                        )
                    }
                }
            }
        }
    }
}
