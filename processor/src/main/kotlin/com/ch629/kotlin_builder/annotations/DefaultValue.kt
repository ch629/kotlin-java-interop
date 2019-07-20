package com.ch629.kotlin_builder.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(val value: String)
