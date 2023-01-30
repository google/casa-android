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

data class CatalogSample(
    val name: String,
    val description: String,
    val tags: List<String>,
    val documentation: String,
    val sourcePath: String,
    val path: String,
    val owners: List<String>,
    val target: CatalogTarget,
    val minSDK: Int = 0,
    val route: String,
)
