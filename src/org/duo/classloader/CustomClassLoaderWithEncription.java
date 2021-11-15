package org.duo.classloader;

import org.duo.Hello;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 通过继承ClassLoader的自定义classloder可以指定父classloder也可以使用默认的父classloder
 * 如果需要指定父classloder则初始化自定义的classloder时使用带参数的构造方法：ClassLoader(ClassLoader parent)
 * 如果使用默认的父classloder则使用无参的构造方法
 * 在ClassLoader中无参的构造方法，实际会调用this(checkCreateClassLoader(), getSystemClassLoader());
 * 而ClassLoader.getSystemClassLoader()返回的是AppClassLoader
 * 所以自定义的classloder一般默认的父classloder是AppClassLoader
 */
public class CustomClassLoaderWithEncription extends ClassLoader {

    public static int seed = 0B10110110;

    @Override
    /**
     * 重写loadClass会打破双亲委派机制
     * 所以一般的自定义ClassLoader只要继承ClassLoader并重写findClass方法
     * 先将class文件以二进制的形式读入内存，然后再调用defineClass将二进制转化为类对象(byte[] -> Class clazz)
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File f = new File("D:\\intellij-workspace\\jvm\\out\\production\\jvm", name.replace("\\.", "\\").concat(".msbclass"));
        try {
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b = 0;
            while ((b = fis.read()) != -1) {
                //对读入的加密过的二进制再进行异或
                baos.write(b ^ seed);
            }
            byte[] bytes = baos.toByteArray();
            baos.close();
            fis.close();//可以写的更加严谨
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.findClass(name); //throws ClassNotFoundException
    }

    public static void main(String[] args) throws Exception {

        encFile("org.duo.Hello");
        ClassLoader l = new CustomClassLoaderWithEncription();

        //如果这个准备被加载的类：org.duo.Hello在当前classpath下，那么上面的findClass是执行不到的
        //因为org.duo.Hello会被CustomClassLoaderWithEncription的父加载器：sun.misc.Launcher$AppClassLoader加载进来
        Class clazz = l.loadClass("org.duo.Hello");
        Hello h = (Hello) clazz.newInstance();
        h.m();

        System.out.println("ClassLoader.getSystemClassLoader ==>" + ClassLoader.getSystemClassLoader());
        System.out.println(clazz.getName() + "的类加载器为：" + clazz.getClassLoader());
    }

    /**
     * 对生成的class文件进行加密
     * 采用最简单的方式：对二进制的每一位进行异或(对异或完的值再进行异或就相当于解密)
     *
     * @param name 类名
     * @throws Exception
     */
    private static void encFile(String name) throws Exception {
        File f = new File("D:\\intellij-workspace\\jvm\\out\\production\\jvm", name.replace(".", "\\").concat(".class"));
        FileInputStream fis = new FileInputStream(f);
        FileOutputStream fos = new FileOutputStream(new File("D:\\intellij-workspace\\jvm\\out\\production\\jvm", name.replace(".", "\\").concat(".msbclass")));
        int b = 0;
        while ((b = fis.read()) != -1) {
            fos.write(b ^ seed);
        }
        fis.close();
        fos.close();
    }
}
