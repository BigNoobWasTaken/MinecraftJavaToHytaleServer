package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util;

import java.util.logging.Level;

public enum Logger {
    INFO,
    WARN,
    ERROR,
    DEBUG;

    public void log(String msg) {
        switch (this) {
            case INFO -> System.out.println(msg);
            case WARN -> System.out.println(msg);
            case ERROR -> System.out.println(msg);
            case DEBUG -> {
                if (/*!plugin.getConfigManager().getMain().isLogDebug()*/false) return;

                Class<?> callerClass = StackWalker
                        .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();

                System.out.println("[" + callerClass + "] " + msg);
            }
        }
    }
}
