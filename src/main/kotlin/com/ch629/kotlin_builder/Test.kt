package com.ch629.kotlin_builder

import com.ch629.kotlin_builder.annotations.Builder
import com.ch629.kotlin_builder.annotations.DefaultValue
import com.example.TestDataBuilder

@Builder
data class TestData(@DefaultValue("Hello") val name: String? = "Hello", val test: Int, val c: Boolean)

fun main() {
    println(TestDataBuilder().name("Name").test(5).c(true).build())
}
