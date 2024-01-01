package com.jevy.ecs;

import dev.dominion.ecs.api.Results;

import java.util.Iterator;
import java.util.stream.Stream;

public class Query1Impl<T1> implements Query1<T1> {

    private Results<T1> results;

    public Query1Impl<T1> setResults(Results<T1> results) {
        this.results = results;
        return this;
    }

    @Override
    public Iterator<T1> iterator() {
        return results.iterator();
    }

    @Override
    public Stream<T1> stream() {
        return results.stream();
    }

    @Override
    public Stream<T1> parallelStream() {
        return results.parallelStream();
    }

    @Override
    public Results<T1> without(Class<?>... componentTypes) {
        return results.without(componentTypes);
    }

    @Override
    public Results<T1> withAlso(Class<?>... componentTypes) {
        return results.withAlso(componentTypes);
    }

    @Override
    public <S extends Enum<S>> Results<T1> withState(S state) {
        return results.withState(state);
    }
}