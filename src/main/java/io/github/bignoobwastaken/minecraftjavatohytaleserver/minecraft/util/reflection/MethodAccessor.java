package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection;

public interface MethodAccessor {
    Object invoke(Object target, Object... args);
    boolean hasMethod(Object target);
}
