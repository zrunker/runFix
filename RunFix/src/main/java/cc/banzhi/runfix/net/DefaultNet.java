package cc.banzhi.runfix.net;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @program: RunFix
 * @description: 网络请求类(HttpURLConnection)
 * @author: zoufengli01
 * @create: 2021-11-05 15:23
 **/
public class DefaultNet implements INet {

    @Override
    public void doDownLoad(@NonNull String url, @NonNull String dirPath)
            throws IOException {
        URL uRL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uRL.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(150000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();

            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(dirPath, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();
        }
    }
}
