package com.travelsdk.activityargs

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.travelsdk.activityargs.ActivityArgsProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.lang.model.type.TypeMirror



object ProcessorUtils {

    private lateinit var processingEnvironment: ProcessingEnvironment

    fun init(environment: ProcessingEnvironment) {
        processingEnvironment = environment
    }

    fun logError(message: String) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    fun generateClass(fileName: String, className: String, packageName: String, typeSpec: TypeSpec) {
        val kaptKotlinGeneratedDir = processingEnvironment.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = FileSpec.builder(packageName, fileName)
            .addType(typeSpec)
            .build()

        file.writeTo(File(kaptKotlinGeneratedDir, fileName))
    }

    fun generateFile(fileSpec: FileSpec) {
        val kaptKotlinGeneratedDir = processingEnvironment.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        fileSpec.writeTo(File(kaptKotlinGeneratedDir, fileSpec.name))
    }

    fun isParcelable(typeMirror: TypeMirror): Boolean {
        val parcelable = processingEnvironment.elementUtils
            .getTypeElement("android.os.Parcelable").asType()

        return processingEnvironment.typeUtils.isAssignable(typeMirror, parcelable)
    }
}