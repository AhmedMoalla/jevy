package com.jevy.ecs.event;

import java.util.*;

public interface EventReader<T> {
    Collection<T> read();
}
