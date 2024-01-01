package com.amoalla.pongl.test.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class Utils {

    public static boolean isAssignable(Types typeUtils, Elements elementUtils, Element element, Class<?> clazz) {
        TypeMirror targetTypeMirror = typeUtils.getDeclaredType(
                elementUtils.getTypeElement(clazz.getCanonicalName()));
        TypeMirror variableTypeMirror = element.asType();
        return typeUtils.isAssignable(variableTypeMirror, targetTypeMirror);
    }

    public static TypeName[] getTypeParameters(Element element) {
        return ((DeclaredType) element.asType())
                .getTypeArguments().stream()
                .map(ClassName::get)
                .toArray(TypeName[]::new);
    }
}
