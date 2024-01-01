package com.amoalla.pongl.test.processor;

import com.amoalla.pongl.test.ECSWorld;
import com.amoalla.pongl.test.Schedule;
import com.amoalla.pongl.test.ScheduleInitializer;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amoalla.pongl.test.ScheduleInitializer.SCHEDULER_INITIALIZER_CLASS_NAME;
import static com.amoalla.pongl.test.ScheduleInitializer.SCHEDULER_INITIALIZER_PACKAGE;

public class SchedulerInitializerGenerator {

    public JavaFile generate(List<AnnotatedFunctionalSystem> annotatedSystems) {
        // final class DefaultScheduleInitializer implements ScheduleInitializer
        TypeSpec.Builder clazz = TypeSpec.classBuilder(SCHEDULER_INITIALIZER_CLASS_NAME)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ScheduleInitializer.class);

        // private void initializeWorld(ECSWorld world)
        ParameterSpec world = ParameterSpec.builder(ECSWorld.class, "world").build();
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
        ParameterSpec scheduleParam = ParameterSpec.builder(Schedule.class, "schedule").build();
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
        clazz.addMethod(initialize.build());

        return JavaFile.builder(SCHEDULER_INITIALIZER_PACKAGE, clazz.build()).build();
    }
}
