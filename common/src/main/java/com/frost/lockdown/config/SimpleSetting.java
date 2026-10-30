package com.frost.lockdown.config;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SimpleSetting<T> implements Setting<T> {

    private final String id;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable saver;

    public SimpleSetting(String id, Supplier<T> getter, Consumer<T> setter, Runnable saver) {
        this.id = Objects.requireNonNull(id);
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
        this.saver = Objects.requireNonNull(saver);
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T value) {
        setter.accept(value);
    }

    @Override
    public void save() {
        saver.run();
    }

    @Override
    public String id() {
        return id;
    }
}
