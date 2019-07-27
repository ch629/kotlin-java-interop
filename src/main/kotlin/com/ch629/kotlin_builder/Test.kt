package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.ch629.kotlin_builder.annotations.DefaultValue

@Builder
data class TestData(@DefaultValue("Hello") val name: String? = "Hello", val test: Int = 1, val c: Boolean = false) {
  companion object
}

fun main() {
  println(TestData.builder().name("Name").test(5).c(true).build())

  println(TestData::class.qualifiedName)
}
