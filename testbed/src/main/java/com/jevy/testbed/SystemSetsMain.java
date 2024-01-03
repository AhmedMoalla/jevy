package com.jevy.testbed;

import com.jevy.ecs.annotation.ScanSystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ScanSystems
public class SystemSetsMain {

    public static void main(String[] args) {

    }

    @interface Order {
        String[] before() default "";

        String[] after() default "";

        Class<? extends SystemLabel>[] beforeLabel() default DefaultSystemLabel.class;

        Class<? extends SystemLabel>[] afterLabel() default DefaultSystemLabel.class;
    }

    @interface Label {
        Class<? extends SystemLabel>[] value();
    }

    interface SystemLabel {
    }

    static class DefaultSystemLabel implements SystemLabel {
    }

    static class MyLabel implements SystemLabel {
    }

    interface SystemSetRulesProvider {
        SystemSetRules buildRules();
    }

    record SystemSetRules(List<String> before, List<String> after) {

        SystemSetRules {
            before = Collections.unmodifiableList(before);
            after = Collections.unmodifiableList(after);
        }

        public static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private List<String> before;
            private List<String> after;

            public Builder before(String label) {
                if (before == null) before = new ArrayList<>();
                before.add(label);
                return this;
            }

            public Builder before(Class<? extends SystemLabel> label) {
                return before(label.getCanonicalName());
            }

            public Builder after(String label) {
                if (after == null) after = new ArrayList<>();
                after.add(label);
                return this;
            }

            public Builder after(Class<? extends SystemLabel> label) {
                return after(label.getCanonicalName());
            }

            public SystemSetRules build() {
                return new SystemSetRules(before, after);
            }
        }
    }

    static class MySystemSet implements SystemSetRulesProvider {
        @Override
        public SystemSetRules buildRules() {
            return SystemSetRules.builder()
                    .after("test")
                    .before(MyLabel.class)
                    .build();
        }
    }

    @interface SystemSet {
        Class<? extends SystemSetRulesProvider> value();
    }
}
