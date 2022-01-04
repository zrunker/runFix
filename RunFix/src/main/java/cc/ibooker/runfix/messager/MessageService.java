package cc.ibooker.runfix.messager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import cc.ibooker.runfix.RunFix;
import cc.ibooker.runfix.net.DefaultNet;
import cc.ibooker.runfix.net.INet;
import cc.ibooker.runfix.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @program: RunFix
 * @description: 消息服务，子线程
 * @author: zoufengli01
 * @create: 2021-11-05 14:22
 **/
public class MessageService extends JobIntentService {
    private static final int JOB_ID = 1010;

    public static void work(@NonNull Context context, Intent intent) {
        enqueueWork(context, MessageService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String type = intent.getStringExtra("type");
        switch (type) {
            case MessageType.DOWNLOAD:
                download(intent.getStringArrayListExtra("data"));
                break;
            case MessageType.COPY_DIR:
                copyDir(intent.getStringExtra("data"));
                break;
            case MessageType.COPY_FILE:
                copyFile(intent.getStringExtra("data"));
                break;
        }
    }

    /**
     * 下载
     *
     * @param list URL集合
     */
    private void download(ArrayList<String> list) {
        if (list != null && list.size() > 0) {
            // 设置下载目录
            String dirPath = FileUtil.getDefaultDir(getApplicationContext());
            if (!TextUtils.isEmpty(dirPath)) {
                // 初始化下载
                INet iNet = new DefaultNet();
                // 执行下载
                for (String url : list) {
                    try {
                        assert dirPath != null;
                        iNet.doDownLoad(url, dirPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 执行修复
                RunFix.getInstance(getApplicationContext()).fix();
            }
        }
    }

    /**
     * 拷贝文件夹
     *
     * @param dirPath 待拷贝文件夹路径
     */
    private void copyDir(String dirPath) {
        if (!TextUtils.isEmpty(dirPath)) {
            File file = new File(dirPath);
            if (file.exists() && file.isDirectory()) {
                // 拷贝文件夹
                String newDirPath = FileUtil.getDefaultDir(getApplicationContext());
                if (!TextUtils.isEmpty(newDirPath)) {
                    assert newDirPath != null;
                    FileUtil.copyFolder(dirPath, newDirPath);
                    // 执行修复
                    RunFix.getInstance(getApplicationContext()).fix();
                }
            }
        }
    }

    /**
     * 拷贝文件
     *
     * @param filePath 待拷贝文件路径
     */
    private void copyFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                // 拷贝文件
                String dirPath = FileUtil.getDefaultDir(getApplicationContext());
                if (!TextUtils.isEmpty(dirPath)) {
                    // 判断新文件
                    String newPath = dirPath + File.separator + file.getName();
                    File newFile = new File(newPath);
                    if (newFile.exists() && newFile.isFile()) {
                        String md51 = FileUtil.getFileMD5(file);
                        String md52 = FileUtil.getFileMD5(newFile);
                        if (md51 == null || md51.equals(md52)) {
                            return;
                        }
                    }
                    // 复制文件
                    FileUtil.copyFile(filePath, newPath);
                    // 执行修复
                    RunFix.getInstance(getApplicationContext()).fix();
                }
            }
        }
    }

}
