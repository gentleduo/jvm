package org.duo.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 重写ClassLoader的loadclass方法，模拟tomcat的热部署
 */
public class ClassReloading extends ClassLoader {

    /**
     * 重写loadClass打破双亲委派
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        File f = new File("D:\\intellij-workspace\\juc\\out\\production\\juc", name.replace(".", "\\").concat(".class"));
        //当目录中存在指定的class文件则加载，否则交给父ClassLoader加载
        if (!f.exists()) return super.loadClass(name);
        try {
            InputStream is = new FileInputStream(f);
            byte[] b = new byte[is.available()];
            is.read(b);
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.loadClass(name);
    }

    public static void main(String[] args) throws Exception {

        //相当于tomcat启动后生成classloder
        ClassReloading m = new ClassReloading();
        Class clazz = m.loadClass("org.duo.bytecode.ClassFormat");

        //当代码更新后会生成新的class文件，如果想实现热部署就必须重新加载更新后的class文件
        //所有这里先new一个新的classloder赋值给该WebApp的类加载器(相当于将原来的类加载器对象废除)，然后再重新loading指定路径下的class
        m = new ClassReloading();
        Class clazzNew = m.loadClass("org.duo.bytecode.ClassFormat");

        System.out.println(clazz == clazzNew);
    }
}
