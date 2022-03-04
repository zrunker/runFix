package cc.banzhi.runfix.fix;

/**
 * @program: RunFix
 * @description: 修复回调
 * @author: zoufengli01
 * @create: 2021-11-05 14:04
 **/
public interface IFixCallBack {

    void onError(@FixType String type, String msg);
}
