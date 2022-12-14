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

package com.google.android.catalog.framework.plugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class CatalogPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val samples = settings.settingsDir.walk().filter {
            (it.name == "build.gradle" || it.name == "build.gradle.kts") && it.path.contains("/samples/")
        }.map {
            it.parent.substring(settings.settingsDir.path.length).replace("/", ":")
        }.toList()

        println("Printing samples")
        println(samples)

        settings.include(samples)
        // include all available samples and store it in the global variable.
        settings.extensions.extraProperties["samples"] = samples

        settings.gradle.afterProject { project ->
            if (project.plugins.hasPlugin("com.android.application")) {
                println("That's the one!")
                with(project) {

                    /*
                    TODO add hilt ksp kapt plugins
                    with(pluginManager) {
                        apply(com.android.build.gradle.AppPlugin::class.java)
                        apply(org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper::class.java)
                    }*/
                    /*with( dependencies) {
                        //add("")
                        samples.forEach { sample ->
                            add("implementation", project(sample))
                        }
                    }*/
                }
            }
        }

        // TODO:
        //  - Samples variable is not accessible from app-project, find how to
        //  - Find how this plugin can apply the setup for app project or if we need a new plugin
        //  - It should add the hilt/framework dependencies and plugins and the samples
        //  - For each sample project it should also add it
    }
}