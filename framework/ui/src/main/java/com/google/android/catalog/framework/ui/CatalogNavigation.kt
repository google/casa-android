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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.base.CatalogTarget
import com.google.android.catalog.framework.ui.components.CatalogTopAppBar
import com.google.android.catalog.framework.ui.components.FragmentContainer

@Composable
internal fun CatalogNavigation(
    startDestination: String,
    samples: Set<CatalogSample>,
    settings: CatalogSettings,
    fragmentManager: FragmentManager
) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = startDestination
    ) {
        // Add the home destination
        composable(CATALOG_DESTINATION) {
            CatalogScreen(samples.toList(), settings, fragmentManager) {
                navController.navigate(it.route)
            }
        }

        // Add all the samples
        samples.forEach { sample ->
            addTargets(
                sample = sample,
                fragmentManager = fragmentManager,
                settings = settings,
                onExpand = { navController.navigate(it.route) },
                onBackClick = navController::popBackStack
            )
        }
    }
}

private fun NavGraphBuilder.addTargets(
    sample: CatalogSample,
    fragmentManager: FragmentManager,
    settings: CatalogSettings,
    onExpand: (CatalogSample) -> Unit,
    onBackClick: () -> Unit,
) {
    when (val target = sample.target) {
        is CatalogTarget.TargetComposable -> {
            composable(sample.route) {
                SampleScaffold(
                    sample = sample,
                    settings = settings,
                    onExpand = { onExpand(sample) },
                    onBackClick = onBackClick,
                ) {
                    target.composable()
                }
            }
        }

        is CatalogTarget.TargetFragment -> {
            composable(sample.route) {
                SampleScaffold(
                    sample = sample,
                    settings = settings,
                    onExpand = { onExpand(sample) },
                    onBackClick = onBackClick
                ) {
                    FragmentContainer(
                        modifier = Modifier.fillMaxSize(),
                        fragmentManager = fragmentManager,
                        commit = { id ->
                            add(id, target.targetClass.java.newInstance())
                        }
                    )
                }
            }
        }

        is CatalogTarget.TargetActivity -> {
            activity(sample.route) {
                label = sample.name
                activityClass = target.targetClass
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SampleScaffold(
    sample: CatalogSample,
    settings: CatalogSettings,
    onExpand: () -> Unit,
    onBackClick: () -> Unit,
    content: @Composable() (BoxScope.() -> Unit)
) {
    Scaffold(
        topBar = {
            if (settings.alwaysShowToolbar) {
                CatalogTopAppBar(
                    selectedSample = sample,
                    onExpand = onExpand,
                    onBackClick = onBackClick
                )
            }
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}
