package com.jevy.ecs.systemset;

import java.util.*;

public record SystemSetRules(List<String> before, List<String> after) {

    public SystemSetRules {
        Objects.requireNonNull(before);
        Objects.requireNonNull(after);
        before = Collections.unmodifiableList(before);
        after = Collections.unmodifiableList(after);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> before = new ArrayList<>();
        private final List<String> after = new ArrayList<>();

        public Builder before(String label) {
            before.add(label);
            return this;
        }

        public Builder before(Class<? extends SystemLabel> label) {
            return before(label.getCanonicalName());
        }

        public Builder after(String label) {
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