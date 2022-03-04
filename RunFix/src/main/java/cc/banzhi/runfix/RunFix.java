package cc.banzhi.runfix;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import cc.banzhi.runfix.fix.FixExecutor;
import cc.banzhi.runfix.fix.FixType;
import cc.banzhi.runfix.fix.IFixCallBack;
import cc.banzhi.runfix.messager.MessageService;
import cc.banzhi.runfix.messager.MessageType;
import cc.banzhi.runfix.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * @program: RunFix
 * @description: 热修复入口
 * @author: zoufengli01
 * @create: 2021-11-04 20:21
 **/
public class RunFix {
    private volatile static RunFix instance;
    private final Context mContext;
    private final FixExecutor mFixExecutor;
    private IFixCallBack mFixCallBack = new IFixCallBack() {
        @Override
        public void onError(@FixType String type, String msg) {
            Log.d("RunFix", "type = " + type + " msg = " + msg);
        }
    };

    public static RunFix getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (RunFix.class) {
                if (instance == null) {
                    instance = new RunFix(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private RunFix(Context context) {
        this.mContext = context.getApplicationContext();
        this.mFixExecutor = new FixExecutor();
    }

    public RunFix setFixCallBack(IFixCallBack fixCallBack) {
        this.mFixCallBack = fixCallBack;
        return this;
    }

    /**
     * URL修复
     *
     * @param url 待修复包网络URL
     */
    public void fixUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(url);
            fixUrlList(list);
        } else {
            if (mFixCallBack != null) {
                mFixCallBack.onError(null, "url == null");
            }
        }
    }

    /**
     * URL集合修复
     *
     * @param urls 待修复包网络URL集合
     */
    public void fixUrlList(ArrayList<String> urls) {
        if (urls != null && urls.size() > 0) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("data", urls);
            intent.putExtra("type", MessageType.DOWNLOAD);
            MessageService.work(mContext, intent);
        } else {
            if (mFixCallBack != null) {
                mFixCallBack.onError(null, "urls == null || urls.size() == 0");
            }
        }
    }

    /**
     * 文件夹修复 - 不推荐
     *
     * @param dirPath 文件夹路径
     */
    public void fixDir2(String dirPath) {
        if (!TextUtils.isEmpty(dirPath)) {
            Intent intent = new Intent();
            intent.putExtra("data", dirPath);
            intent.putExtra("type", MessageType.COPY_DIR);
            MessageService.work(mContext, intent);
        } else {
            if (mFixCallBack != null) {
                mFixCallBack.onError(null, "dirPath == null");
            }
        }
    }

    /**
     * 文件夹修复
     *
     * @param dirPath 文件夹路径
     */
    public void fixDir(String dirPath) {
        if (!TextUtils.isEmpty(dirPath)) {
            File dirFile = new File(dirPath);
            if (dirFile.exists() && dirFile.isDirectory()) {
                mFixExecutor.fix(mContext, dirPath, mFixCallBack);
            } else {
                if (mFixCallBack != null) {
                    mFixCallBack.onError(null, "dirPath目录文件夹找不到");
                }
            }
        } else {
            if (mFixCallBack != null) {
                mFixCallBack.onError(null, "dirPath == null");
            }
        }
    }

    /**
     * 文件修复
     *
     * @param filePath 文件路径
     */
    public void fixFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            Intent intent = new Intent();
            intent.putExtra("data", filePath);
            intent.putExtra("type", MessageType.COPY_FILE);
            MessageService.work(mContext, intent);
        } else {
            if (mFixCallBack != null) {
                mFixCallBack.onError(null, "filePath == null");
            }
        }
    }

    /**
     * 直接执行修复
     */
    public void fix() {
        mFixExecutor.fix(mContext, null, mFixCallBack);
    }

    /**
     * 获取修复文件目录路径
     */
    public String fixDefaultDir() {
        return FileUtil.getDefaultDir(mContext);
    }
}
