package com.githang.statusbar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 设置系统状态栏颜色
 *
 * @author msdx (msdx.android@qq.com)
 * @version 0.5.1
 * @since 0.1
 */

public class StatusBarUtil {

    static final IStatusBar IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IMPL = new StatusBarMImpl();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isEMUI()) {
            IMPL = new StatusBarLollipopImpl();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            IMPL = new StatusBarKitkatImpl();
        } else {
            IMPL = new IStatusBar() {
                @Override
                public void setStatusBarColor(Window window, int color) {
                }
            };
        }
    }

    private static boolean isEMUI() {
        File file = new File(Environment.getRootDirectory(), "build.prop");
        if (file.exists()) {
            Properties properties = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                properties.load(fis);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return properties.containsKey("ro.build.hw_emui_api_level");
        }
        return false;
    }

    public static void setStatusBarColor(Activity activity, int color) {
        boolean isLightColor = toGrey(color) > 225;
        setStatusBarColor(activity, color, isLightColor);
    }

    /**
     * 使用限制： 显示的为布局中第一个有backgroud的控件的颜色
     * 并且 设置的为colordrawable
     * 其他条件下使用 setStatusBarColor（）;
     *                 或者自定义
     * 
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void enableStatusBar(Activity activity){
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        getHeaderColor(activity, viewGroup);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void getHeaderColor(Activity activity,View view){
        if (view instanceof ViewGroup){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View childView = ((ViewGroup) view).getChildAt(i);
                Drawable drawable = childView.getBackground();
                if (drawable instanceof ColorDrawable){
                    int color = ((ColorDrawable)drawable).getColor();
                    setStatusBarColor(activity,color);
                    return;
                }else {
                    getHeaderColor(activity,childView);
                }
            }
        }else {
            Drawable drawable = view.getBackground();
            if (drawable instanceof ColorDrawable){
                int color = ((ColorDrawable)drawable).getColor();
                setStatusBarColor(activity,color);
                return;
            }
        }
    }

    /**
     * 把颜色转换成灰度值。
     * 代码来自 Flyme 示例代码
     */
    public static int toGrey(int color) {
        int blue = Color.blue(color);
        int green = Color.green(color);
        int red = Color.red(color);
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    /**
     * Set system status bar color.
     *
     * @param activity
     * @param color          status bar color
     * @param lightStatusBar if the status bar color is light. Only effective in some devices.
     */
    public static void setStatusBarColor(Activity activity, int color, boolean lightStatusBar) {
        setStatusBarColor(activity.getWindow(), color, lightStatusBar);
    }

    /**
     * Set the system status bar color
     * @param window the window
     * @param color status bar color
     * @param lightStatusBar if the status bar color is light. Only effective in some devices.
     */
    public static void setStatusBarColor(Window window, int color, boolean lightStatusBar) {
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) > 0
                || StatusBarExclude.exclude) {
            return;
        }
        IMPL.setStatusBarColor(window, color);
        LightStatusBarCompat.setLightStatusBar(window, lightStatusBar);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void setFitsSystemWindows(Window window, boolean fitSystemWindows) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                mChildView.setFitsSystemWindows(fitSystemWindows);
            }
        }
    }

    /**
     * @param window the window will be set
     * @param isLightStatusBar if the status bar color is light
     * @since 0.5.1
     */
    public static void setLightStatusBar(Window window, boolean isLightStatusBar) {
        LightStatusBarCompat.setLightStatusBar(window, isLightStatusBar);
    }
}
