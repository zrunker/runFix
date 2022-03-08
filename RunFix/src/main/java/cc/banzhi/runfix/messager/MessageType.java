package cc.banzhi.runfix.messager;

import androidx.annotation.StringDef;

/**
 * @program: RunFix
 * @description: 消息类型
 * @author: zoufengli01
 * @create: 2021-11-05 14:22
 **/
@StringDef({MessageType.DOWNLOAD, MessageType.COPY_DIR, MessageType.COPY_FILE})
public @interface MessageType {
    String DOWNLOAD = "DOWNLOAD";
    String COPY_DIR = "COPY_DIR";
    String COPY_FILE = "COPY_FILE";
}
