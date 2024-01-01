package com.amoalla.pongl.test.processor;

import com.amoalla.pongl.test.FunctionalSystem;
import com.amoalla.pongl.test.Query;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
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

        // TODO check if all parameter classes are public
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

    public String runnerName() {
        return "%sSystemRunner".formatted(capitalize(name));
    }

    public boolean hasQuery(Types typeUtils, Elements elementUtils) {
        return parameters.stream()
                .anyMatch(elt -> Utils.isAssignable(typeUtils, elementUtils, elt, Query.class));
    }
    public VariableElement getQuery(Types typeUtils, Elements elementUtils) throws ProcessingException {
        if (!hasQuery(typeUtils, elementUtils)) {
            throw new ProcessingException(null, "System %s does not have queries. Check if it hasQueries before calling getQuery.", name);
        }
        List<? extends VariableElement> queries = parameters.stream()
                .filter(elt -> Utils.isAssignable(typeUtils, elementUtils, elt, Query.class))
                .toList();
        if (queries.size() != 1) {
            throw new ProcessingException(null, "Cannot have multiple Query parameters in system " + name);
        }
        return queries.getFirst();
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
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
