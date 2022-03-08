package cc.banzhi.runfix.fix;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cc.banzhi.runfix.fix.loader.ResLoader;


/**
 * @program: RunFix
 * @description: 资源文件修复类
 * PS:
 * 1. 需要将工程中所有资源打包到APK中
 * 2. 在BaseActivity#attachBaseContext方法中执行fix，目的是替换Activity中mAssets
 * 3. 单文件修复，如果是多个文件只取其中一个文件
 * @author: zoufengli01
 * @create: 2021-11-05 14:03
 **/
public class ResFix implements IRunFix {

    // AssetManager.class.getConstructor(new Class[0]).newInstance(new Object[0]);
    // setApkAssets/addAssetPathInternal(path, false /*overlay*/, false /*appAsLib*/)
    // ensureStringBlocks()
    // 替换所有Activity中的Resource中的mAssets字段
    // 替换所有Activity的主题mAssets
    // 替换Resources弱引用集合mAssets
    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        Log.d("RunFix-ResFix-1", dirPath + "");
        // 获取补丁文件
        Set<File> resDirs = new ResLoader().load(context, dirPath);
        // 获取补丁文件
        if (resDirs != null && resDirs.size() > 0) {
            Log.d("RunFix-SoFix-2", resDirs.size() + "");
            for (File res : resDirs) {
                try {
                    AssetManager assetManager = createAssetManager(res.getAbsolutePath());
                    ArrayList<Activity> list = getActivities(context);
                    for (Activity activity : list) {
                        replaceResourceAssets(activity, assetManager);
                        replaceTheme(activity, assetManager);
                        replaceResourcesWeak(assetManager);
                    }
                } catch (Exception e) {
                    if (callBack != null) {
                        callBack.onError(FixType.RES, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 创建AssetManager对象
     *
     * @param dirPath APK文件地址
     */
    private AssetManager createAssetManager(String dirPath)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        AssetManager assetManager = AssetManager.class.getConstructor(new Class[0]).newInstance(new Object[0]);
        Method method = assetManager.getClass().getDeclaredMethod("addAssetPathInternal", String.class, Boolean.class, Boolean.class);
        method.setAccessible(true);
        method.invoke(assetManager, dirPath, false, false);

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        Method ensureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
        ensureStringBlocks.setAccessible(true);
        ensureStringBlocks.invoke(assetManager);

        return assetManager;
    }

    /**
     * 替换所有Activity中Resource中的mAssets字段
     *
     * @param activity 待替换Activity
     * @param assetM   替换值
     */
    private void replaceResourceAssets(Activity activity, AssetManager assetM)
            throws NoSuchFieldException, IllegalAccessException {
        Resources resources = activity.getResources();
        updateResources(resources, assetM);
    }

    /**
     * 替换Activity中主题
     *
     * @param activity 待替换Activity
     * @param assetM   替换值
     */
    private void replaceTheme(Activity activity, AssetManager assetM)
            throws NoSuchFieldException, IllegalAccessException {
        Resources.Theme theme = activity.getTheme();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6.0
            Field field = theme.getClass().getDeclaredField("mThemeImpl");
            field.setAccessible(true);
            Object themeImpl = field.get(theme);

            if (themeImpl != null) {
                Field field1 = themeImpl.getClass().getDeclaredField("mAssets");
                field1.setAccessible(true);
                field1.set(themeImpl, assetM);
            }
        } else {
            Field field = theme.getClass().getDeclaredField("mAssets");
            field.setAccessible(true);
            field.set(theme, assetM);
        }
    }

    /**
     * 替换Resources弱引用集合
     *
     * @param assetM 替换值
     */
    private void replaceResourcesWeak(AssetManager assetM)
            throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        // 获取WeakReference<Resources>
        Collection<WeakReference<Resources>> referencesWeakRs = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Class<?> clazz = Class.forName("android.app.ResourcesManager");
            Method method = clazz.getMethod("getInstance");
            Object resourcesManager = method.invoke(null);

            Field field = clazz.getDeclaredField("mResourceReferences");
            field.setAccessible(true);
            referencesWeakRs = (Collection<WeakReference<Resources>>) field.get(resourcesManager);
        } else {
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Method method = clazz.getMethod("currentActivityThread");
            Object activityThread = method.invoke(null);

            Field field = clazz.getDeclaredField("mActiveResources");
            field.setAccessible(true);
            HashMap<?, WeakReference<Resources>> map = (HashMap<?, WeakReference<Resources>>) field.get(activityThread);
            if (map != null) {
                referencesWeakRs = map.values();
            }
        }
        // 处理WeakReference<Resources>
        if (referencesWeakRs != null) {
            for (WeakReference<Resources> wr : referencesWeakRs) {
                Resources resources = wr.get();
                if (resources != null) {
                    updateResources(resources, assetM);
                    resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                }
            }
        }
    }

    /**
     * 更新Resources
     *
     * @param resources 待更新Resources
     * @param assetM    替换值
     */
    private void updateResources(Resources resources, AssetManager assetM)
            throws NoSuchFieldException, IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6.0
            Field field1 = resources.getClass().getDeclaredField("mResourcesImpl");
            field1.setAccessible(true);
            Object resourcesImpl = field1.get(resources);

            Field field2 = resourcesImpl.getClass().getDeclaredField("mAssets");
            field2.setAccessible(true);
            field2.set(resourcesImpl, assetM);
        } else {
            Field field1 = resources.getClass().getDeclaredField("mAssets");
            field1.setAccessible(true);
            field1.set(resources, assetM);
        }
    }

    /**
     * 获取已经加载过的 Activity
     *
     * @param context 上下文对象
     */
    private ArrayList<Activity> getActivities(Context context)
            throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Activity> list = new ArrayList<>();
        // LoadedApk
        Field mLoadedApkField = Application.class.getDeclaredField("mLoadedApk");
        mLoadedApkField.setAccessible(true);
        Object mLoadedApk = mLoadedApkField.get(context.getApplicationContext());
        if (mLoadedApk != null) {
            // ActivityThread
            Field mActivityThreadField = mLoadedApk.getClass().getDeclaredField("mActivityThread");
            mActivityThreadField.setAccessible(true);
            Object mActivityThread = mActivityThreadField.get(mLoadedApk);
            if (mActivityThread != null) {
                // ArrayMap<IBinder, ActivityClientRecord>
                Field mActivitiesField = mActivityThread.getClass().getDeclaredField("mActivities");
                mActivitiesField.setAccessible(true);
                // ActivityThread.ActivityClientRecord
                Object mActivities = mActivitiesField.get(mActivityThread);
                // 注意这里一定写成Map，低版本这里用的是HashMap，高版本用的是ArrayMap
                if (mActivities instanceof Map) {
                    Map<Object, Object> arrayMap = (Map<Object, Object>) mActivities;
                    for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                        Object value = entry.getValue();
                        Field activityField = value.getClass().getDeclaredField("activity");
                        activityField.setAccessible(true);
                        Object obj = activityField.get(value);
                        list.add((Activity) obj);
                    }
                }
            }
        }
        return list;
    }
}
