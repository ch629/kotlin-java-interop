package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.ch629.kotlin_builder.model.KotlinClass
import com.ch629.kotlin_builder.model.processClass
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

      val genFile = File(sourceRootFile, "${kClass.`package`.replace('.', '\\')}\\${kClass.name}Builder.kt")
      if (!genFile.parentFile.exists()) genFile.parentFile.mkdirs()
      if (genFile.exists()) genFile.delete()
      genFile.createNewFile()
      genFile.writeText(createFileContents(kClass))
    }

    return false
  }

  private fun createFileContents(kClass: KotlinClass): String {
    val decapName = kClass.name.decapitalize()
    val variableListSb = StringBuilder()
    val variableSetterSb = StringBuilder()
    val buildParamSb = StringBuilder()
    val ofFieldSb = StringBuilder()

    kClass.constructors.firstOrNull()?.parameters?.forEach { param ->
      variableListSb.appendln("  private var _${param.name}: ${param.type.name}? = ${param.defaultValue}")
      variableSetterSb.appendln("  fun ${param.name}(${param.name}: ${param.type.name}) = apply { _${param.name} = ${param.name} }")
      buildParamSb.append("${param.name} = _${param.name}${if (param.type.nullable) "" else "!!"}, ")
      ofFieldSb.appendln("      _${param.name} = $decapName.${param.name}")
    }

    return """
      |package ${kClass.`package`}

      |${"// TODO: Imports"}

      |class ${kClass.name}Builder {
      |$variableListSb
      |$variableSetterSb
      |  fun build(): ${kClass.name} = ${kClass.name}(${buildParamSb.dropLast(2)})
  
      |  companion object {
      |    @JvmStatic
      |    fun of($decapName: ${kClass.name}) = ${kClass.name}Builder().apply {
      |${ofFieldSb.dropLast(1)}
      |    }
      |  }
      |}${if (kClass.companion != null) "\n\nfun ${kClass.name}.Companion.builder() = ${kClass.name}Builder()" else ""}
    """.trimMargin()
  }
}
