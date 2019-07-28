package com.ch629.kotlin_builder.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

// Decorator wrappers to provide extra information about java Elements.
sealed class KotlinTypeElement

data class KotlinClass(
  val originalElement: TypeElement,
  val name: String,
  val constructors: Set<KotlinFunction>,
  val functions: Set<KotlinFunction>,
  val `package`: String,
  val className: ClassName,
  val companion: TypeElement?
) :
  KotlinTypeElement()

data class KotlinFunction(val originalElement: ExecutableElement, val name: String, val parameters: Set<KotlinParameter>) : KotlinTypeElement()
data class KotlinParameter(val originalElement: VariableElement, val name: String, val type: KotlinType, val defaultValue: String) : KotlinTypeElement()
data class KotlinType(val name: TypeName, val nullable: Boolean) : KotlinTypeElement()
