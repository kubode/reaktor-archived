package com.github.kubode.reaktor.compiler

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
class ReaktorAnnotationProcessor : AbstractProcessor() {
//    override fun getSupportedAnnotationTypes(): MutableSet<String> {
//        return mutableSetOf(TestAnnotation::class.java.canonicalName)
//    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
//        roundEnv.getElementsAnnotatedWith(TestAnnotation::class.java)
//        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "$annotations, $roundEnv")
        return true
    }
}
