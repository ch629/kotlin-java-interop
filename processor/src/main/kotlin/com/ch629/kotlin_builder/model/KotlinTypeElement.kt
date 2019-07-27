package com.ch629.kotlin_builder.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.TypeElement

sealed class KotlinTypeElement

data class KotlinClass(
  val name: String,
  val constructors: Set<KotlinFunction>,
  val functions: Set<KotlinFunction>,
  val `package`: String,
  val className: ClassName,
  val companion: TypeElement?
) :
  KotlinTypeElement()

data class KotlinFunction(val name: String, val parameters: Set<KotlinParameter>) : KotlinTypeElement()
data class KotlinParameter(val name: String, val type: KotlinType, val defaultValue: String) : KotlinTypeElement()
data class KotlinType(val name: TypeName, val nullable: Boolean) : KotlinTypeElement()
