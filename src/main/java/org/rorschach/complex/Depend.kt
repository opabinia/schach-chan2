package org.rorschach.complex

@kotlin.annotation.Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Depend(val value: String)