package com.frost.lockdown.config;

public interface Setting<T> {

    T get();

    void set(T value);

    void save();

    String id();
}
