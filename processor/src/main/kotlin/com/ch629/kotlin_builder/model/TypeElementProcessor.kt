package com.ch629.kotlin_builder.model

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

fun processClass(element: Element) =
  if (element.kind == ElementKind.CLASS) ElementVisitor.visit(element as TypeElement)
  else null
