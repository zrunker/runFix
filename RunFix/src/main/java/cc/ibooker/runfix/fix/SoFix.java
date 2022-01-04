package cc.ibooker.runfix.fix;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cc.ibooker.runfix.fix.loader.SoLoader;
import dalvik.system.PathClassLoader;


/**
 * @program: RunFix
 * @description: So库修复类
 * @author: zoufengli01
 * @create: 2021-11-05 14:03
 **/
public class SoFix implements IRunFix {

    /**
     * 修复SO库
     *
     * @param context  上下文对象
     * @param dirPath  待修复文件目录地址
     * @param callBack 回调
     */
    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        Log.d("RunFix-SoFix-1", dirPath + "");
        // 获取补丁文件
        Set<File> soDirs = new SoLoader().load(context, dirPath);
        // 获取补丁文件
        if (soDirs != null && soDirs.size() > 0) {
            Log.d("RunFix-SoFix-2", soDirs.size() + "");
            try {
                // 加载应用程序PathClassLoader
                PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();

                // 获取当前应用已经加载SO库数组
                Object pathList = getPathList(pathLoader);
                Object rightArr = getNativeLibraryArr(pathList);

                // 合并SO库数组，重新设置目标值
                Object leftArr = generateNativeLibraryArr(pathLoader, new ArrayList<>(soDirs));

                if (leftArr != null) {
                    Object mergeArr = mergeArray(leftArr, rightArr);
                    updateNativeLibraryArr(pathLoader, mergeArr);
                }
            } catch (Exception e) {
                if (callBack != null) {
                    callBack.onError(FixType.SO, e.getMessage());
                }
            }
        }
    }

    /**
     * 获取BaseDexClassLoader中pathList类变量
     *
     * @param classLoader 加载器实例
     */
    private Object getPathList(ClassLoader classLoader)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = Class.forName("dalvik.system.BaseDexClassLoader");
        Field field = clazz.getDeclaredField("pathList");
        field.setAccessible(true);
        return field.get(classLoader);
    }

    /**
     * 获取DexPathList中资源数组
     *
     * @param pathList 实例
     */
    private Object getNativeLibraryArr(Object pathList)
            throws NoSuchFieldException, IllegalAccessException {
        return getNativeLibraryField(pathList.getClass()).get(pathList);
    }

    /**
     * 更新NativeLibraryArr
     *
     * @param classLoader 加载器实例
     * @param mergeArr    待更新数据
     */
    private void updateNativeLibraryArr(ClassLoader classLoader, Object mergeArr)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Object pathList = getPathList(classLoader);
        getNativeLibraryField(pathList.getClass()).set(pathList, mergeArr);
    }

    /**
     * 获取nativeLibrary-Field
     *
     * @param clazz Class对象
     */
    private Field getNativeLibraryField(Class<?> clazz) throws NoSuchFieldException {
        Field field;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6.0
            field = clazz.getDeclaredField("nativeLibraryPathElements");
        } else {
            field = clazz.getDeclaredField("nativeLibraryDirectories");
        }
        field.setAccessible(true);
        return field;
    }

    /**
     * 根据文件夹列表生成nativeLibraryArr
     *
     * @param classLoader 加载器实例
     * @param dirs        待转换文件夹集合
     */
    private Object[] generateNativeLibraryArr(ClassLoader classLoader, List<File> dirs)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException {
        Object[] nativeLibraryArr;
        Object dexPathList = getPathList(classLoader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6.0 - 通过makePathElements生成Element数组
            // 插入nativeLibraryPathElements数组头部
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // < Android 7.0
                // private static Element[] makePathElements(List<File> files,
                // File optimizedDirectory, List<IOException> suppressedExceptions)
                Method makePathElements = dexPathList.getClass().getDeclaredMethod("makePathElements",
                        List.class, File.class, List.class);
                makePathElements.setAccessible(true);
                nativeLibraryArr = (Object[]) makePathElements.invoke(dexPathList, dirs, null,
                        new ArrayList<IOException>());
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // >= Android 7.0
                // private static Element[] makePathElements(List<File> files,
                // List<IOException> suppressedExceptions, ClassLoader loader)
                Method makePathElements = dexPathList.getClass().getDeclaredMethod(
                        "makePathElements", List.class, File.class, ClassLoader.class);
                makePathElements.setAccessible(true);
                nativeLibraryArr = (Object[]) makePathElements.invoke(dexPathList, dirs,
                        null, classLoader);
            } else {
                // >= Android 8.0
                // private static NativeLibraryElement[] makePathElements(List<File> files)
                Method makePathElements = dexPathList.getClass().getDeclaredMethod(
                        "makePathElements", List.class);
                makePathElements.setAccessible(true);
                nativeLibraryArr = (Object[]) makePathElements.invoke(dexPathList, dirs);
            }
        } else {
            // 小于 Android 6.0，直接取文件夹，arm64-v8a，armeabi等等
            // 插入nativeLibraryDirectories数组头部
            nativeLibraryArr = dirs.toArray();
        }
        return nativeLibraryArr;
    }

    /**
     * 数组合并
     *
     * @param leftArr  补丁Dex集合
     * @param rightArr 原Dex集合
     */
    private Object mergeArray(@NonNull Object leftArr, Object rightArr) {
        int leftLength = Array.getLength(leftArr);
        int rightLength = Array.getLength(rightArr);
        int totalLength = leftLength + rightLength;
        // 创建一个类型为componentType，长度为totalLength的新数组
        Class<?> componentType = leftArr.getClass().getComponentType();
        assert componentType != null;
        Object result = Array.newInstance(componentType, totalLength);
        System.arraycopy(leftArr, 0, result, 0, leftLength);
        System.arraycopy(rightArr, 0, result, leftLength, rightLength);
        return result;
    }

}
