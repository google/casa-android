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

package com.google.android.catalog.framework.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.base.CatalogTarget
import com.google.android.catalog.framework.ui.components.AdaptivePane
import com.google.android.catalog.framework.ui.components.CardItem
import com.google.android.catalog.framework.ui.components.CatalogTopAppBar
import com.google.android.catalog.framework.ui.components.FilterTabRow
import com.google.android.catalog.framework.ui.components.FragmentContainer
import com.google.android.catalog.framework.ui.components.SearchTopAppBar
import com.google.android.catalog.framework.ui.components.isExpandedScreen

internal const val CATALOG_DESTINATION = "catalog"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun CatalogScreen(
    catalogSamples: List<CatalogSample>,
    catalogSettings: CatalogSettings,
    fragmentManager: FragmentManager,
    launchSample: (CatalogSample) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var searchState by rememberSaveable {
        mutableStateOf(false)
    }
    var searchTerm by rememberSaveable(searchState) {
        mutableStateOf("")
    }
    val filters by remember(catalogSamples, catalogSettings) {
        derivedStateOf {
            catalogSettings.filters.flatMap { filter ->
                when (filter) {
                    is CatalogFilter.Path -> catalogSamples.groupBy {
                        it.path.split("/").take(filter.depth + 1).joinToString("/")
                    }.keys.distinct()

                    CatalogFilter.Tag -> catalogSamples.flatMap { it.tags }.distinct()
                }
            }.sorted()
        }
    }
    val selectedFilters = rememberSaveable(
        saver = listSaver(
            save = {
                it.toList()
            },
            restore = {
                it.toMutableStateList()
            }
        )
    ) {
        mutableStateListOf<String>()
    }
    val displayedSamples by remember(searchTerm, selectedFilters) {
        derivedStateOf {
            catalogSamples.filter { sample ->
                selectedFilters.isEmpty() ||
                    selectedFilters.any { sample.path.contains(it) } ||
                    sample.tags.any { selectedFilters.contains(it) }
            }.filter { sample ->
                searchTerm.isBlank() ||
                    sample.name.contains(searchTerm, ignoreCase = true) ||
                    sample.description.contains(searchTerm, ignoreCase = true) ||
                    sample.tags.any { it.equals(searchTerm, ignoreCase = true) }
            }.sortedWith(catalogSettings.order)
        }
    }

    val isExpandedScreen = isExpandedScreen()
    val saver = Saver<CatalogSample?, String>(
        save = { it?.route.orEmpty() },
        restore = { item -> displayedSamples.find { it.route == item } }
    )
    var selectedSample by rememberSaveable(isExpandedScreen, stateSaver = saver) {
        mutableStateOf(null)
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = searchState,
                transitionSpec = {
                    if (targetState) {
                        expandHorizontally { it / 8 } + fadeIn(tween(100)) with fadeOut()
                    } else {
                        fadeIn(tween(500, delayMillis = 100)) with
                            shrinkHorizontally(tween(400)) { it / 8 } + fadeOut(tween(400))
                    }
                },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (it) {
                    SearchTopAppBar(
                        searchTerm = searchTerm,
                        focusRequester = focusRequester,
                        onSearch = {
                            searchState = false
                        },
                        onClear = {
                            searchState = false
                        },
                        onValueChange = {
                            searchTerm = it
                        }
                    )
                } else {
                    CatalogTopAppBar(
                        selectedSample = if (isExpandedScreen) selectedSample else null,
                        onSearch = { searchState = true },
                        onExpand = { launchSample(selectedSample!!) }
                    )
                }
            }
        }
    ) { paddingValues ->
        AdaptivePane(
            paddingValues = paddingValues,
            start = { innerPadding ->
                SamplesList(
                    filters,
                    selectedFilters,
                    displayedSamples,
                    selectedSample,
                    catalogSettings.cardAppearance,
                    innerPadding,
                ) {
                    // Activities cannot be shown in split screen, instead always launch them.
                    if (!isExpandedScreen || it.target is CatalogTarget.TargetActivity) {
                        launchSample(it)
                    }
                    if (it.target !is CatalogTarget.TargetActivity) {
                        selectedSample = it
                    }
                    searchState = false
                }
            },
            end = { innerPadding ->
                when (val target = selectedSample?.target) {
                    is CatalogTarget.TargetFragment -> {
                        FragmentContainer(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            fragmentManager = fragmentManager,
                            commit = { id ->
                                add(id, target.targetClass.java.newInstance())
                            }
                        )
                    }

                    is CatalogTarget.TargetComposable -> {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            target.composable()
                        }
                    }

                    else -> EmptySample(Modifier.padding(innerPadding))
                }
            }
        )
    }
}

@Composable
private fun EmptySample(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Select a sample")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SamplesList(
    filters: List<String>,
    selectedFilters: SnapshotStateList<String>,
    displayedSamples: List<CatalogSample>,
    selectedSample: CatalogSample?,
    appearance: CatalogCardAppearance,
    contentPadding: PaddingValues,
    launchSample: (CatalogSample) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FilterTabRow(filters, selectedFilters) {
                if (selectedFilters.contains(it)) {
                    selectedFilters.remove(it)
                } else {
                    selectedFilters.add(it)
                }
            }
        }
        items(displayedSamples, key = { it.route }) {
            CardItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(spring())
                    .padding(horizontal = 16.dp),
                label = it.name,
                appearance = appearance,
                description = it.description,
                tags = it.tags,
                owners = it.owners,
                minSDK = it.minSDK,
                selected = it == selectedSample && isExpandedScreen()
            ) {
                launchSample(it)
            }
        }
    }
}
