package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.ch629.kotlin_builder.model.KotlinClass
import com.ch629.kotlin_builder.model.processClass
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes("com.ch629.kotlin_builder.annotations.Builder")
@SupportedOptions(BuilderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BuilderProcessor : AbstractProcessor() {
  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
  }

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(Builder::class.java.canonicalName)
  }

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val genSourceRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: throw Error("NO LOCATION FOUND")

    val sourceRootFile = File(genSourceRoot)
    sourceRootFile.mkdir()

    val annotatedElements = roundEnv.getElementsAnnotatedWith(Builder::class.java) ?: mutableSetOf()
    annotatedElements.forEach { el ->
      val kClass = processClass(el) ?: return@forEach

      createBuilder(kClass).writeTo(sourceRootFile)
    }

    return false
  }

  fun createBuilder(kClass: KotlinClass): FileSpec {
    val builderName = "${kClass.name}Builder"
    val classBuilder = TypeSpec.classBuilder(builderName)
    val classFieldName = kClass.name.decapitalize()

    val builderFunction = FunSpec.builder("build").returns(kClass.className)
    val paramSb = StringBuilder()
    val ofSb = StringBuilder()

    kClass.constructors.firstOrNull()?.parameters?.forEach { param ->
      val builderFieldName = "_${param.name}"

      classBuilder.addProperty(
        PropertySpec.builder(
          builderFieldName,
          param.type.name.copy(nullable = true),
          KModifier.PRIVATE
        )
          .mutable()
          .initializer(param.defaultValue)
          .build()
      )

      classBuilder.addFunction(
        FunSpec.builder(param.name).addParameter(
          ParameterSpec.builder(
            param.name,
            param.type.name.copy(param.type.nullable)
          ).build()
        )
          .addStatement("return apply { $builderFieldName = ${param.name} }")
          .build()
      )

      paramSb.append("${param.name} = $builderFieldName${if (param.type.nullable) "" else "!!"}, ")
      ofSb.appendln("  $builderFieldName = $classFieldName.${param.name}")
    }

    builderFunction.addStatement("return ${kClass.name}(${paramSb.dropLast(2)})")
    classBuilder.addFunction(builderFunction.build())

    classBuilder.addType(
      TypeSpec.companionObjectBuilder()
        .addFunction(
          FunSpec.builder("of")
            .addAnnotation(JvmStatic::class)
            .addParameter(classFieldName, kClass.className)
            .addStatement("return $builderName().apply {\n$ofSb}")
            .build()
        ).build()
    )

    val fileSpec = FileSpec.builder(kClass.`package`, builderName).addType(classBuilder.build())

    if (kClass.companion != null) {
      fileSpec.addFunction(
        FunSpec.builder("builder")
          .receiver(kClass.companion.asType().asKotlinType())
          .addStatement("return $builderName()")
          .build()
      )
    }

    return fileSpec.build()
  }
}
