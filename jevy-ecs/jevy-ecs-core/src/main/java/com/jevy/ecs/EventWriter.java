package com.jevy.ecs;

public interface EventWriter<T> {
    void send(T event);
}
