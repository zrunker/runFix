package cc.banzhi.runfix.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cc.banzhi.runfix.Config;

/**
 * @program: RunFix
 * @description: 文件管理类
 * @author: zoufengli01
 * @create: 2021-11-05 14:22
 **/
public class FileUtil {

    /**
     * 默认修复文件目录
     *
     * @param context 上下文对象
     */
    public static String getDefaultDir(@NonNull Context context) {
        String dirPath = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + File.separator + Config.DEFAULT_FIX_DIR;
        File dirFile = new File(dirPath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            if (!dirFile.mkdirs()) {
                return null;
            }
        }
        return dirPath;
    }

    /**
     * 默认修复文件解压目录
     *
     * @param context 上下文对象
     */
    public static String getDefaultOptimizeDir(@NonNull Context context) {
        // 获取Dex文件解压目录
        String optimizeDirPath = context.getFilesDir().getAbsolutePath()
                + File.separator + Config.OPTIMIZE_DEX_DIR;
        File dexLibDir = new File(optimizeDirPath);
        if (!dexLibDir.exists() || !dexLibDir.isDirectory()) {
            if (!dexLibDir.mkdirs()) {
                return null;
            }
        }
        return optimizeDirPath;
    }

    /**
     * 获取单个文件的MD5值
     *
     * @param file 待验证文件
     */
    public static String getFileMD5(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int len;
            fis = new FileInputStream(file);
            while ((len = fis.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            fis.close();
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 复制单个文件（复制文件内容）-子线程
     *
     * @param oldPath String 原文件路径 如：c:/abc.txt
     * @param newPath String 复制后路径 如：f:/abc.txt
     */
    public static void copyFile(@NonNull String oldPath, @NonNull String newPath) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            File oldFile = new File(oldPath);
            if (oldFile.exists() && oldFile.isFile()) {
                is = new FileInputStream(oldPath);
                fos = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024 * 8];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制整个文件夹内容-子线程
     *
     * @param oldPath String 原文件路径 如：c:/abc
     * @param newPath String 复制后路径 如：f:/bcd
     */
    public static void copyFolder(@NonNull String oldPath, @NonNull String newPath) {
        File newFile = new File(newPath);
        boolean bool = newFile.exists();
        if (!bool) {
            bool = newFile.mkdirs();
        }
        if (bool) {
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            if (files != null && files.length > 0) {
                File temp;
                for (String file : files) {
                    if (oldPath.endsWith(File.separator)) {
                        temp = new File(oldPath + file);
                    } else {
                        temp = new File(oldPath + File.separator + file);
                    }
                    if (temp.isFile()) {
                        FileInputStream fis = null;
                        FileOutputStream fos = null;
                        try {
                            fis = new FileInputStream(temp);
                            fos = new FileOutputStream(
                                    newPath + File.separator + temp.getName());
                            byte[] b = new byte[1024 * 8];
                            int len;
                            while ((len = fis.read(b)) != -1) {
                                fos.write(b, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null)
                                try {
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (temp.isDirectory()) {
                        copyFolder(oldPath + File.separator + file,
                                newPath + File.separator + file);
                    }
                }
            }
        }
    }

    /**
     * 解压文件
     *
     * @param srcPath  待解压路径
     * @param destPath 目标路径
     */
    private void unZip(String srcPath, String destPath) throws IOException {
        File file = new File(srcPath);
        if (!file.exists()) {
            throw new RuntimeException(srcPath + "所指文件不存在");
        }
        ZipFile zf = new ZipFile(file);
        Enumeration<?> entries = zf.entries();
        ZipEntry entry;
        while (entries.hasMoreElements()) {
            entry = (ZipEntry) entries.nextElement();
            System.out.println("解压" + entry.getName());
            if (entry.isDirectory()) {
                String dirPath = destPath + File.separator + entry.getName();
                File dir = new File(dirPath);
                dir.mkdirs();
            } else {
                // 表示文件
                File f = new File(destPath + File.separator + entry.getName());
                if (!f.exists()) {
                    String dirs = f.getParent();
                    if (dirs != null) {
                        File parentDir = new File(dirs);
                        parentDir.mkdirs();
                    }
                }
                f.createNewFile();
                // 将压缩文件内容写入到这个文件中
                InputStream is = zf.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);
                int count;
                byte[] buf = new byte[8192];
                while ((count = is.read(buf)) != -1) {
                    fos.write(buf, 0, count);
                }
                is.close();
                fos.close();
            }
        }
    }

}