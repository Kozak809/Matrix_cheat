package dev.mlml.matrix.event;

import dev.mlml.matrix.MatrixMod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventManager {
    protected final Map<Class<?>, Set<Handler<?>>> handlers = new ConcurrentHashMap<>();

    /**
     * This method is used to register an object or a class to handle events.
     * The object or class should contain one or more methods annotated with {@link Listener}.
     * These methods should have a single parameter, which is a subclass of Event.
     * <p>
     * If the object passed is an instance of a class, the method will register all non-static methods
     * in the class that are annotated with {@link Listener} and have the correct parameters.
     * <p>
     * If the object passed is a Class, the method will register all static methods in the class
     * that are annotated with {@link Listener} and have the correct parameters.
     * <p>
     * If a method is found that is annotated with {@link Listener} but does not meet the requirements
     * (it is non-static and the object is a Class, or it does not have the correct parameters),
     * an error message will be logged and the method will not be registered.
     *
     * @param object The object or class to be registered. This should be an instance of a class
     *               that contains non-static methods annotated with {@link Listener}, or a Class that
     *               contains static methods annotated with {@link Listener}.
     */
    public void register(Object object) {
        Class<?> clazz = object.getClass();
        if (object instanceof Class<?>) {
            clazz = (Class<?>) object;
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (methodHasCorrectParams(method) && hasListenerAnnotation(method)) {
                if (object instanceof Class<?> && !java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    MatrixMod.LOGGER.error("Tried to register non-static method " + method.getName() + " in " + clazz.getSimpleName());
                    continue;
                }

                Class<?> eventType = method.getParameterTypes()[0];
                registerHandlerMethod(object, method, eventType);
            }
        }
    }

    private boolean methodHasCorrectParams(Method method) {
        return method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private boolean hasListenerAnnotation(Method method) {
        return method.isAnnotationPresent(Listener.class);
    }

    private void registerHandlerMethod(Object parent, Method method, Class<?> eventType) {
        method.setAccessible(true);
        try {
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
            Handler<?> handler = event -> {
                try {
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                        methodHandle.invoke(event);
                    } else {
                        methodHandle.invoke(parent, event);
                    }
                } catch (Throwable e) {
                    MatrixMod.LOGGER.error("Error invoking event handler " + method.getName(), e);
                }
            };
            handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(handler);
        } catch (IllegalAccessException e) {
            MatrixMod.LOGGER.error("Failed to access method " + method.getName(), e);
        }
    }

    /**
     * This method triggers an event, causing all registered handlers for that event type to be invoked.
     *
     * @param event The event to be triggered.
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void trigger(E event) {
        Set<Handler<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (Handler<?> handler : eventHandlers) {
                ((Handler<E>) handler).handle(event);
            }
        }
    }
}
