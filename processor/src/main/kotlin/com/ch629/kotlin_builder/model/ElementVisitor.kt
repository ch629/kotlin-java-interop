package com.ch629.kotlin_builder.model

import com.ch629.kotlin_builder.asKotlinType
import com.ch629.kotlin_builder.getCompanion
import com.ch629.kotlin_builder.getDefaultValue
import com.ch629.kotlin_builder.name
import com.squareup.kotlinpoet.asClassName
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.*

object ElementVisitor {
  fun visit(element: Element) = when (element.kind) {
    ElementKind.CLASS -> visit(element as TypeElement)
    ElementKind.METHOD, ElementKind.CONSTRUCTOR -> visit(element as ExecutableElement)
    ElementKind.PARAMETER -> visit(element as VariableElement)
    else -> null
  }

  fun visit(element: TypeElement): KotlinClass {
    val constructors = mutableSetOf<KotlinFunction>()
    val functions = mutableSetOf<KotlinFunction>()
    val className = element.asClassName()

    // Using this rather than mapping, as it'll be more efficient than filter & mapping twice (4n vs n here)
    element.enclosedElements.forEach {
      when (it.kind) {
        ElementKind.CONSTRUCTOR -> constructors += visit(it as ExecutableElement)
        ElementKind.METHOD -> functions += visit(it as ExecutableElement)
      }
    }

    return KotlinClass(
      element,
      element.name,
      constructors,
      functions,
      className.packageName,
      className,
      element.getCompanion()
    )
  }

  fun visit(element: ExecutableElement) = KotlinFunction(
    element,
    element.name,
    element.parameters.mapTo(HashSet()) { visit(it) }
  )

  fun visit(element: VariableElement) = KotlinParameter(
    element,
    element.name,
    KotlinType(element.asKotlinType(), element.getAnnotation(Nullable::class.java) != null),
    element.getDefaultValue()
  )
}

