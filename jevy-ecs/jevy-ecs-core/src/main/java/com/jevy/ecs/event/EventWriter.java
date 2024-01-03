package com.jevy.ecs.event;

public interface EventWriter<T> {
    void send(T event);
}
