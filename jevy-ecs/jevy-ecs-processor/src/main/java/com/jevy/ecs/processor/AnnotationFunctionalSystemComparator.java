package com.jevy.ecs.processor;

import com.jevy.ecs.systemset.*;

import java.util.*;

public class AnnotationFunctionalSystemComparator implements Comparator<AnnotatedFunctionalSystem> {
    @Override
    public int compare(AnnotatedFunctionalSystem system1, AnnotatedFunctionalSystem system2) {
        System.out.println("Comparing " + system1.name() + "[" + system1.rules() + "] and " + system2.name() + "[" + system2.rules() + "]");

        SystemSetRules rules1 = system1.rules();
        SystemSetRules rules2 = system2.rules();
        if (rules1 == null && rules2 == null) {
            return 1;
        }

        if (rules1 != null) {
            for (String after : rules1.after()) {
                if (after.equals(system2.name())) {
                    return 1;
                }
            }

            for (String before : rules1.before()) {
                if (before.equals(system2.name())) {
                    return -1;
                }
            }
        }

        if (rules2 != null) {
            for (String after : rules2.after()) {
                if (after.equals(system1.name())) {
                    return -1;
                }
            }

            for (String before : rules2.before()) {
                if (before.equals(system1.name())) {
                    return 1;
                }
            }
        }

        return 0;
    }
}
