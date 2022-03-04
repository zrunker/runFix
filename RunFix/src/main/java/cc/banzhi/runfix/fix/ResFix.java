package cc.banzhi.runfix.fix;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;


/**
 * @program: RunFix
 * @description: 资源文件修复类
 * @author: zoufengli01
 * @create: 2021-11-05 14:03
 **/
public class ResFix implements IRunFix {

    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        // TODO
        // AssetManager.class.getConstructor(new Class[0]).newInstance(new Object[0]);
        // setApkAssets/addAssetPathInternal(path, false /*overlay*/, false /*appAsLib*/)
        // 替换所有Activity中的Resource中的mAssets字段
        // ensureStringBlocks()
        // 替换Resources弱引用集合
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
                    @SuppressWarnings("unchecked")
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
