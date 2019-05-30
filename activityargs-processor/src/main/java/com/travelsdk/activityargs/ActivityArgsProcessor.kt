package com.travelsdk.activityargs

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.travelsdk.annotation.ActivityArgs
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(ActivityArgsProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ActivityArgsProcessor : AbstractProcessor() {

    private val intentFunSpecs = ArrayList<FunSpec>()
    private val dslExtensionSpec = ArrayList<FunSpec>()
    private val argNameSpecs = ArrayList<String>()

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val PACKAGE_NAME = "com.travelsdk.activityargs"

        private const val NEW_INTENT_PREFIX = "create"
        private const val NEW_INTENT_SUFFIX = "Intent"

        private const val CONTEXT_PARAM_NAME = "context"
        private const val DATA_PARAM_NAME = "data"

        private const val BLOCK_PARAM_NAME = "block"
        private const val ARG_DSL_FILE_NAME = "ArgDsl.kt"

        private const val CLASS_SUFFIX = "::class.java"
        private const val CLASS_NAME_ACTIVITY_NAVIGATOR = "ActivityNavigator"

        private val intentClass = ClassName.bestGuess("android.content.Intent")
        private val contextClass = ClassName.bestGuess("android.content.Context")
    }

    override fun getSupportedAnnotationTypes() =
        mutableSetOf(ActivityArgs::class.java.name)

    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)

        ProcessorUtils.init(processingEnvironment!!)
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (!processActivityArgs(roundEnvironment)) {
            return false
        }

        createIntent()
        createDsl()

        return true
    }

    private fun processActivityArgs(roundEnvironment: RoundEnvironment?): Boolean {
        val elements = roundEnvironment?.getElementsAnnotatedWith(ActivityArgs::class.java) ?: emptySet()

        if (elements.isNullOrEmpty()) {
            return true
        }

        elements.forEach {
            if (it.kind != ElementKind.CLASS) {
                ProcessorUtils.logError("Annotation ${ActivityArgs::class.java.name} can be used with classes")

                return false
            }

            if (!generateIntentFun(it as TypeElement)) {
                return false
            }

        }

        return true
    }


    private fun generateIntentFun(element: TypeElement): Boolean {
        val funSpecBuilder = FunSpec.builder("$NEW_INTENT_PREFIX${element.simpleName}$NEW_INTENT_SUFFIX")
            .addModifiers(KModifier.PUBLIC)
            .addParameter(CONTEXT_PARAM_NAME, contextClass)
            .returns(intentClass)

        funSpecBuilder
            .addStatement("val intent = %T(%L, %L)",
                intentClass, CONTEXT_PARAM_NAME, "${element.qualifiedName}$CLASS_SUFFIX")

        try {
            val dataClass = element.getAnnotation(ActivityArgs::class.java).data
            val paramName = "$DATA_PARAM_NAME${dataClass.asClassName().simpleName}"

            funSpecBuilder.addParameter(paramName, dataClass)
            argNameSpecs.add("ARG_${element.simpleName.toString().toUpperCase()}")
        } catch (exception: MirroredTypeException) {
            val dataClassMirror = exception.typeMirror as DeclaredType
            val typeElement = dataClassMirror.asElement() as TypeElement

            val paramName = "$DATA_PARAM_NAME${typeElement.simpleName}"
            argNameSpecs.add("ARG_${element.simpleName.toString().toUpperCase()}")

            funSpecBuilder.addParameter(paramName, dataClassMirror.asTypeName())
            funSpecBuilder.addStatement("intent.putExtra(%S, %L)",
                "ARG_${element.simpleName.toString().toUpperCase()}", paramName)

            generateDsl(typeElement)

        }

        funSpecBuilder.addStatement("return intent")

        intentFunSpecs.add(funSpecBuilder.build())

        return true
    }

    private fun generateDsl(element: TypeElement) {
        val lambda = LambdaTypeName.get(
            receiver = element.asClassName(),
            parameters = *arrayOf(TypeVariableName("")),
            returnType = Unit::class.asClassName()
        )

        val dslSpecBuilder = FunSpec.builder(element.asClassName().simpleName.decapitalize())
            .returns(element.asClassName())
            .addParameter(BLOCK_PARAM_NAME, lambda)
            .addStatement("return %T().apply($BLOCK_PARAM_NAME)", element)

        dslExtensionSpec.add(dslSpecBuilder.build())
    }

    private fun createIntent() {
        try {
            val typeSpecBuilder = TypeSpec.objectBuilder(CLASS_NAME_ACTIVITY_NAVIGATOR)

            argNameSpecs.forEach {
                typeSpecBuilder.addProperty(PropertySpec.builder(it, String::class, KModifier.CONST)
                    .initializer("\"$it\"").build())
            }

            intentFunSpecs.forEach {
                typeSpecBuilder.addFunction(it)
            }

            ProcessorUtils.generateClass(CLASS_NAME_ACTIVITY_NAVIGATOR,
                CLASS_NAME_ACTIVITY_NAVIGATOR, PACKAGE_NAME, typeSpecBuilder.build())
        } catch (exception: IOException) {
            ProcessorUtils.logError(exception.message!!)
        }
    }

    private fun createDsl() {
        try {
            val dslSpecBuilder = FileSpec.builder(PACKAGE_NAME, ARG_DSL_FILE_NAME)

            dslExtensionSpec.forEach {
                dslSpecBuilder.addFunction(it)
            }

            ProcessorUtils.generateFile(dslSpecBuilder.build())
        } catch (exception: IOException) {
            ProcessorUtils.logError(exception.message!!)
        }
    }
}