package com.travelsdk.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ActivityArgs(val data: KClass<out Any>)