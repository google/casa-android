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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.ui.components.CardItem
import com.google.android.catalog.framework.ui.components.CatalogTopAppBar
import com.google.android.catalog.framework.ui.components.FilterTabRow
import com.google.android.catalog.framework.ui.components.SearchTopAppBar

internal const val CATALOG_DESTINATION = "catalog"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun CatalogScreen(
    catalogSamples: List<CatalogSample>,
    catalogSettings: CatalogSettings,
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
                if (searchTerm.isBlank()) {
                    selectedFilters.isEmpty() ||
                        selectedFilters.contains(sample.path) ||
                        sample.tags.any { selectedFilters.contains(it) }
                } else {
                    sample.name.contains(searchTerm, ignoreCase = true) ||
                        sample.description.contains(searchTerm, ignoreCase = true) ||
                        sample.tags.any { it.equals(searchTerm, ignoreCase = true) }
                }
            }.sortedWith(catalogSettings.order)
        }
    }

    Scaffold(
        topBar = {
            if (searchState) {
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
                CatalogTopAppBar(onSearch = { searchState = true })
            }
        }
    ) { paddingValues ->
        SamplesList(
            paddingValues,
            filters,
            selectedFilters,
            displayedSamples,
            catalogSettings.cardAppearance
        ) {
            launchSample(it)
            searchState = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SamplesList(
    paddingValues: PaddingValues,
    filters: List<String>,
    selectedFilters: SnapshotStateList<String>,
    displayedSamples: List<CatalogSample>,
    appearance: CatalogCardAppearance,
    launchSample: (CatalogSample) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = paddingValues
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
                    .padding(horizontal = 16.dp),
                label = it.name,
                appearance = appearance,
                description = it.description,
                tags = it.tags,
                owners = it.owners,
                minSDK = it.minSDK,
            ) {
                launchSample(it)
            }
        }
    }
}
