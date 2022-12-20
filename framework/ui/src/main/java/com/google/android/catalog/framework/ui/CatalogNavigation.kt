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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import com.google.android.catalog.framework.ui.components.FragmentContainer

private const val HOME_DESTINATION = "home"

@Composable
fun CatalogNavigation(samples: Set<CatalogSample>, fragmentManager: FragmentManager) {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = HOME_DESTINATION
    ) {
        // Add the home destination
        composable(HOME_DESTINATION) {
            CatalogScreen(samples.toList()) {
                navController.navigate(it.name)
            }
        }

        // Add all the samples
        samples.forEach { sample ->
            addTargets(sample, fragmentManager)
        }
    }
}

private fun NavGraphBuilder.addTargets(sample: CatalogSample, fragmentManager: FragmentManager) {
    when (val target = sample.target) {
        is CatalogTarget.TargetComposable -> {
            composable(sample.name) {
                target.composable()
            }
        }

        is CatalogTarget.TargetFragment -> {
            composable(sample.name) {
                FragmentContainer(
                    modifier = Modifier.fillMaxSize(),
                    fragmentManager = fragmentManager,
                    commit = { id ->
                        add(id, target.targetClass.java.newInstance())
                    }
                )
            }
        }

        is CatalogTarget.TargetActivity -> {
            activity(sample.name) {
                label = sample.name
                activityClass = target.targetClass
            }
        }
    }
}
