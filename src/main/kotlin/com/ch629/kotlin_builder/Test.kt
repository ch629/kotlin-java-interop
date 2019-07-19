package com.ch629.kotlin_builder

import com.example.TestDataBuilder

@Builder
data class TestData(val name: String, val test: Int, val c: Boolean)

fun main() {
    println(TestDataBuilder().name("Name").test(5).c(true).build())
}
