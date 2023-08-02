package com.mkm75.deckshare.app;

import java.lang.reflect.Method;

/**
 * アプリケーションのエントリポイントです。<br><br>
 * 引数なしで実行された場合最初にJavaFX GUIの起動を試みます。
 * 起動できた場合はそのままGUIアプリケーションとして実行しますが、
 * classpath上にJavaFXが無い場合ClassLoader#loadClassでNoClassDefFoundErrorが発生するため、
 * そういった場合はCLIとして実行します。
 * コマンドライン引数が指定されている場合、GUIとしての実行を試みません。
 */
public class Invoker {
    public static void main(String[] args) {
        if (args.length == 0) {
            try {
                Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.mkm75.deckshare.app.FXApp");
                Method method = clazz.getMethod("main", String[].class);
                method.invoke(null, (Object) args);
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {}
        }
        CLI.main(args);
    }

}
