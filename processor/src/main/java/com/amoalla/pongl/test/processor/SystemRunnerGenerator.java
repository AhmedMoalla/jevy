package com.amoalla.pongl.test.processor;

import com.amoalla.pongl.test.ECSWorld;
import com.amoalla.pongl.test.Filter;
import com.amoalla.pongl.test.Query;
import com.amoalla.pongl.test.SystemRunner;
import com.google.auto.common.MoreElements;
import com.squareup.javapoet.*;
import dev.dominion.ecs.api.Dominion;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;

import static com.amoalla.pongl.test.ScheduleInitializer.SCHEDULER_INITIALIZER_PACKAGE;
import static com.amoalla.pongl.test.processor.Utils.getTypeParameters;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class SystemRunnerGenerator {

    private final Types typeUtils;
    private final Elements elementUtils;
    private final ProcessingEnvironment processingEnv;

    public SystemRunnerGenerator(Types typeUtils, Elements elementUtils, ProcessingEnvironment processingEnv) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.processingEnv = processingEnv;
    }

    public JavaFile generate(AnnotatedFunctionalSystem system) throws ProcessingException {
        // final class UpdateSystemRunner implements SystemRunner
        TypeSpec.Builder clazz = TypeSpec.classBuilder(system.runnerName())
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(SystemRunner.class);

        // private final Main2 parent
        FieldSpec parent = FieldSpec.builder(system.enclosingClassName(), "parent", Modifier.PRIVATE, Modifier.FINAL)
                .build();
        clazz.addField(parent);

        // UpdateSystemRunner(ECSWorld world)
        ParameterSpec world = ParameterSpec.builder(ECSWorld.class, "world").build();
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addParameter(world)
                .addStatement("this.$N = $N.getResource($T.class)", parent, world, system.enclosingClassName());

        // Populate fields and constructor statements
        for (VariableElement parameter : system.parameters()) {
            if (isQuery(parameter)) {
                // private final Query1Impl<Main2.EntityName> query = new Query1Impl<>()
                addQueryField(clazz, parameter);
            } else {
                FieldSpec field = asField(parameter);
                // this.parameter = world.getResource(Parameter.class)
                constructor.addStatement("this.$N = $N.getResource($T.class)", field.name, world, field.type);
                // private final Parameter parameter
                clazz.addField(field);
            }
        }

        // public void run()
        ExecutableElement superRun = getExecutableElement(SystemRunner.class, "run");
        MethodSpec.Builder run = MethodSpec.overriding(superRun);

        // Add dominion field and statement
        // Handle queries and filters
        if (system.hasQuery(typeUtils, elementUtils)) {
            VariableElement query = system.getQuery(typeUtils, elementUtils);
            handleQueries(clazz, constructor, run, world, query);
        }

        clazz.addMethod(constructor.build());

        String args = system.parameters().stream()
                .map(VariableElement::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.joining(", "));
        run.addStatement("$N.%s(%s)".formatted(system.name(), args), parent);
        clazz.addMethod(run.build());

        return JavaFile.builder(SCHEDULER_INITIALIZER_PACKAGE, clazz.build()).build();
    }

    private void handleQueries(TypeSpec.Builder clazz, MethodSpec.Builder constructor,
                               MethodSpec.Builder run, ParameterSpec world, VariableElement query) {
        FieldSpec dominion = FieldSpec.builder(Dominion.class, "dominion", Modifier.PRIVATE, Modifier.FINAL)
                .build();
        // private final Dominion dominion
        clazz.addField(dominion);
        // this.dominion = world.dominion()
        constructor.addStatement("this.$N = $N.dominion()", dominion, world);

        // var compositions = dominion.findCompositionsWith(Main2.EntityName.class)
        String findArgs = Arrays.stream(getTypeParameters(query))
                .map(typeName -> typeName.toString() + ".class")
                .collect(Collectors.joining(", "));
        String code = "var compositions = $N.findCompositionsWith(%s)".formatted(findArgs);

        code += generateFilters(query);

        run.addStatement(code, dominion)
                // query.setResults(compositions)
                .addStatement("$N.setResults(compositions)", asField(query));
    }

    private String generateFilters(VariableElement query) {
        return extractFilters(query)
                .map(filters -> filters.keySet().stream()
                        .map(type -> {
                            String withArgs = filters.get(type).stream()
                                    .map(className -> className.canonicalName() + ".class")
                                    .collect(Collectors.joining(", "));
                            return "." + type.ecsMethodName + "(%s)".formatted(withArgs);
                        }).collect(Collectors.joining()))
                .orElse("");
    }

    private void addQueryField(TypeSpec.Builder clazz, VariableElement parameter) {
        TypeElement element = (TypeElement) typeUtils.asElement(parameter.asType());
        ClassName queryType = ClassName.get(Query.class.getPackageName(),
                element.getSimpleName().toString() + "Impl");
        TypeName[] queryTypeParameters = getTypeParameters(parameter);
        TypeName queryTypeWithGenerics = ParameterizedTypeName.get(queryType, queryTypeParameters);
        FieldSpec field = FieldSpec.builder(queryTypeWithGenerics, "query", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", queryType)
                .build();
        clazz.addField(field);
    }

    private FieldSpec asField(VariableElement parameter) {
        TypeElement element = (TypeElement) typeUtils.asElement(parameter.asType());
        ClassName className = ClassName.get(element);
        String name = parameter.getSimpleName().toString();
        return FieldSpec.builder(className, name, Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private ExecutableElement getExecutableElement(Class<?> clazz, String methodName) {
        TypeElement type = processingEnv.getElementUtils()
                .getTypeElement(clazz.getCanonicalName());
        return methodsIn(type.getEnclosedElements()).stream()
                .filter(elt -> elt.getSimpleName().contentEquals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private boolean isQuery(VariableElement element) {
        return Utils.isAssignable(typeUtils, elementUtils, element, Query.class);
    }

    @SuppressWarnings({"UnstableApiUsage", "unchecked", "rawtypes"})
    private Optional<Map<QueryFilterType, List<ClassName>>> extractFilters(Element element) {
        return MoreElements.getAnnotationMirror(element, Filter.class)
                .toJavaUtil()
                .map(AnnotationMirror::getElementValues)
                .map(values -> {
                    Map<QueryFilterType, List<ClassName>> filters = new EnumMap<>(QueryFilterType.class);
                    for (ExecutableElement executable : values.keySet()) {
                        List<ClassName> classNames = ((List) values.get(executable).getValue())
                                .stream().<ClassName>map(obj -> ClassName.bestGuess(obj.toString().replace(".class", "")))
                                .toList();
                        QueryFilterType filterType = QueryFilterType.valueOf(executable.getSimpleName().toString().toUpperCase());
                        filters.put(filterType, classNames);
                    }
                    return filters;
                });
    }

    enum QueryFilterType {
        WITH("withAlso"), WITHOUT("without");
        private final String ecsMethodName;

        QueryFilterType(String ecsMethodName) {
            this.ecsMethodName = ecsMethodName;
        }
    }
}
