package com.jevy.ecs.query;

import dev.dominion.ecs.api.*;

import java.util.*;
import java.util.stream.*;

public class Query2Impl<T1, T2> implements Query2<T1, T2> {

    private Results<With2<T1, T2>> results;

    public Query2Impl<T1, T2> setResults(Results<With2<T1, T2>> results) {
        this.results = results;
        return this;
    }

    @Override
    public Iterator<With2<T1, T2>> iterator() {
        return results.iterator();
    }

    @Override
    public Stream<With2<T1, T2>> stream() {
        return results.stream();
    }

    @Override
    public Stream<With2<T1, T2>> parallelStream() {
        return results.parallelStream();
    }

    @Override
    public Results<With2<T1, T2>> without(Class<?>... componentTypes) {
        return results.without(componentTypes);
    }

    @Override
    public Results<With2<T1, T2>> withAlso(Class<?>... componentTypes) {
        return results.withAlso(componentTypes);
    }

    @Override
    public <S extends Enum<S>> Results<With2<T1, T2>> withState(S state) {
        return results.withState(state);
    }
}