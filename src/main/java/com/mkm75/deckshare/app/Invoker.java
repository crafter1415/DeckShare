package com.mkm75.deckshare.app;

import java.lang.reflect.Method;

public class Invoker {
    public static void main(String[] args) {
        if (args.length == 0) {
            try {
                Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.mkm75.deckshare.app.FXApp");
                Method method = clazz.getMethod("main", String[].class);
                method.invoke(null, (Object) args);
                return;
            } catch (ReflectiveOperationException ignored) {}
        }
        CLI.main(args);
    }

}
