package cc.banzhi.runfix.net;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * @program: ZRunFix
 * @description: 网络请求接口
 * @author: zoufengli01
 * @create: 2021-11-05 15:25
 **/
public interface INet {

    void doDownLoad(@NonNull String url, @NonNull String dirPath) throws IOException;
}
