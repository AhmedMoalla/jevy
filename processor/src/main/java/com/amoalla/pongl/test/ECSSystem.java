package com.amoalla.pongl.test;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ECSSystem {
}
