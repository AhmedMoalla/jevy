package com.amoalla.pongl.test.processor;

import com.amoalla.pongl.test.Filter;
import com.amoalla.pongl.test.FunctionalSystem;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;
import java.util.Set;

// http://hannesdorfmann.com/annotation-processing/annotationprocessing101/
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({FunctionalSystem.FQN, Filter.FQN})
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
        return true;
    }

    private boolean doProcess(RoundEnvironment roundEnv) throws ProcessingException, IOException {
        List<AnnotatedFunctionalSystem> systems = roundEnv.getElementsAnnotatedWith(FunctionalSystem.class)
                .stream()
                .map(this::checkElementValid)
                .map(ExecutableElement.class::cast)
                .map(AnnotatedFunctionalSystem::new)
                .toList();
        if (!systems.isEmpty()) {
            SystemRunnerGenerator generator = new SystemRunnerGenerator(typeUtils, elementUtils, processingEnv);
            for (AnnotatedFunctionalSystem system : systems) {
                JavaFile file = generator.generate(system);
                file.writeTo(filer);
            }

            new SchedulerInitializerGenerator()
                    .generate(systems)
                    .writeTo(filer);
        }

        return true;
    }

    public Element checkElementValid(Element element) {
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
