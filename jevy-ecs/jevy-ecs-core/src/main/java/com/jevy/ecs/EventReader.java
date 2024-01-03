package com.jevy.ecs;

import java.util.Collection;

public interface EventReader<T> {
    Collection<T> read();
}
