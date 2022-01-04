package cc.ibooker.zrunfix;

import android.app.Application;

import cc.ibooker.runfix.RunFix;

/**
 * @program: ZRunFix
 * @description: 自定义Application
 * @author: zoufengli01
 * @create: 2021-11-05 19:03
 **/
public class ZApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RunFix.getInstance(this).fix();
    }
}
