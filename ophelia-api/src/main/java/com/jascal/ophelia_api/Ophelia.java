package com.jascal.ophelia_api;

import android.app.Activity;

import java.lang.reflect.Constructor;

public class Ophelia {
    public static UnBinder bind(Activity activity) {
        try {
            Class<? extends UnBinder> bindClazz = (Class<? extends UnBinder>)
                    Class.forName(activity.getClass().getName() + "_ViewBinding");
            // 构造函数
            Constructor<? extends UnBinder> bindConstructor = bindClazz.getDeclaredConstructor(activity.getClass());

            UnBinder unbinder = bindConstructor.newInstance(activity);
            return unbinder;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return UnBinder.EMPTY;
    }
}
