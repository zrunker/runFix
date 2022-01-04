package cc.ibooker.runfix.fix.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @program: RunFix
 * @description: 加载器接口
 * @author: zoufengli01
 * @create: 2021-11-05 10:36
 **/
public interface ILoader {

    /**
     * 加载文件
     *
     * @param context 上下文对象
     * @param dirPath 待修复文件夹路径
     */
    Set<File> load(@NonNull Context context, String dirPath);
}
