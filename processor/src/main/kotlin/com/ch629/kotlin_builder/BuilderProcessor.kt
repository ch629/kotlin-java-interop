package com.ch629.kotlin_builder

import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import java.io.File
import java.lang.StringBuilder
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

var env: ProcessingEnvironment? = null

@SupportedAnnotationTypes("com.ch629.kotlin_builder.Builder")
@SupportedOptions(BuilderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BuilderProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val genSourceRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: throw Error("NO LOCATION FOUND")
        env = processingEnv

        val sourceRootFile = File(genSourceRoot)
        sourceRootFile.mkdir()

        val newFile = File(sourceRootFile, "Test.kt")
        newFile.createNewFile()

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Builder::class.java) ?: mutableSetOf()
        annotatedElements.forEach { el ->
            if (el.kind != ElementKind.CLASS) return false
            val clazz = el as TypeElement

            val constructor =
                clazz.enclosedElements.firstOrNull { it.kind == ElementKind.CONSTRUCTOR } as? ExecutableElement ?: throw Error("")
            newFile.writeText(
                createBuilder(
                    el,
                    "com.example",
                    clazz.simpleName.toString(),
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
            clazz.addProperty(
                PropertySpec.builder(
                    "_${field.simpleName}",
                    field.asType().toKotlinType().copy(nullable = true),
                    KModifier.PRIVATE
                )
                    .mutable()
                    .initializer("null").build()
            )

            clazz.addFunction(
                FunSpec.builder(field.simpleName.toString())
                    .addParameter(
                        ParameterSpec.builder(
                            field.simpleName.toString(),
                            field.asType().toKotlinType()
                        ).build()
                    )
                    .addCode(
                        """ 
                            |return apply {
                            |   _${field.simpleName} = ${field.simpleName}
                            |}
                            |
                """.trimMargin()
                    )
                    .build()
            )
        }

        val buildFunction = FunSpec.builder("build").returns(classElement.asClassName())

        val params = fields.joinToString(", ") { "${it.simpleName} = _${it.simpleName}!!" }

        buildFunction.addCode(StringBuilder().apply {
            append("return $className(")
            append(params)
            appendln(")")
        }.toString())

        clazz.addFunction(buildFunction.build())

        return FileSpec.builder(packageName, "$builderName.kt").addType(clazz.build()).build()
    }
}

private fun VariableElement.isNullable() = getAnnotation(Nullable::class.java) != null

private val primitiveMap = mapOf<String, TypeName>(
    java.lang.Integer::class.java.typeName to Int::class.asTypeName(),
    java.lang.Boolean::class.java.typeName to Boolean::class.asTypeName(),
    java.lang.String::class.java.typeName to String::class.asTypeName()
)

private fun TypeMirror.toKotlinType() = primitiveMap[asTypeName().toString()] ?: asTypeName()
