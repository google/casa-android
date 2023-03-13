/*
 * Copyright 2023 Google LLC
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

import android.os.Build
import com.google.android.catalog.framework.base.CatalogSample
import java.util.Collections

/**
 * Defines certain behavior for the Catalog app UI.
 *
 * @param filters a list of filters to include in the home top bar
 * @param order the order to use when sorting the catalog samples list
 * @param alwaysShowToolbar true to show the toolbar inside the samples or false to hide it
 */
data class CatalogSettings(
    val filters: List<CatalogFilter> = listOf(CatalogFilter.Path(), CatalogFilter.Tag),
    val order: CatalogOrder = CatalogOrder.Name(),
    val alwaysShowToolbar: Boolean = true,
    val cardAppearance: CatalogCardAppearance = CatalogCardAppearance(),
)

/**
 * Defines the comparator to use when sorting the list of samples for the main screen
 */
sealed interface CatalogOrder : Comparator<CatalogSample> {

    /**
     * No order, use the auto-generated one
     */
    object None : CatalogOrder {
        override fun compare(sample1: CatalogSample, sample2: CatalogSample): Int = 0
    }

    /**
     * Order samples by name (ascending by default)
     *
     * @param asc true to order them ascending, false otherwise
     */
    data class Name(val asc: Boolean = true) : CatalogOrder {
        override fun compare(sample1: CatalogSample, sample2: CatalogSample): Int {
            val compareBy = compareBy<CatalogSample> { it.name }.run {
                if (asc) this
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.reversed()
                } else {
                    Collections.reverseOrder(this)
                }
            }
            return compareBy.compare(sample1, sample2)
        }
    }

    /**
     * Define your own comparator
     *
     * @param comparator the comparator to use when ordering the list of CatalogSample
     */
    data class Custom(val comparator: Comparator<CatalogSample>) : CatalogOrder {
        override fun compare(
            sample1: CatalogSample,
            sample2: CatalogSample
        ): Int = comparator.compare(sample1, sample2)
    }
}

/**
 * Define a type of filter to show in the top bar in the home screen
 */
sealed interface CatalogFilter {

    /**
     * Enables filter by sample path (by default only module root)
     *
     * @param depth the depth of the path (e.g depth = 1 would include the first layer of subfolder)
     */
    data class Path(val depth: Int = 0) : CatalogFilter

    /**
     * Enables filter by tag specified by the samples.
     */
    object Tag : CatalogFilter
}

/**
 * Defines the appearance of the catalog lists cards
 *
 * @param description defines the amount of lines the description can take. No limit by default.
 * @param tags show or not the tags
 * @param owners show or not the owners
 */
data class CatalogCardAppearance(
    val description: Int = Int.MAX_VALUE,
    val tags: Boolean = true,
    val owners: Boolean = true
)
