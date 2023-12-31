package com.jevy.ecs.processor;


import com.jevy.ecs.*;
import com.jevy.ecs.schedule.*;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import java.util.*;
import java.util.stream.*;

import static com.jevy.ecs.schedule.ScheduleInitializer.*;

public class SchedulerInitializerGenerator {

    static final ParameterSpec scheduleParam = ParameterSpec.builder(Schedule.class, "schedule").build();
    static final ParameterSpec world = ParameterSpec.builder(ECSWorld.class, "world").build();

    private final List<AnnotatedFunctionalSystem> annotatedSystems;
    private final ScanSystemsHandler scanSystemsHandler;

    public SchedulerInitializerGenerator(List<AnnotatedFunctionalSystem> annotatedSystems) {
        this.annotatedSystems = annotatedSystems;
        scanSystemsHandler = new ScanSystemsHandler(annotatedSystems);
    }

    public JavaFile generate() {
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
            scanSystemsHandler.handleStandaloneClasses(clazz, initialize);
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

    private boolean hasStandaloneClasses(List<AnnotatedFunctionalSystem> annotatedSystems) {
        return annotatedSystems.stream()
                .anyMatch(AnnotatedFunctionalSystem::isEnclosingClassStandalone);
    }
}
