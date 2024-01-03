package com.jevy.ecs.processor;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static com.jevy.ecs.ScheduleInitializer.SCHEDULER_INITIALIZER_PACKAGE;
import static com.jevy.ecs.processor.SchedulerInitializerGenerator.scheduleParam;
import static com.jevy.ecs.processor.SchedulerInitializerGenerator.world;

public class ScanSystemsHandler {

    private static final ParameterizedTypeName optionalString = ParameterizedTypeName.get(Optional.class, String.class);

    private final List<AnnotatedFunctionalSystem> annotatedSystems;
    private final List<ClassName> standaloneClasses;

    public ScanSystemsHandler(List<AnnotatedFunctionalSystem> annotatedSystems) {
        this.annotatedSystems = annotatedSystems;
        standaloneClasses = annotatedSystems.stream()
                .filter(AnnotatedFunctionalSystem::isEnclosingClassStandalone)
                .map(AnnotatedFunctionalSystem::enclosingClassName)
                .distinct()
                .toList();
    }

    public void handleStandaloneClasses(TypeSpec.Builder clazz, MethodSpec.Builder initialize) {
        // private static final Set<String> standaloneClassNames = Set.of(...)
        addClassNamesSet(clazz);

        // private String getCallingStandaloneClassName()
        MethodSpec getCallingStandaloneClassName = addGetCallingStandaloneClassName(clazz);

        initialize.addStatement("$T callingStandaloneClassNameOpt = $N()", optionalString, getCallingStandaloneClassName)
                .beginControlFlow("if (callingStandaloneClassNameOpt.isPresent())")
                .addStatement("String callingStandaloneClassName = callingStandaloneClassNameOpt.get()");

        boolean firstIteration = true;
        for (int i = 0; i < standaloneClasses.size(); i++) {
            ClassName className = standaloneClasses.get(i);
            Map<TypeName, List<AnnotatedFunctionalSystem>> systemsBySchedule = annotatedSystems.stream()
                    .filter(system -> system.enclosingClassName().equals(className))
                    .collect(Collectors.groupingBy(AnnotatedFunctionalSystem::schedule));
            if (firstIteration) {
                initialize.beginControlFlow("if (callingStandaloneClassName.equals($S))", className.canonicalName());
                firstIteration = false;
            } else {
                initialize.nextControlFlow("else if (callingStandaloneClassName.equals($S))", className.canonicalName());
            }
            addSystemPerSchedule(initialize, systemsBySchedule);
        }
        initialize.endControlFlow();
        initialize.nextControlFlow("else");
            Map<TypeName, List<AnnotatedFunctionalSystem>> systemsBySchedule = annotatedSystems.stream()
                    .collect(Collectors.groupingBy(AnnotatedFunctionalSystem::schedule));
        addSystemPerSchedule(initialize, systemsBySchedule);
        initialize.endControlFlow();
    }

    private static void addSystemPerSchedule(MethodSpec.Builder initialize, Map<TypeName, List<AnnotatedFunctionalSystem>> systemsBySchedule) {
        boolean firstIteration = true;
        for (TypeName schedule : systemsBySchedule.keySet()) {
            List<AnnotatedFunctionalSystem> systems = systemsBySchedule.get(schedule);
            if (firstIteration) {
                initialize.beginControlFlow("if ($N.label().equals($T.class))", scheduleParam, schedule);
                firstIteration = false;
            } else {
                initialize.nextControlFlow("else if ($N.label().equals($T.class))", scheduleParam, schedule);
            }
            for (AnnotatedFunctionalSystem system : systems) {
                initialize.addStatement("$N.addSystem(new $T($N))", scheduleParam,
                        ClassName.get(SCHEDULER_INITIALIZER_PACKAGE, system.runnerName()), world);
            }
        }
        initialize.endControlFlow();
    }

    // private static final Set<String> standaloneClassNames = Set.of(...)
    private void addClassNamesSet(TypeSpec.Builder clazz) {
        String classNames = standaloneClasses.stream()
                .map(className -> "\"" + className.canonicalName() + "\"")
                .collect(Collectors.joining(", "));

        FieldSpec standaloneClassNames = FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class),
                        "standaloneClassNames", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.of(%s)".formatted(classNames), Set.class)
                .build();
        clazz.addField(standaloneClassNames);
    }

    // private String getCallingStandaloneClassName()
    private static MethodSpec addGetCallingStandaloneClassName(TypeSpec.Builder clazz) {
        MethodSpec getCallingStandaloneClassName = MethodSpec.methodBuilder("getCallingStandaloneClassName")
                .returns(optionalString)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addCode("""
                        return $T.stream(Thread.currentThread().getStackTrace())
                                .map(StackTraceElement::getClassName)
                                .filter(standaloneClassNames::contains)
                                .findFirst();
                        """, Arrays.class)
                .build();
        clazz.addMethod(getCallingStandaloneClassName);
        return getCallingStandaloneClassName;
    }
}
