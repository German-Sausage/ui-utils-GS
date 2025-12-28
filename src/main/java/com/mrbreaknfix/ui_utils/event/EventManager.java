/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file EventManager.java
 */
package com.mrbreaknfix.ui_utils.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mrbreaknfix.ui_utils.UiUtils;

public class EventManager {
    private final Map<Class<? extends Event>, CopyOnWriteArrayList<Method>> listeners =
            new ConcurrentHashMap<>();
    private final Map<Object, List<Method>> listenerInstances = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Registers a listener instance or class with the EventManager. If a class is provided, it
     * creates an instance internally.
     *
     * @param listener Either an instance or a class of the listener.
     */
    @SuppressWarnings("unchecked")
    public void addListener(Object listener) {
        if (listener instanceof Class) {
            try {
                listener = ((Class<?>) listener).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                UiUtils.LOGGER.error("Failed to instantiate listener", e);
                return;
            }
        }

        List<Method> methods = new ArrayList<>();
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)
                    && method.getParameterCount() == 1
                    && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {

                method.setAccessible(true);
                listeners
                        .computeIfAbsent(
                                (Class<? extends Event>) method.getParameterTypes()[0],
                                k -> new CopyOnWriteArrayList<>())
                        .add(method);
                methods.add(method);
            }
        }
        listenerInstances.put(listener, methods);
    }

    /**
     * Removes a listener instance or class and its associated methods from the EventManager.
     *
     * @param listener Either an instance or a class of the listener to be removed.
     */
    @SuppressWarnings("unchecked")
    public void removeListener(Object listener) {
        if (listener instanceof Class) {
            listenerInstances
                    .entrySet()
                    .removeIf(entry -> entry.getKey().getClass().equals(listener));
            return;
        }

        List<Method> methods = listenerInstances.remove(listener);
        if (methods != null) {
            for (Method method : methods) {
                listeners
                        .getOrDefault(
                                (Class<? extends Event>) method.getParameterTypes()[0],
                                new CopyOnWriteArrayList<>())
                        .remove(method);
            }
        }
    }

    /**
     * Triggers the given event, invoking all registered listeners that handle this event type.
     *
     * @param event The event to be triggered.
     * @param <T> The type of the event.
     */
    public <T extends Event> void trigger(T event) {
        List<Method> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null) return;

        for (Method method : eventListeners) {
            executor.submit(
                    () -> {
                        try {
                            for (Object listener : listenerInstances.keySet()) {
                                if (method.getDeclaringClass().isInstance(listener)) {
                                    method.invoke(listener, event);
                                }
                            }
                            //                            if (event.isCancelled()) {
                            //                        System.out.println("Event '" +
                            // event.getClass().getSimpleName() + "' was cancelled.");
                            //                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            UiUtils.LOGGER.error(e.getMessage());
                        }
                    });
        }
    }

    /** Shuts down the event manager, stopping the executor. */
    public void shutdown() {
        executor.shutdown();
    }
}
