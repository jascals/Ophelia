package com.jascal.ophelia_api;

import android.app.Activity;
import android.view.View;

public class Utils {

    public static <T extends View> T findViewById(Activity activity, int resourceId) {
        return (T) activity.findViewById(resourceId);
    }
}
