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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.ui.components.CardItem
import com.google.android.catalog.framework.ui.components.CatalogTopAppBar
import com.google.android.catalog.framework.ui.components.FilterTabRow
import com.google.android.catalog.framework.ui.components.SearchTopAppBar

internal const val CATALOG_DESTINATION = "catalog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogScreen(
    catalogSamples: List<CatalogSample>,
    launchSample: (CatalogSample) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var searchState by remember {
        mutableStateOf(false)
    }
    var searchTerm by remember(searchState) {
        mutableStateOf("")
    }
    val categories = remember(catalogSamples) {
        catalogSamples.groupBy { it.path }
    }
    val tags = remember(catalogSamples) {
        catalogSamples.flatMap { it.tags }.distinct()
    }
    val filters by remember {
        derivedStateOf {
            (categories.keys + tags).sorted()
        }
    }
    val selectedFilters = remember { mutableStateListOf<String>() }
    val displayedSamples = catalogSamples.filter { sample ->
        if (searchTerm.isBlank()) {
            selectedFilters.isEmpty() ||
                selectedFilters.contains(sample.path) ||
                sample.tags.any { selectedFilters.contains(it) }
        } else {
            sample.name.contains(searchTerm, ignoreCase = true) ||
                sample.description.contains(searchTerm, ignoreCase = true) ||
                sample.tags.any { it.equals(searchTerm, ignoreCase = true) }
        }
    }.sortedBy {
        it.name
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
            items(displayedSamples) {
                CardItem(
                    label = it.name,
                    description = it.description,
                    tags = it.tags,
                    minSDK = it.minSDK,
                ) {
                    launchSample(it)
                    searchState = false
                }
            }
        }
    }
}
