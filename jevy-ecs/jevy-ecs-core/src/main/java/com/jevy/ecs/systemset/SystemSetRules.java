package com.jevy.ecs.systemset;

import java.util.*;

public record SystemSetRules(List<String> before, List<String> after) {

    public SystemSetRules {
        before = Collections.unmodifiableList(before);
        after = Collections.unmodifiableList(after);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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