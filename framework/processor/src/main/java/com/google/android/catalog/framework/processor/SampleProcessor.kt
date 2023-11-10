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

package com.google.android.catalog.framework.processor

import com.google.android.catalog.framework.annotations.Sample
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate

/**
 * This processor search for @Sample annotations in the code and creates a hilt module to inject
 * the PlaygroundSample.
 *
 * Note: it could be improved. Right now it uses a bit of hardcoded values (i.e search for Fragment as String class)
 * also it creates a file for each annotation. Probably it can be optimized.
 */
@OptIn(KspExperimental::class)
class SampleProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val visitor = SampleVisitor(logger, codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(Sample::class.java.name)
            .filter {
                val isValid = it.validate { first, second ->
                    // Skip checking fields of a class since its causing issues with viewBinding
                    !(first is KSClassDeclaration && second is KSPropertyDeclaration)
                }
                if (!isValid) {
                    logger.warn("Annotated sample is not valid ${it.containingFile?.filePath}")
                }
                (it is KSFunctionDeclaration || it is KSClassDeclaration) &&
                    isValid &&
                    !it.isAnnotationPresent(Deprecated::class)
            }
            .forEach { it.accept(visitor, Unit) }

        return emptyList()
    }
}
