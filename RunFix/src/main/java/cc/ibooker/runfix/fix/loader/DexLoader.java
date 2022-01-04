package cc.ibooker.runfix.fix.loader;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import cc.ibooker.runfix.utils.FileUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @program: RunFix
 * @description: Dex文件Loader
 * @author: zoufengli01
 * @create: 2021-11-05 10:37
 **/
public class DexLoader implements ILoader {
    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";

    @Override
    public Set<File> load(@NonNull Context context, String dirPath) {
        // 定义集合
        Set<File> dexFiles = new HashSet<>();
        // 获取修复目录路径
        String fixPath = FileUtil.getDefaultDir(context);
        if (!TextUtils.isEmpty(dirPath)) {
            File dirFile = new File(dirPath);
            if (dirFile.exists() && dirFile.isDirectory()) {
                fixPath = dirFile.getAbsolutePath();
            }
        }
        // 执行文件筛选
        if (!TextUtils.isEmpty(fixPath)) {
            assert fixPath != null;
            File file = new File(fixPath);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File item : files) {
                        if (item != null && item.exists() && item.isFile()) {
                            String name = item.getName().toLowerCase(Locale.ROOT);
                            if (name.endsWith(DEX_SUFFIX)
                                    || name.endsWith(APK_SUFFIX)
                                    || name.endsWith(JAR_SUFFIX)
                                    || name.endsWith(ZIP_SUFFIX)) {
                                dexFiles.add(item);
                            }
                        }
                    }
                }
            }
        }
        return dexFiles;
    }
}
