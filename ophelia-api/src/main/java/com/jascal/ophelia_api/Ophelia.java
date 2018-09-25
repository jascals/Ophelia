package com.jascal.ophelia_api;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Ophelia {
    private static final String TAIL = "$OpheliaProxy";

    public Ophelia() {
    }

    private static <T extends Activity> void initialization(T target, String suffix) {
        Class<?> targetClass = target.getClass();
        String className = targetClass.getName();
        try {
            Class<?> bindingClass = targetClass.getClassLoader().loadClass(className + suffix);
            Constructor<?> constructor = bindingClass.getConstructor(targetClass);
            constructor.newInstance(target);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static <T extends Fragment> void initialization(T target, View view, String suffix) {
        Class<?> targetClass = target.getClass();
        String className = targetClass.getName();
        Class<?> viewClass = View.class;

        try {
            Class<?> bindingClass = targetClass.getClassLoader().loadClass(className + suffix);

            Constructor<?> constructor = bindingClass.getConstructor(targetClass, viewClass);
            constructor.newInstance(target, view);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void bind(Activity activity) {
        initialization(activity, TAIL);
    }

    public static void bind(Fragment fragment, View view) {
        initialization(fragment, view, TAIL);
    }
}
