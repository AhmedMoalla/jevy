package com.amoalla.pongl.test;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dev.dominion.ecs.api.Dominion;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.amoalla.pongl.test.WorldRunner.SCHEDULER_INITIALIZER_PACKAGE;
import static com.amoalla.pongl.test.WorldRunner.SCHEDULER_INITIALIZER_CLASS_NAME;
import static javax.lang.model.util.ElementFilter.methodsIn;

// http://hannesdorfmann.com/annotation-processing/annotationprocessing101/
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({FunctionalSystem.FQN, Filter.FQN})
public class FunctionalSystemProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private ScheduleInitializerIntermediaryResult scheduleInitializer;
    private boolean doneProcessing = false;

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
        if (doneProcessing) {
            return true;
        }

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(FunctionalSystem.class);
        for (Element element : annotatedElements) {
            if (element.getKind() != ElementKind.METHOD) {
                // TODO adds checks on methods signature
                throw new ProcessingException(element, "Only methods can be annotated with @%s",
                        FunctionalSystem.class.getSimpleName());
            }

            AnnotatedFunctionalSystem system = new AnnotatedFunctionalSystem((ExecutableElement) element);
            if (scheduleInitializer == null) {
                scheduleInitializer = generateScheduleInitializer(system);
            }

            JavaFile systemRunner = generateSystemRunnerAndUpdateScheduleInitializer(system);
            systemRunner.writeTo(filer);
        }

        if (annotatedElements.isEmpty() && scheduleInitializer != null) {
            TypeSpec clazz = scheduleInitializer.classBuilder
                    .addMethod(scheduleInitializer.initializeBuilder.build())
                    .build();
            JavaFile.builder(SCHEDULER_INITIALIZER_PACKAGE, clazz).build()
                    .writeTo(filer);
            doneProcessing = true;
        }

        return true;
    }

    private JavaFile generateSystemRunnerAndUpdateScheduleInitializer(AnnotatedFunctionalSystem system) {

        List<FieldSpec> fields = new ArrayList<>();
        FieldSpec parent = FieldSpec.builder(system.enclosingClassName(), "parent", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addParameter(scheduleInitializer.world)
                .addStatement("this.$N = $N.getResource($T.class)", parent, scheduleInitializer.world, system.enclosingClassName());
//                .addStatement("this.$N = new $T()", parent, system.enclosingClassName());

        FieldSpec dominion = FieldSpec.builder(Dominion.class, "dominion", Modifier.PRIVATE, Modifier.FINAL).build();

        Optional<Map<String, List<ClassName>>> filters = null;
        FieldSpec queryField = null;
        TypeName[] queryTypeParameters = null;
        boolean addDominionField = false;
        for (VariableElement parameter : system.parameters()) {
            TypeMirror type = parameter.asType();
            TypeElement element = (TypeElement) typeUtils.asElement(type);
            boolean isQuery = element.getInterfaces().stream()
                    .map(TypeName::get)
                    .anyMatch(name -> name.equals(TypeName.get(Query.class)));

            FieldSpec field;
            if (isQuery) {
                if (queryTypeParameters != null) {
                    error(parameter, "Cannot have multiple Query parameters in system " + system.name());
                    break;
                }
                //noinspection UnstableApiUsage
                filters = MoreElements.getAnnotationMirror(parameter, Filter.class)
                        .toJavaUtil()
                        .map(AnnotationMirror::getElementValues)
                        .map(values -> {
                            Map<String, List<ClassName>> result = new HashMap<>();
                            for (ExecutableElement executable : values.keySet()) {
                                List<ClassName> classNames = ((List) values.get(executable).getValue())
                                        .stream().<ClassName>map(obj -> ClassName.bestGuess(obj.toString().replace(".class", "")))
                                        .toList();
                                result.put(executable.getSimpleName().toString(), classNames);
                            }
                            return result;
                        });
                addDominionField = true;
                ClassName queryType = ClassName.get(Query.class.getPackageName(), element.getSimpleName().toString() + "Impl");
                queryTypeParameters = ((DeclaredType) type).getTypeArguments().stream()
                        .map(ClassName::get)
                        .toArray(TypeName[]::new);
                TypeName queryTypeWithGenerics = ParameterizedTypeName.get(queryType, queryTypeParameters);
                field = queryField = FieldSpec.builder(queryTypeWithGenerics, "query", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", queryType)
                        .build();
            } else {
                ClassName className = ClassName.get(element);
                String name = parameter.getSimpleName().toString();
                field = FieldSpec.builder(className, name, Modifier.PRIVATE, Modifier.FINAL).build();
                constructor.addStatement("this.$N = $N.getResource($T.class)", field, scheduleInitializer.world, field.type);
            }
            fields.add(field);
        }

        ExecutableElement superRun = getExecutableElement(SystemRunner.class, "run");
        MethodSpec.Builder run = MethodSpec.overriding(superRun);

        String fieldArgs = fields.stream()
                .map(f -> f.name)
                .collect(Collectors.joining(", "));

        if (addDominionField) {
            fields.add(dominion);
            constructor.addStatement("this.$N = $N.dominion()", dominion, scheduleInitializer.world);

            String findArgs = Arrays.stream(queryTypeParameters)
                    .map(param -> "$T.class")
                    .collect(Collectors.joining(", "));
            String code = "var compositions = $N.findCompositionsWith(%s)".formatted(findArgs);

            int nFilterArgs = 0;
            if (filters.isPresent()) {
                if (filters.get().containsKey("with")) {
                    List<ClassName> markerClassNames = filters.get().get("with");
                    nFilterArgs += markerClassNames.size();
                    String withAlsoArgs = markerClassNames.stream()
                            .map(marker -> "$T.class")
                            .collect(Collectors.joining(", "));
                    code += ".withAlso(%s)".formatted(withAlsoArgs);
                }
                if (filters.get().containsKey("without")) {
                    List<ClassName> markerClassNames = filters.get().get("without");
                    nFilterArgs += markerClassNames.size();
                    String withAlsoArgs = markerClassNames.stream()
                            .map(marker -> "$T.class")
                            .collect(Collectors.joining(", "));
                    code += ".without(%s)".formatted(withAlsoArgs);
                }
            }

            Object[] args = new Object[1 + queryTypeParameters.length + nFilterArgs];
            args[0] = dominion;
            System.arraycopy(queryTypeParameters, 0, args, 1, queryTypeParameters.length);
            if (filters.isPresent()) {
                int destPos = 1 + queryTypeParameters.length;
                if (filters.get().containsKey("with")) {
                    ClassName[] markerClassNames = filters.get().get("with").toArray(ClassName[]::new);
                    System.arraycopy(markerClassNames, 0, args, destPos, markerClassNames.length);
                    destPos += markerClassNames.length;
                }
                if (filters.get().containsKey("without")) {
                    ClassName[] markerClassNames = filters.get().get("without").toArray(ClassName[]::new);
                    System.arraycopy(markerClassNames, 0, args, destPos, markerClassNames.length);
                }
            }
            run.addStatement(code, args)
                    .addStatement("$N.setResults(compositions)", queryField);
        }

        run.addStatement("$N.%s(%s)".formatted(system.name(), fieldArgs), parent);


        String className = "%sSystemRunner".formatted(capitalize(system.name()));
        TypeSpec systemRunner = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(SystemRunner.class)
                .addField(parent)
                .addFields(fields)
                .addMethod(constructor.build())
                .addMethod(run.build())
                .build();

        updateScheduleInitializer(system, systemRunner);

        return JavaFile.builder(SCHEDULER_INITIALIZER_PACKAGE, systemRunner)
                .build();
    }

    private void updateScheduleInitializer(AnnotatedFunctionalSystem system, TypeSpec systemRunner) {
        scheduleInitializer.initializeBuilder
                .beginControlFlow("if (schedule == $T.$L)", Schedule.class, system.schedule())
                .addStatement("$N.addSystems(new $T($N))", scheduleInitializer.schedule,
                        ClassName.get(SCHEDULER_INITIALIZER_PACKAGE, systemRunner.name), scheduleInitializer.world)
                .endControlFlow();
    }

    private ScheduleInitializerIntermediaryResult generateScheduleInitializer(AnnotatedFunctionalSystem system) {
        ParameterSpec world = ParameterSpec.builder(ECSWorld.class, "world").build();
        MethodSpec initializeWorld = MethodSpec.methodBuilder("initializeWorld")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addParameter(world)
                .addStatement("$N.addResource(new $T())", world, system.enclosingClassName())
                .build();

        FieldSpec worldInitialized = FieldSpec.builder(boolean.class, "worldInitialized", Modifier.PRIVATE)
                .build();

        ParameterSpec schedule = ParameterSpec.builder(Schedule.class, "schedule").build();
        MethodSpec.Builder initializeBuilder = MethodSpec.methodBuilder("initialize")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(world)
                .addParameter(schedule)
                .beginControlFlow("if (!$N)", worldInitialized)
                .addStatement("$N($N)", initializeWorld, world)
                .addStatement("$N = true", worldInitialized)
                .endControlFlow();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(SCHEDULER_INITIALIZER_CLASS_NAME)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ScheduleInitializer.class)
                .addField(worldInitialized)
                .addMethod(initializeWorld);

        return new ScheduleInitializerIntermediaryResult(classBuilder, initializeBuilder, world, schedule);
    }

    private ExecutableElement getExecutableElement(Class<?> clazz, String methodName) {
        TypeElement type = processingEnv.getElementUtils()
                .getTypeElement(clazz.getCanonicalName());
        return methodsIn(type.getEnclosedElements()).stream()
                .filter(elt -> elt.getSimpleName().contentEquals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private void error(Element e, String msg) {
        messager.printError(msg, e);
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    record ScheduleInitializerIntermediaryResult(TypeSpec.Builder classBuilder, MethodSpec.Builder initializeBuilder,
                                                 ParameterSpec world, ParameterSpec schedule) {
    }
}
