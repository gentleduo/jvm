package org.duo.classformat;

public class SingletonInstance {

    /**
     * instance要加volatile修饰的原因；
     * 其实new在转换成字节码后会变成下面的new,dup,invokespecial,astore_1四条指令,而invokespecial和astore_1有可能发生指令重排，如果invokespecial和astore_1发生指令重排会出现下面的问题：
     * 现在有2个线程A,B
     * A在执行new SingletonInstance的时候，B线程进来，此时A执行了new和astore_1没有执行invokespecial，
     * 此时B线程判断instance不为null 直接返回一个未初始化的对象，就会出现问题
     */
    private volatile static SingletonInstance instance;

    private SingletonInstance() {
    }

    public static SingletonInstance getInstance() {
        // 不把锁加在方法上的原因：
        // 缩小锁的粒度，如果将锁加在方法上那么所有调用getInstance的线程都要去竞争锁，但是其实除了第一次初始化instance对象，
        // 其他的时候都可以通过空判断直接获得instance，所以同步块放在if判断之后
        if (instance == null) {
            synchronized (SingletonInstance.class) {
                // 这里还需要对instance做一次空判断的原因
                // 在上一个对instance做空判断之后，到给SingletonInstance.class加锁之间这段时间内，有可能有其他的线程调用了getInstance方法对instance进行了初始化操作
                if (instance == null) {
                    //0 new #3 <org/duo/classformat/SingletonInstance>
                    //3 dup
                    //4 invokespecial #4 <org/duo/classformat/SingletonInstance.<init> : ()V>
                    //7 astore_1
                    //8 return
                    instance = new SingletonInstance();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        SingletonInstance instance = new SingletonInstance();
    }
}
