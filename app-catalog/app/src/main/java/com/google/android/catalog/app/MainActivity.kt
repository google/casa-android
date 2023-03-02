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

package com.google.android.catalog.app

import android.app.Application
import com.google.android.catalog.framework.ui.CatalogActivity
import com.google.android.catalog.framework.ui.CatalogCardAppearance
import com.google.android.catalog.framework.ui.CatalogFilter
import com.google.android.catalog.framework.ui.CatalogOrder
import com.google.android.catalog.framework.ui.CatalogSettings
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application()

@AndroidEntryPoint
class MainActivity : CatalogActivity() {

    /**
     * You can optionally modify certain aspects of the catalog
     */
    override val settings = CatalogSettings(
        filters = listOf(CatalogFilter.Path(), CatalogFilter.Tag),
        order = CatalogOrder.Name(),
        alwaysShowToolbar = true,
        cardAppearance = CatalogCardAppearance(
            description = 1,
            tags = true,
            owners = false
        )
    )
}
