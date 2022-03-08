package cc.banzhi.runfix.fix.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cc.banzhi.runfix.utils.FileUtil;

/**
 * @program: ZRunFix
 * @description: So库加载器
 * @author: zoufengli01
 * @create: 2021/12/30 5:07 下午
 **/
public class SoLoader implements ILoader {

    @Override
    public Set<File> load(@NonNull Context context, String dirPath) {
        // 定义集合
        Set<File> soDirs = new HashSet<>();
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
            // 过滤条件
            List<String> cpuAbis = Arrays.asList(cpuAbi(context));
            assert fixPath != null;
            traverseDirFile(new File(fixPath), cpuAbis, soDirs);
        }
        return soDirs;
    }

    /**
     * 遍历文件夹
     *
     * @param dirFile 目标文件夹
     * @param cpuAbis 支持CPU架构
     * @param soDirs  保存目录集合
     */
    private void traverseDirFile(File dirFile,
                                 List<String> cpuAbis, Set<File> soDirs) {
        if (dirFile.exists() && dirFile.isDirectory()) {
            File[] files = dirFile.listFiles();
            if (files != null && files.length > 0) {
                for (File item : files) {
                    if (item != null && item.exists() && item.isDirectory()) {
                        String name = item.getName().toLowerCase(Locale.ROOT);
                        if (cpuAbis.contains(name)) {
                            soDirs.add(item);
                        } else {
                            traverseDirFile(item, cpuAbis, soDirs);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取当前系统CPU架构
     */
    private String[] cpuAbi(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
                if (applicationInfo != null) {
                    Field field = applicationInfo.getClass().getDeclaredField("primaryCpuAbi");
                    field.setAccessible(true);
                    String primaryCpuAbi = (String) field.get(applicationInfo);
                    if (primaryCpuAbi != null) {
                        return new String[]{primaryCpuAbi};
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Build.SUPPORTED_ABIS;
        } else {
            return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
    }
}
