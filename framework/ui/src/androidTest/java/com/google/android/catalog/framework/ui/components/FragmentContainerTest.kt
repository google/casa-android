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

package com.google.android.catalog.framework.ui.components

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.catalog.framework.ui.TestActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class FragmentContainerTest {

    @get:Rule
    val rule = createAndroidComposeRule<TestActivity>()

    @Test
    fun fragmentLifecycle() {
        rule.onNodeWithText("LifecycleFragment").performClick()
        rule.waitForIdle()
        val fragments = rule.activity.supportFragmentManager.fragments
        assertThat(fragments).hasSize(1)
        assertThat(fragments[0]).isInstanceOf(LifecycleFragment::class.java)
        onView(withText("hello lifecycle")).check(matches(isDisplayed()))
        val fragment = fragments[0] as LifecycleFragment
        assertThat(fragment.created).isTrue()
        assertThat(fragment.started).isTrue()
        assertThat(fragment.resumed).isTrue()
        pressBack()
        rule.waitForIdle()
        assertThat(fragment.resumed).isFalse()
        assertThat(fragment.started).isFalse()
        assertThat(fragment.created).isFalse()
    }

    @Test
    fun orientationChange() {
        rule.onNodeWithText("LifecycleFragment").performClick()
        rule.waitForIdle()
        // Switch to portrait. It's probably portrait right from the start.
        rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rule.waitForIdle()
        onView(withText("hello lifecycle")).check(matches(isDisplayed()))
        // Switch to landscape.
        rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        rule.waitForIdle()
        onView(withText("hello lifecycle")).check(matches(isDisplayed()))
        // Back to portrait.
        rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rule.waitForIdle()
        onView(withText("hello lifecycle")).check(matches(isDisplayed()))
    }
}
