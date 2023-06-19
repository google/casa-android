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

import androidx.annotation.RequiresApi
import com.google.android.catalog.framework.annotations.Sample
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
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

    private val classes = mutableListOf<KSClassDeclaration>()
    private val functions = mutableListOf<KSFunctionDeclaration>()

    private val visitor = SampleVisitor(classes, functions)

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

    override fun finish() {
        functions.forEach { declaration ->
            require(
                declaration.annotations.any { it.shortName.asString() == "Composable" } &&
                    declaration.parameters.isEmpty()
            ) {
                "@Sample must be a in a Composable function with empty parameters"
            }

            createModule(
                functionSample = declaration,
                target = "targetComposable { ${declaration.toFullPath()}() }"
            )
        }
        classes.forEach { declaration ->
            val target = declaration.getAllSuperTypes().firstNotNullOfOrNull {
                val className = it.declaration.qualifiedName?.asString().orEmpty()
                logger.warn(className)
                when (className) {
                    "android.app.Activity" -> {
                        "targetActivity<${declaration.toFullPath()}>()"
                    }

                    "androidx.fragment.app.Fragment" -> {
                        "targetFragment<${declaration.toFullPath()}>()"
                    }

                    else -> null
                }
            }
            requireNotNull(target) {
                "@Sample only supports Fragments, Activities or Composable functions"
            }

            createModule(
                functionSample = declaration,
                target = target
            )
        }
    }

    @OptIn(KspExperimental::class)
    private fun createModule(functionSample: KSDeclaration, target: String) {
        val filePath = getRelativeFilePath(functionSample)
        val packageName = functionSample.packageName.asString()
        val sampleFile = functionSample.simpleName.asString()
        val sample = functionSample.getAnnotationsByType(Sample::class).first()
        val sampleSource = sample.sourcePath.ifBlank { filePath }
        val minSDK = functionSample.getAnnotationsByType(RequiresApi::class).minOfOrNull {
            it.value
        } ?: 0

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                sources = (functions + classes).map { it.containingFile!! }.toTypedArray()
            ),
            packageName = packageName,
            fileName = "${sampleFile}Module"
        )
        file.use { stream ->
            stream.write(
                sampleTemplate(
                    sampleFile = sampleFile,
                    samplePackage = packageName,
                    sampleName = sample.name,
                    sampleDescription = sample.description,
                    sampleTags = sample.tags,
                    sampleDocs = sample.documentation,
                    sampleSource = sampleSource,
                    samplePath = filePath.substringBefore("/src"),
                    sampleOwners = sample.owners,
                    sampleTarget = target,
                    sampleMinSdk = minSDK,
                    sampleRoute = "$sampleSource-$sampleFile",
                ).toByteArray()
            )
        }
    }

    private fun KSDeclaration.toFullPath() =
        "${packageName.asString()}.${simpleName.asString()}"

    private fun getRelativeFilePath(declaration: KSDeclaration): String {
        val path = (declaration.location as? FileLocation)?.filePath.orEmpty()
        return path.substringAfterLast("/samples/")
    }
}

private fun sampleTemplate(
    sampleFile: String,
    samplePackage: String,
    sampleName: String,
    sampleDescription: String,
    sampleTags: Array<String>,
    sampleDocs: String,
    sampleSource: String,
    samplePath: String,
    sampleOwners: Array<String>,
    sampleTarget: String,
    sampleMinSdk: Int,
    sampleRoute: String,
) = """
package $samplePackage

import com.google.android.catalog.framework.base.*

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
class ${sampleFile}Module {

    @Provides
    @IntoSet
    fun provide${sampleFile}Sample(): CatalogSample {
        return CatalogSample(
            "$sampleName",
            "$sampleDescription",
            listOf(${sampleTags.joinToString(",") { "\"$it\"" }}),
            "$sampleDocs",
            "$sampleSource",
            "$samplePath",
            listOf(${sampleOwners.joinToString(",") { "\"$it\"" }}),
            $sampleTarget,
            $sampleMinSdk,
            "$sampleRoute",
        )
    }
}
""".trimIndent()
