package cc.banzhi.runfix.fix;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * @program: RunFix
 * @description: 热修复接口
 * @author: zoufengli01
 * @create: 2021-11-04 20:25
 **/
public interface IRunFix {

    /**
     * 修复文件
     *
     * @param context  上下文对象
     * @param dirPath  待修复文件夹地址
     * @param callBack 回调
     */
    void fix(@NonNull Context context, String dirPath, IFixCallBack callBack);
}
