package com.ct.heigirl.utils;

import android.util.Log;

/**
 * Created by Crystal on 2017/3/10.
 */

public class LogUtils {

    private static boolean isDebug = true;

    public static void d(String tag, String msg) {

        if (!isDebug) {     //不是调试模式，则不打印log
            return;
        }
        Log.d(tag, msg);
    }
}
