package com.frost.lockdown;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemLockerService {

    public static Optional<ResourceLocation> getItemLocation(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(stack.getItemHolder().unwrapKey()
                .map(ResourceKey::location)
                .orElse(ResourceLocation.withDefaultNamespace("unknown")));
    }

    public static boolean addToSimpleList(
            Supplier<List<? extends String>> getter,
            Consumer<List<String>> setter,
            Runnable saver,
            Runnable cacheRebuilder,
            String value
    ) {
        List<String> current = new ArrayList<>(getter.get());
        if (current.contains(value)) {
            return false;
        }
        current.add(value);
        saveAndRebuild(setter, saver, cacheRebuilder, current);
        return true;
    }

    public static boolean removeFromSimpleList(
            Supplier<List<? extends String>> getter,
            Consumer<List<String>> setter,
            Runnable saver,
            Runnable cacheRebuilder,
            String value
    ) {
        List<String> current = new ArrayList<>(getter.get());
        if (!current.remove(value)) {
            return false;
        }
        saveAndRebuild(setter, saver, cacheRebuilder, current);
        return true;
    }

    public static void setKeyValue(
            Supplier<List<? extends String>> getter,
            Consumer<List<String>> setter,
            Runnable saver,
            Runnable cacheRebuilder,
            String key,
            int value
    ) {
        List<String> current = new ArrayList<>(getter.get());
        current.removeIf(entry -> entry.startsWith(key + "=") || entry.equals(key));
        current.add(key + "=" + value);
        saveAndRebuild(setter, saver, cacheRebuilder, current);
    }

    public static boolean removeKeyValue(
            Supplier<List<? extends String>> getter,
            Consumer<List<String>> setter,
            Runnable saver,
            Runnable cacheRebuilder,
            String key
    ) {
        List<String> current = new ArrayList<>(getter.get());
        boolean removed = current.removeIf(entry -> entry.startsWith(key + "=") || entry.equals(key));
        if (!removed) {
            return false;
        }
        saveAndRebuild(setter, saver, cacheRebuilder, current);
        return true;
    }

    private static void saveAndRebuild(
            Consumer<List<String>> setter,
            Runnable saver,
            Runnable cacheRebuilder,
            List<String> newList
    ) {
        setter.accept(newList);
        saver.run();
        cacheRebuilder.run();
    }
}