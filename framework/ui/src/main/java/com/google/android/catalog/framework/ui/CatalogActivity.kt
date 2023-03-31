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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.google.android.catalog.framework.base.CatalogSample
import com.google.android.catalog.framework.ui.components.LocalWindowSize
import com.google.android.catalog.framework.ui.theme.CatalogTheme
import javax.inject.Inject

/**
 * Entry point for the samples catalog.
 *
 * How to use it:
 *
 * ```
 * @HiltAndroidApp
 * class MainApp : Application()
 *
 * @AndroidEntryPoint
 * class MainActivity : CatalogActivity()
 * ```
 */
open class CatalogActivity : AppCompatActivity() {

    companion object {
        /**
         * Key to retrieve the start destination from the launching intent.
         *
         * For example, you can start a sample by passing `-estart "Compose Sample"` to the am start
         * command
         */
        const val KEY_START = "start"
    }

    @Inject
    lateinit var catalogSamples: Set<CatalogSample>

    /**
     * Override this field to customize the certain aspects of the UI.
     *
     * @see CatalogSettings
     */
    open val settings: CatalogSettings = CatalogSettings()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure that the declaring activity theme don't show an actionbar
        actionBar?.hide()

        val startDestination = getStartDestination()

        setContent {
            CatalogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sizeClass = calculateWindowSizeClass(this)
                    CompositionLocalProvider(LocalWindowSize provides sizeClass) {
                        CatalogNavigation(
                            startDestination = startDestination,
                            samples = catalogSamples,
                            settings = settings,
                            fragmentManager = supportFragmentManager
                        )
                    }
                }
            }
        }
    }

    /**
     * Get the starting destination from the launching intent or the home screen if not found.
     */
    private fun getStartDestination(): String {
        val value = intent.getStringExtra(KEY_START).orEmpty()
        if (value.isEmpty()) {
            return CATALOG_DESTINATION
        }

        return catalogSamples.find {
            it.route == value || it.name.equals(value, ignoreCase = true)
        }?.route ?: CATALOG_DESTINATION
    }
}
