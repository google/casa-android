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

package com.google.android.catalog.app.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.startup.Initializer
import com.google.android.catalog.framework.annotations.Sample

@Sample(name = "Activity sample", description = "A sample that uses an Activity as target")
class ActivitySample : ComponentActivity(R.layout.activity_sample) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivitySampleDependency.instance.doSomething()
    }
}

/**
 * Example on how to initialize a component required by this sample without having to access root
 * application class
 */
class ActivitySampleInitializer : Initializer<ActivitySampleDependency> {
    override fun create(context: Context): ActivitySampleDependency {
        return ActivitySampleDependency.init()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // Add any dependencies needed in the create method
        return emptyList()
    }
}

/**
 * Dummy component to showcase dependency initialization for this sample
 */
class ActivitySampleDependency private constructor() {
    companion object {

        lateinit var instance: ActivitySampleDependency
        fun init(): ActivitySampleDependency {
            instance = ActivitySampleDependency()
            return instance
        }
    }

    fun doSomething() = println("Hello!")
}