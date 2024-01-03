package com.jevy.ecs.processor;


import com.jevy.ecs.ECSWorld;
import com.jevy.ecs.Schedule;
import com.jevy.ecs.ScheduleInitializer;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static com.jevy.ecs.ScheduleInitializer.SCHEDULER_INITIALIZER_CLASS_NAME;
import static com.jevy.ecs.ScheduleInitializer.SCHEDULER_INITIALIZER_PACKAGE;

public class SchedulerInitializerGenerator {

    private static final ParameterSpec scheduleParam = ParameterSpec.builder(Schedule.class, "schedule").build();
    private static final ParameterSpec world = ParameterSpec.builder(ECSWorld.class, "world").build();

    public JavaFile generate(List<AnnotatedFunctionalSystem> annotatedSystems) {
        // final class DefaultScheduleInitializer implements ScheduleInitializer
        TypeSpec.Builder clazz = TypeSpec.classBuilder(SCHEDULER_INITIALIZER_CLASS_NAME)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ScheduleInitializer.class);

        // private void initializeWorld(ECSWorld world)
        MethodSpec.Builder initializeWorldBuilder = MethodSpec.methodBuilder("initializeWorld")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addParameter(world);
        annotatedSystems.stream()
                .map(AnnotatedFunctionalSystem::enclosingClassName)
                .distinct()
                .forEach(enclosingClassName ->
                        initializeWorldBuilder.addStatement("$N.addResource(new $T())", world, enclosingClassName));
        MethodSpec initializeWorld = initializeWorldBuilder.build();
        clazz.addMethod(initializeWorld);

        // private boolean worldInitialized
        FieldSpec worldInitialized = FieldSpec.builder(boolean.class, "worldInitialized", Modifier.PRIVATE)
                .build();
        clazz.addField(worldInitialized);

        // public void initialize(ECSWorld world, Schedule schedule)
        MethodSpec.Builder initialize = MethodSpec.methodBuilder("initialize")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(world)
                .addParameter(scheduleParam)
                .beginControlFlow("if (!$N)", worldInitialized)
                .addStatement("$N($N)", initializeWorld, world)
                .addStatement("$N = true", worldInitialized)
                .endControlFlow();

        if (hasStandaloneClasses(annotatedSystems)) {
            handleStandaloneClasses(clazz, initialize, annotatedSystems);
        } else {
            Map<TypeName, List<AnnotatedFunctionalSystem>> systemsBySchedule = annotatedSystems.stream()
                    .collect(Collectors.groupingBy(AnnotatedFunctionalSystem::schedule));

            systemsBySchedule.forEach((schedule, systems) -> {
                initialize.beginControlFlow("if ($N.label().equals($T.class))", scheduleParam, schedule);
                for (AnnotatedFunctionalSystem system : systems) {
                    initialize.addStatement("$N.addSystem(new $T($N))", scheduleParam,
                            ClassName.get(SCHEDULER_INITIALIZER_PACKAGE, system.runnerName()), world);
                }
                initialize.endControlFlow();
            });
        }
        clazz.addMethod(initialize.build());

        return JavaFile.builder(SCHEDULER_INITIALIZER_PACKAGE, clazz.build()).build();
    }

    private void handleStandaloneClasses(TypeSpec.Builder clazz, MethodSpec.Builder initialize, List<AnnotatedFunctionalSystem> annotatedSystems) {
        // private static final Set<String> standaloneClassNames = Set.of(...)
        String classNames = annotatedSystems.stream()
                .filter(AnnotatedFunctionalSystem::isEnclosingClassStandalone)
                .map(AnnotatedFunctionalSystem::enclosingClassName)
                .distinct()
                .map(className -> "\"" + className.canonicalName() + "\"")
                .collect(Collectors.joining(", "));

        FieldSpec standaloneClassNames = FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class),
                        "standaloneClassNames", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.of(%s)".formatted(classNames), Set.class)
                .build();
        clazz.addField(standaloneClassNames);

        // private String getCallingStandaloneClassName()
        ParameterizedTypeName optionalString = ParameterizedTypeName.get(Optional.class, String.class);
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

        initialize.addStatement("$T callingStandaloneClassNameOpt = $N()", optionalString, getCallingStandaloneClassName)
                .beginControlFlow("if (callingStandaloneClassNameOpt.isPresent())")
                .addStatement("String callingStandaloneClassName = callingStandaloneClassNameOpt.get()");

        List<ClassName> standaloneClasses = annotatedSystems.stream()
                .filter(AnnotatedFunctionalSystem::isEnclosingClassStandalone)
                .map(AnnotatedFunctionalSystem::enclosingClassName)
                .distinct()
                .toList();
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

            boolean scheduleFirstIteration = true;
            for (TypeName schedule : systemsBySchedule.keySet()) {
                List<AnnotatedFunctionalSystem> systems = systemsBySchedule.get(schedule);
                if (scheduleFirstIteration) {
                    initialize.beginControlFlow("if ($N.label().equals($T.class))", scheduleParam, schedule);
                    scheduleFirstIteration = false;
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
        initialize.endControlFlow();
        initialize.nextControlFlow("else");
        Map<TypeName, List<AnnotatedFunctionalSystem>> systemsBySchedule = annotatedSystems.stream()
                .collect(Collectors.groupingBy(AnnotatedFunctionalSystem::schedule));

        firstIteration = true;
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
        initialize.endControlFlow();
    }

    private boolean hasStandaloneClasses(List<AnnotatedFunctionalSystem> annotatedSystems) {
        return annotatedSystems.stream()
                .anyMatch(AnnotatedFunctionalSystem::isEnclosingClassStandalone);
    }
}
