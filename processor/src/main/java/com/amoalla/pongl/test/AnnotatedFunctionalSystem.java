package com.amoalla.pongl.test;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import java.util.List;

public class AnnotatedFunctionalSystem {

    private final String name;
    private final TypeName schedule;
    private final ClassName enclosingClassName;
    private final List<? extends VariableElement> parameters;

    public AnnotatedFunctionalSystem(ExecutableElement executable) {

        name = executable.getSimpleName().toString();

        schedule = extractScheduleFromAnnotation(executable);

        TypeElement enclosingElement = (TypeElement) executable.getEnclosingElement();
        enclosingClassName = ClassName.get(enclosingElement);

        parameters = executable.getParameters();
    }

    public String name() {
        return this.name;
    }

    public TypeName schedule() {
        return this.schedule;
    }

    public ClassName enclosingClassName() {
        return this.enclosingClassName;
    }

    public List<? extends VariableElement> parameters() {
        return this.parameters;
    }

    private TypeName extractScheduleFromAnnotation(ExecutableElement executable) {
        FunctionalSystem annotation = executable.getAnnotation(FunctionalSystem.class);
        try {
            return TypeName.get(annotation.value());
        } catch (MirroredTypeException e) {
            return TypeName.get(e.getTypeMirror());
        }
    }

}
