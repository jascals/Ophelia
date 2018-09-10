package com.jascal.ophelia_api;

import android.app.Activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Ophelia {
    public Ophelia() {
    }

    private static <T extends Activity> void initialization(T target, String suffix) {
        Class<?> tClass = target.getClass();
        String className = tClass.getName();
        try {
            Class<?> bindingClass = tClass.getClassLoader().loadClass(className + suffix);
            Constructor<?> constructor = bindingClass.getConstructor(tClass);
            constructor.newInstance(target);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void bind(Activity activity) {
        initialization(activity, "$Binding");
    }
}
