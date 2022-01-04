package cc.ibooker.runfix.fix;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import cc.ibooker.runfix.fix.loader.DexLoader;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Set;

import cc.ibooker.runfix.Config;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @program: RunFix
 * @description: Dex修复 - 下载Dex文件
 * @author: zoufengli01
 * @create: 2021-11-04 20:37
 **/
public class DexFix implements IRunFix {

    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        Log.d("RunFix-DexFix-1", dirPath + "");
        // 获取补丁文件
        Set<File> dexFiles = new DexLoader().load(context, dirPath);
        if (dexFiles != null && dexFiles.size() > 0) {
            Log.d("RunFix-DexFix-2", dexFiles.size() + "");
            // 获取Dex文件解压目录
            String optimizeDirPath = context.getFilesDir().getAbsolutePath()
                    + File.separator + Config.OPTIMIZE_DEX_DIR;
            File optLibDir = new File(optimizeDirPath);
            if (!optLibDir.exists() || !optLibDir.isDirectory()) {
                boolean result = optLibDir.mkdirs();
                if (!result) {
                    if (callBack != null) {
                        callBack.onError(FixType.DEX, "创建解压目录失败，请仔细检查文件读写权限！");
                    }
                    return;
                }
            }
            try {
                // 加载应用程序PathClassLoader
                PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();
                for (File dex : dexFiles) {
                    if (dex.exists() && dex.isFile()) {
                        Log.d("RunFix-DexFix-3", optLibDir.getAbsolutePath() + "");
                        Log.d("RunFix-DexFix-4", dex.getAbsolutePath() + "");
                        // 加载待修复DexClassLoader
                        DexClassLoader dexLoader = new DexClassLoader(
                                dex.getAbsolutePath(),
                                optLibDir.getAbsolutePath(),
                                null,
                                pathLoader
                        );
                        // Dex文件合并
                        Object dexPathList = getPathList(dexLoader);
                        Object pathList = getPathList(pathLoader);
                        Object leftArr = getDexElements(dexPathList);
                        Object rightArr = getDexElements(pathList);
                        if (leftArr != null) {
                            Object mergeArr = mergeArray(leftArr, rightArr);
                            updateDexElements(pathLoader, mergeArr);
                        }
                    }
                }
            } catch (Exception e) {
                if (callBack != null) {
                    callBack.onError(FixType.DEX, e.getMessage());
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
     * 获取PathList中dexElements数组
     *
     * @param pathList 类实例
     */
    private Object getDexElements(Object pathList)
            throws NoSuchFieldException, IllegalAccessException {
        return getDexElementsField(pathList.getClass()).get(pathList);
    }

    /**
     * 更新DexElements
     *
     * @param classLoader 加载器实例
     * @param mergeArr    待更新数据
     */
    private void updateDexElements(ClassLoader classLoader, Object mergeArr)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Object pathList = getPathList(classLoader);
        getDexElementsField(pathList.getClass()).set(pathList, mergeArr);
    }

    /**
     * 获取PathList中dexElements - Field
     *
     * @param clazz class实例
     */
    private Field getDexElementsField(Class<?> clazz) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField("dexElements");
        field.setAccessible(true);
        return field;
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
