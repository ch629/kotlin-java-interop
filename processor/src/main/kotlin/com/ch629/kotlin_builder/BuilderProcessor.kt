package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.squareup.kotlinpoet.*
import java.io.File
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

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Builder::class.java) ?: mutableSetOf()
        annotatedElements.forEach { el ->
            if (el.kind != ElementKind.CLASS) return false
            val clazz = el as TypeElement
            val pkg = processingEnv.elementUtils.getPackageOf(clazz).toString()
            val constructor = clazz.getConstructors().firstOrNull() ?: throw Error("")

            createBuilder(
                el,
                pkg,
                clazz.getName(),
                constructor.parameters
            ).writeTo(sourceRootFile)

        }

        return false
    }

    fun createBuilder(
        classElement: TypeElement,
        packageName: String,
        className: String,
        fields: List<VariableElement>
    ): FileSpec {
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
                    .initializer(defaultValue)
                    .build()
            )

            clazz.addFunction(
                FunSpec.builder(field.getName())
                    .addParameter(
                        ParameterSpec.builder(
                            originalName,
                            field.asKotlinType().copy(field.isNullable())
                        ).build()
                    )
                    .addStatement("return apply { $builderFieldName = $originalName }")
                    .build()
            )
        }

        val params = fields.joinToString(", ") {
            "${it.getName()} = _${it.getName()}${if (it.isNullable()) "" else "!!"}"
        }

        clazz.addFunction(
            FunSpec.builder("build")
                .returns(classElement.asClassName())
                .addStatement("return $className($params)")
                .build()
        )
        val fileSpec = FileSpec.builder(packageName, builderName).addType(clazz.build())

        val companion = classElement.getCompanion()
        if (companion != null) {
            // JvmStatic can't be applied to extension functions.
            fileSpec.addFunction(
                FunSpec.builder("builder")
                    .receiver(companion.asType().asKotlinType())
                    .addStatement("return $builderName()")
                    .build()
            )
        }

        return fileSpec.build()
    }
}
