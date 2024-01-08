package com.jevy.ecs.processor;

import com.google.auto.service.*;
import com.jevy.ecs.annotation.*;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.util.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

// http://hannesdorfmann.com/annotation-processing/annotationprocessing101/
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class FunctionalSystemProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return doProcess(roundEnv);
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(FunctionalSystem.class, Filter.class, ScanSystems.class, Order.class, Label.class,
                        SystemSet.class)
                .stream().map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }

    private boolean doProcess(RoundEnvironment roundEnv) throws ProcessingException, IOException {
        List<AnnotatedFunctionalSystem> systems = roundEnv.getElementsAnnotatedWith(FunctionalSystem.class)
                .stream()
                .map(this::checkElementValid)
                .map(ExecutableElement.class::cast)
                .map(AnnotatedFunctionalSystem::new)
                .sorted(new AnnotationFunctionalSystemComparator())
                .toList();
        if (!systems.isEmpty()) {
            SystemRunnerGenerator generator = new SystemRunnerGenerator(typeUtils, elementUtils, processingEnv);
            for (AnnotatedFunctionalSystem system : systems) {
                JavaFile file = generator.generate(system);
                file.writeTo(filer);
            }

            new SchedulerInitializerGenerator(systems)
                    .generate()
                    .writeTo(filer);
            return true;
        }

        return false;
    }

    private Element checkElementValid(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            // TODO adds checks on methods signature (is public, has supported types as arguments...)
            error(element, "Only methods can be annotated with @%s", FunctionalSystem.class.getSimpleName());
        }
        return element;
    }

    private void error(Element e, String msg, Object... formatArgs) {
        messager.printError(msg.formatted(formatArgs), e);
    }
}
