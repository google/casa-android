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

package com.google.android.catalog.framework.base

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

sealed class CatalogTarget {
    data class TargetActivity(val targetClass: KClass<out Activity>) : CatalogTarget()
    data class TargetFragment(val targetClass: KClass<out Fragment>) : CatalogTarget()
    class TargetComposable(val composable: @Composable () -> Unit) : CatalogTarget()
}

inline fun <reified T : Activity> targetActivity() = CatalogTarget.TargetActivity(T::class)
inline fun <reified T : Fragment> targetFragment() = CatalogTarget.TargetFragment(T::class)
fun targetComposable(block: @Composable () -> Unit) = CatalogTarget.TargetComposable(block)
