package com.jevy.ecs.processor;

import com.jevy.ecs.annotation.*;
import com.jevy.ecs.query.*;
import com.jevy.ecs.systemset.*;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import java.util.*;

public class AnnotatedFunctionalSystem {

    private final String name;
    private final TypeName schedule;
    private final ClassName enclosingClassName;
    private final boolean enclosingClassStandalone;
    private final List<? extends VariableElement> parameters;

    private final SystemSetRules rules;

    public AnnotatedFunctionalSystem(ExecutableElement executable) {

        name = executable.getSimpleName().toString();

        schedule = extractScheduleFromAnnotation(executable);

        TypeElement enclosingElement = (TypeElement) executable.getEnclosingElement();
        enclosingClassName = ClassName.get(enclosingElement);
        ScanSystems scanSystems = enclosingElement.getAnnotation(ScanSystems.class);
        enclosingClassStandalone = scanSystems != null
                                   && (scanSystems.packageName() == null || scanSystems.packageName().isEmpty());

        // TODO check if all parameter classes are public
        parameters = executable.getParameters();

        rules = buildSystemSetRules(executable);
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

    public boolean isEnclosingClassStandalone() {
        return this.enclosingClassStandalone;
    }

    public List<? extends VariableElement> parameters() {
        return this.parameters;
    }

    public SystemSetRules rules() {
        return rules;
    }

    public String runnerName() {
        String className = enclosingClassName.canonicalName().replace(".", "_");
        return "%s_%s_SystemRunner".formatted(className, capitalize(name));
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

    private SystemSetRules buildSystemSetRules(ExecutableElement executable) {
        Order order = executable.getAnnotation(Order.class);
        if (order == null) {
            return null;
        }
        SystemSetRules.Builder builder = SystemSetRules.builder();
        for (String after : order.after()) {
            builder.after(after);
        }
        for (String before : order.before()) {
            builder.before(before);
        }
        return builder.build();
    }

}
