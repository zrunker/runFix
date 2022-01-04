package cc.ibooker.runfix.fix;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;


/**
 * @program: RunFix
 * @description: 修复执行类
 * @author: zoufengli01
 * @create: 2021-11-05 11:12
 **/
public class FixExecutor implements IRunFix {

    @Override
    public void fix(@NonNull Context context, String dirPath, IFixCallBack callBack) {
        // 修复Class
        new DexFix().fix(context, dirPath, callBack);
        // 修复SO
        new SoFix().fix(context, dirPath, callBack);

//        new ResFix().fix(context, dirPath, callBack);
    }
}
