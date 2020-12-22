package com.nivelle.base.jdk.jvm;

/**
 * 网络类加载器
 *
 * @author fuxinzhong
 * @date 2020/12/22
 */
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class NetworkClassLoader extends ClassLoader {

    private String rootUrl;

    public NetworkClassLoader(String rootUrl) {
        // 指定URL
        this.rootUrl = rootUrl;
    }

    // 获取类的字节码
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] getClassData(String className) {
        // 从网络上读取的类的字节
        String path = classNameToPath(className);
        try {
            URL url = new URL(path);
            InputStream ins = url.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead = 0;
            // 读取类文件的字节
            while ((bytesNumRead = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesNumRead);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String classNameToPath(String className) {
        // 得到类文件的URL
        return rootUrl + "/"
                + className.replace('.', '/') + ".class";
    }
}