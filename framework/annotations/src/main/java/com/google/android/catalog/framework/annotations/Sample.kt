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

package com.google.android.catalog.framework.annotations

import dagger.hilt.GeneratesRootInput

/**
 * Add this annotation to a parameterless `@Compose` function, fragment or activity classes to
 * create a new sample entry-point that will be automatically included in the Catalog app.
 *
 * @param name a name to use when displaying the sample
 * @param description a description to use when displaying the sample
 * @param tags keywords or labels to use when displaying the sample
 * @param documentation an optional documentation link, it can be absolute or relative to the
 * provided `documentation_base_url` resource ID.
 * @param sourcePath an optional source code path, it can be absolute or relative to the
 * provided `source_base_url` resource ID.
 * @param owners an optional owners information, it can be used to show who proposed the sample
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@GeneratesRootInput
annotation class Sample(
    val name: String,
    val description: String,
    val tags: Array<String> = [],
    val documentation: String = "",
    val sourcePath: String = "",
    val owners: Array<String> = []
)
