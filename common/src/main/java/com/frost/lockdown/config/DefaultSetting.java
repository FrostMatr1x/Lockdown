package com.frost.lockdown.config;

import java.util.Objects;

public final class DefaultSetting<T> implements Setting<T> {

    private final String id;
    private T value;

    public DefaultSetting(String id, T initialValue) {
        this.id = Objects.requireNonNull(id);
        this.value = initialValue;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void save() {
    }

    @Override
    public String id() {
        return id;
    }
}
