package com.amoalla.pongl.test;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class AnnotatedFunctionalSystem {

    private final String name;
    private final Schedule schedule;
    private final ClassName enclosingClassName;
    private final List<? extends VariableElement> parameters;

    public AnnotatedFunctionalSystem(ExecutableElement executable) {

        name = executable.getSimpleName().toString();

        FunctionalSystem annotation = executable.getAnnotation(FunctionalSystem.class);
        schedule = annotation.value();

        TypeElement enclosingElement = (TypeElement) executable.getEnclosingElement();
        enclosingClassName = ClassName.get(enclosingElement);

        parameters = executable.getParameters();
    }

    public String name() {
        return this.name;
    }

    public Schedule schedule() {
        return this.schedule;
    }

    public ClassName enclosingClassName() {
        return this.enclosingClassName;
    }

    public List<? extends VariableElement> parameters() {
        return this.parameters;
    }
}
