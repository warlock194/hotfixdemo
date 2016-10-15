package com.example.hotpatch;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * Created by hp on 2016/4/11.
 */
public class HotPatch {

//    private HotPatch() {}

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        File hackDir = context.getDir("hackDir", 0);
        File hackJar = new File(hackDir,"hack_dex.jar");
        try {
            AssetsUtil.copyAssets(context,"hack_dex.jar", hackJar.getAbsolutePath());
            Log.d("warlock", "hackJar.getAbsolutePath()  : " + hackJar.getAbsolutePath());
            inject(hackJar.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void inject(String path) {
        File file = new File(path);
        Log.d("warlock"," file exists  --- " + path);
        if(file.exists()) {
            Log.d("warlock"," file exists ");
            try {
                // 获取classes的dexElements
                Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
                Object pathList = ReflectUtil.getField(cl, "pathList", mContext.getClassLoader());
                Object baseElements = ReflectUtil.getField(pathList.getClass(), "dexElements", pathList);

                // 获取patch_dex的dexElements（需要先加载dex）
                String dexopt = mContext.getDir("dexopt", 0).getAbsolutePath();
                DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt,mContext.getClassLoader());
                Object obj = ReflectUtil.getField(cl, "pathList", dexClassLoader);
                Object dexElements = ReflectUtil.getField(obj.getClass(), "dexElements", obj);

                // 合并两个Elements
                Object combineElements = ReflectUtil.combineArray(dexElements, baseElements);

                // 将合并后的Element数组重新赋值给app的classLoader
                ReflectUtil.setField(pathList.getClass(), "dexElements", pathList, combineElements);

                Object object = getField(pathList.getClass(), "dexElements", pathList);
                int length = Array.getLength(object);
                Log.e("warlock", "length = " + length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("warlock", file.getAbsolutePath()+" does not exists");
        }
    }

    /**
     * 通过反射获取对象的属性值
     */
    private static Object getField(Class cl, String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
        Field field = cl.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
