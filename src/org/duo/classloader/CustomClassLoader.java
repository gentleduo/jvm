package org.duo.classloader;

import org.duo.Hello;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * 通过继承ClassLoader的自定义classloder可以指定父classloder也可以使用默认的父classloder
 * 如果需要指定父classloder则初始化自定义的classloder时使用带参数的构造方法：ClassLoader(ClassLoader parent)
 * 如果使用默认的父classloder则使用无参的构造方法
 * 在ClassLoader中无参的构造方法，实际会调用this(checkCreateClassLoader(), getSystemClassLoader());
 * 而ClassLoader.getSystemClassLoader()返回的是AppClassLoader
 * 所以自定义的classloder一般默认的父classloder是AppClassLoader
 */
public class CustomClassLoader extends ClassLoader {

    @Override
    /**
     * 重写loadClass会打破双亲委派机制
     * 所以一般的自定义ClassLoader只要继承ClassLoader并重写findClass方法
     * 先将class文件以二进制的形式读入内存，然后再调用defineClass将二进制转化为类对象(byte[] -> Class clazz)
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File f = new File("D:\\intellij-workspace\\juc\\out\\production\\juc", name.replace(".", "\\").concat(".class"));
        try {
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b = 0;
            while ((b = fis.read()) != -1) {
                baos.write(b);
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

        ClassLoader l = new CustomClassLoader();

        //如果这个准备被加载的类：org.duo.Hello在当前classpath下，那么上面的findClass是执行不到的
        //因为org.duo.Hello会被CustomClassLoader的父加载器：sun.misc.Launcher$AppClassLoader加载进来
        Class clazz = l.loadClass("org.duo.Hello");
        Hello h = (Hello) clazz.newInstance();
        h.m();

//        //如果这个准备被加载的类：org.duo.bytecode.ClassFormat不在当前classpath，那么上面的findClass将被执行
//        //由于加载的类不在当前classpath中，所以需要反射才能执行被加载的类中的方法
//        Class clazz = l.loadClass("org.duo.bytecode.ClassFormat");
//        Method method = clazz.getMethod("print");
//        method.invoke(clazz.newInstance());

        System.out.println("ClassLoader.getSystemClassLoader ==>" + ClassLoader.getSystemClassLoader());
        System.out.println(clazz.getName() + "的类加载器为：" + clazz.getClassLoader());
    }
}
