package com.nivelle.core.javacore.lang.reflect;

import com.nivelle.core.pojo.User;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射
 *
 * @author nivelle
 * @date 2019/07/19
 */
public class ReflectClassDemo {

    /**
     * Java反射（Reflection）框架主要提供以下功能：
     *
     * 1. 在运行时判断任意一个对象所属的类；
     * 2. 运行时构造任意一个类的对象；
     * 3. 在运行时判断任意一个类所具有的成员变量和方法（通过反射甚至可以调用private方法 ）
     * 4. 在运行时调用任意一个对象的方法
     */

    /**
     * Java反射（Reflection）的主要用途
     * <p>
     * 1. 工厂模式：Factory类中用反射的话，添加了一个新的类之后，就不需要再修改工厂类Factory了
     * 2. 数据库JDBC中通过Class.forName(Driver).来获得数据库连接驱动
     * 3. 分析类文件：毕竟能得到类中的方法等等
     * 4. 访问一些不能访问的变量或属性：破解别人代码
     */
    @CallerSensitive
    public static void main(String[] args) throws Throwable {

        Class userClass = Class.forName(User.class.getName());
        System.out.println("根据类名字反射获取的Class对象:" + userClass);

        //获取指定参数类型的构造函数,然后创建
        Constructor constructor = userClass.getConstructor(Integer.class, String.class);
        User userInstance = (User) constructor.newInstance(12, "ReflectTest");
        System.out.println("通过参数获取指定的构造器，然后实例话对象:" + userInstance.getName());

        User user = new User(13, "newConstructor");
        System.out.println("自定义实例获取的Class对象:" + user.getClass());
        System.out.println(user.getClass().equals(userClass));
        //获取实例对象的属性,专门修改
        Field filed = user.getClass().getDeclaredField("age");
        filed.setAccessible(true);
        filed.set(user, 14);
        System.out.println("通过反射获取指定名字的属性，然后修改修:" + user);

        //包名
        System.out.println("包名:" + user.getClass().getPackage().getName());
        //完整类名
        System.out.println("完整类名:" + user.getClass().getName());

        //获取实例继承的对象或者实现的接口
        Class parentClass = user.getClass().getSuperclass();
        System.out.println("获取实例继承的对象或者实现的接口:" + parentClass.getName());

        Class[] interfaces = user.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            System.err.println("实现的接口:" + interfaces[i].getName());
        }

        //通过反射方法调用类的方法
        Method method = user.getClass().getDeclaredMethod("showDetail", String.class);
        Object result = method.invoke(user, "fuck");
        System.out.println("通过反射调用方法:" + result.toString());

        //获取类加载信息
        String classLoaderName = user.getClass().getClassLoader().getClass().getName();
        System.out.println("classLoader name is:" + classLoaderName);
        String classLoaderNameParent = user.getClass().getClassLoader().getParent().getClass().getName();
        System.out.println("parent classLoader name is:" + classLoaderNameParent);
        //如果父类为空则为bootStrapClassLoader
        Object classLoaderNameParent2 = user.getClass().getClassLoader().getParent().getParent();
        System.out.println("parent classLoader name is:" + classLoaderNameParent2);

        /**
         * Reflection.getCallerClass()此方法的调用者必须有权限，需要什么样的权限呢？
         * 1. 由bootstrap class loader加载的类可以调用
         * 2. 由extension class loader加载的类可以调用
         *
         * 用户路径的类加载都是由 application class loader 进行加载的,换句话说就是用户自定义的一些类中无法调用此方法
         *
         * Reflection.getCallerClass()方法调用所在的方法必须用@CallerSensitive进行注解，
         * 通过此方法获取class时会跳过链路上所有的有@CallerSensitive注解的方法的类，直到遇到第一个未使用该注解的类，避免了用Reflection.getCallerClass(int n) 这个过时方法来自己做判断。
         */

        /**
         * 0 和小于0  -   返回 Reflection类
         *
         * 1  -   返回自己的类
         *
         * 2  -    返回调用者的类
         *
         * 3. 4. ....层层上传。
         */
        Class callerClass = Reflection.getCallerClass(1);
        System.out.println("获取调用者的类:" + callerClass);
        Class callerClass2 = Reflection.getCallerClass(2);
        System.out.println("获取调用者的类2:" + callerClass2);

    }
}
