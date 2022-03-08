package cc.banzhi.runfix.fix;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;

import cc.banzhi.runfix.utils.FileUtil;


/**
 * @program: RunFix
 * @description: 修复执行类
 * 1. 修复Class，补丁包为dex或jar
 * 2. 修复SO，补丁包为普通文件夹，在该文件内SO库是在armeabi，armeabi-v7a，x86，mips，arm64- v8a，mips64，x86_64文件夹下
 * 3. 修复资源，补丁包为Apk
 * @author: zoufengli01
 * @create: 2021-11-05 11:12
 **/
public class FixExecutor implements IRunFix {

    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        // rar tar gz zip 7z，建议子线程执行
        if (!TextUtils.isEmpty(dirPath)
                && (dirPath.toLowerCase().endsWith(".zip") || dirPath.toLowerCase().endsWith(".rar"))) {
            // 解压ZIP
            String destDir = FileUtil.getDefaultDir(context);
            if (TextUtils.isEmpty(destDir)) {
                destDir = dirPath;
            }
            try {
                FileUtil.unZip(dirPath, destDir);
                dirPath = destDir;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 修复Class，dex或jar
        new DexFix().fix(context, dirPath, callBack);
        // 修复SO，普通文件夹
        new SoFix().fix(context, dirPath, callBack);
        // 修复资源，Apk
        new ResFix().fix(context, dirPath, callBack);
    }
}
