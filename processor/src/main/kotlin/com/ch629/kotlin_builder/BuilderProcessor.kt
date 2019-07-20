package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.squareup.kotlinpoet.*
import java.io.File
import java.lang.StringBuilder
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@SupportedAnnotationTypes("com.ch629.kotlin_builder.annotations.Builder")
@SupportedOptions(BuilderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BuilderProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val genSourceRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: throw Error("NO LOCATION FOUND")

        val sourceRootFile = File(genSourceRoot)
        sourceRootFile.mkdir()

        val newFile = File(sourceRootFile, "Test.kt")
        newFile.createNewFile()

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Builder::class.java) ?: mutableSetOf()
        annotatedElements.forEach { el ->
            if (el.kind != ElementKind.CLASS) return false
            val clazz = el as TypeElement

            val constructor = clazz.getConstructors().firstOrNull() ?: throw Error("")
            newFile.writeText(
                createBuilder(
                    el,
                    "com.example",
                    clazz.getName(),
                    constructor.parameters
                ).toString()
            )
        }

        return false
    }

    // TODO: Add extension function from original to access the builder easier? i.e. Test.builder().withName("").build()
    fun createBuilder(classElement: TypeElement, packageName: String, className: String, fields: List<VariableElement>): FileSpec {
        val builderName = "${className}Builder"
        val clazz = TypeSpec.classBuilder(builderName)
        fields.forEach { field ->
            val originalName = field.getName()
            val builderFieldName = "_$originalName"
            val defaultValue = field.getDefaultValue()

            clazz.addProperty(
                PropertySpec.builder(
                    builderFieldName,
                    field.asKotlinType().copy(nullable = true),
                    KModifier.PRIVATE
                )
                    .mutable()
                    .initializer(if(defaultValue != null) "\"$defaultValue\"" else "null").build()
            )

            clazz.addFunction(
                FunSpec.builder(field.getName())
                    .addParameter(
                        ParameterSpec.builder(
                            originalName,
                            field.asKotlinType().copy(field.isNullable())
                        ).build()
                    )
                    .addCode(
                        """ 
                            |return apply {
                            |   $builderFieldName = $originalName
                            |}
                            |
                """.trimMargin()
                    )
                    .build()
            )
        }

        val buildFunction = FunSpec.builder("build").returns(classElement.asClassName())

        val params = fields.joinToString(", ") {
            val error = "!!" // if(it.isNullable()) "" else " ?: throw Error(\"${it.getName()} is not nullable\")"
            "${it.getName()} = _${it.getName()}$error"
        }

        val map = fields.associate { it.getName() to "_${it.getName()}!!" }

        // TODO: addStatement
        buildFunction.addCode(StringBuilder().apply {
            append("return $className(")
            append(params)
            appendln(")")
        }.toString())

        clazz.addFunction(buildFunction.build())
        val fileSpec = FileSpec.builder(packageName, "$builderName.kt").addType(clazz.build())

        // TODO: Figure out how to get Companion without it becoming `Companion.builder`() =
        if(classElement.hasCompanion()) {
            fileSpec.addFunction(
                FunSpec.builder("Companion.builder").receiver(classElement.asClassName()).addStatement(
                    "return $builderName()"
                ).build()
            )
        }

        return fileSpec.build()
    }
}
