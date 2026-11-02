package com.frost.lockdown.config;

import java.util.Objects;
import java.util.function.Function;

public final class DeferredSetting<T> implements Setting<T> {

    private final String id;
    private final Function<ConfigAccess, Setting<T>> resolver;

    public DeferredSetting(String id, Function<ConfigAccess, Setting<T>> resolver) {
        this.id = Objects.requireNonNull(id);
        this.resolver = Objects.requireNonNull(resolver);
    }

    private Setting<T> resolve() {
        return resolver.apply(ConfigAccess.get());
    }

    @Override
    public T get() {
        return resolve().get();
    }

    @Override
    public void set(T value) {
        resolve().set(value);
    }

    @Override
    public void save() {
        resolve().save();
    }

    @Override
    public String id() {
        return id;
    }
}
