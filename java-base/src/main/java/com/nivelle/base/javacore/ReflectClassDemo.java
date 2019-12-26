package com.nivelle.base.javacore;

import com.nivelle.base.pojo.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射
 *
 * @author fuxinzhong
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

    public static void main(String[] args) throws Exception {

        Class driver = Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println(driver);

        Class userClass = Class.forName("com.nivelle.spring.pojo.User");
        System.out.println("反射获取的Class对象:" + userClass);

        //获取指定参数类型的构造函数,然后创建
        Constructor constructor = userClass.getConstructor(int.class, String.class);
        User userInstance = (User) constructor.newInstance(12, "ReflectTest");
        System.out.println(userInstance.getName());

        User user = new User(13, "newConstructor");
        System.out.println("自定义实例获取的Class对象:" + user.getClass());
        System.out.println(user.getClass().equals(userClass));
        //获取实例对象的属性,专门修改
        Field filed = user.getClass().getDeclaredField("age");
        filed.setAccessible(true);
        filed.set(user, 14);
        System.out.println("修改属性后的对象:" + user);

        //包名
        System.out.println("包名:" + user.getClass().getPackage().getName());
        //完整类名
        System.out.println("完整类名:" + user.getClass().getName());

        //获取实例继承的对象或者实现的接口
        Class parentClass = user.getClass().getSuperclass();
        System.out.println("parent class:" + parentClass.getName());

        Class[] interfaces = user.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            System.out.println("实现的接口:" + interfaces[i].getName());
        }

        //通过反射方法调用类的方法
        Method method = user.getClass().getDeclaredMethod("showDetail", String.class);
        Object result = method.invoke(user, "fuck");
        System.out.println(result.toString());

        //获取类加载信息
        String classLoaderName = user.getClass().getClassLoader().getClass().getName();
        System.out.println("classLoader name is" + classLoaderName);
        String classLoaderNameParent = user.getClass().getClassLoader().getParent().getClass().getName();
        System.out.println("parent classLoader name is" + classLoaderNameParent);
        //如果父类为空则为bootStrapCloassLoader
        String classLoaderNameParent2 = user.getClass().getClassLoader().getParent().getParent().getClass().getName();
        System.out.println("parent classLoader name is" + classLoaderNameParent2);





    }
}
