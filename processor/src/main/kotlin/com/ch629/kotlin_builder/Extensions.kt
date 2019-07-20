package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.DefaultValue
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

// Variables
internal fun VariableElement.isNullable() = getAnnotation(Nullable::class.java) != null
internal fun VariableElement.asKotlinType() = asType().asKotlinType()
internal fun VariableElement.getDefaultValue() = getAnnotation(DefaultValue::class.java)?.value

// Classes
internal fun TypeElement.hasCompanion() =  false

internal fun TypeElement.getConstructors() =
    enclosedElements.filter { it.kind == ElementKind.CONSTRUCTOR }.mapNotNull { it as? ExecutableElement }

internal fun TypeElement.getFunctions() = enclosedElements.filter { it.kind == ElementKind.METHOD }.map { it as ExecutableElement }

internal fun Element.getName() = simpleName.toString()

private val javaTypeMap = mapOf<String, TypeName>(
    java.lang.Integer::class.java.typeName to Int::class.asTypeName(),
    java.lang.Boolean::class.java.typeName to Boolean::class.asTypeName(),
    java.lang.String::class.java.typeName to String::class.asTypeName()
)

internal fun TypeMirror.asKotlinType() = javaTypeMap[asTypeName().toString()] ?: asTypeName()
