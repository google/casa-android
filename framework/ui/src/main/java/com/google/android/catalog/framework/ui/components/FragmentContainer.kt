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

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

@Composable
internal fun FragmentContainer(
    modifier: Modifier = Modifier,
    fragmentManager: FragmentManager,
    createFragment: () -> Fragment,
) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FragmentContainerView(context).also { view ->
                view.id = containerId
            }
        },
    )
    DisposableEffect(fragmentManager) {
        fragmentManager.commit {
            replace(containerId, createFragment())
        }
        onDispose {
            fragmentManager.findFragmentById(containerId)?.let { fragment ->
                fragmentManager.commit(allowStateLoss = true) {
                    remove(fragment)
                }
            }
        }
    }
}
