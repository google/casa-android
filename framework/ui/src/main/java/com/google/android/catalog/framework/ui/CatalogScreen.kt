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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.ui.components.CardItem
import com.google.android.catalog.framework.ui.components.FilterTabRow
import com.google.android.catalog.framework.ui.components.SearchTopAppBar

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
    val categories = catalogSamples.groupBy { it.path }
    val selectedFilters = remember { mutableStateListOf<String>() }
    val displayedSamples = catalogSamples.filter {
        if (searchTerm.isBlank()) {
            selectedFilters.isEmpty() || selectedFilters.contains(it.path)
        } else {
            it.name.contains(searchTerm, ignoreCase = true) ||
                it.description.contains(searchTerm, ignoreCase = true)
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
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = { searchState = !searchState }) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search button"
                            )
                        }
                    },
                )
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
                FilterTabRow(categories.keys.sorted(), selectedFilters) {
                    if (selectedFilters.contains(it)) {
                        selectedFilters.remove(it)
                    } else {
                        selectedFilters.add(it)
                    }
                }
            }
            items(displayedSamples) {
                CardItem(it.name, it.description) {
                    launchSample(it)
                    searchState = false
                }
            }
        }
    }
}
