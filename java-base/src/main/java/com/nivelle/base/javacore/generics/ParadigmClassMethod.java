package com.nivelle.base.javacore.generics;

/**
 * 范型类&范型方法
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class ParadigmClassMethod<T> {

    /**
     * 范型方法1
     *
     * <T>:表明是范型方法
     * <p>
     * T:表示是返回值
     *
     * @param <T>
     * @return
     */
    public <T> T getParam1(Integer params) {
        T result = (T) params;
        return result;
    }

    /**
     * 范型方法1变异
     *
     * <T>:表明是范型方法
     * <p>
     * T:表示是返回值
     *
     * @param <T>
     * @return
     */
    public <T> T getParam1Sub(T params) {
        T result = params;
        return result;
    }


    /**
     * 范型方法2变异
     *
     * @param <T>
     * @return
     */
    public <T> void getParam2(T params) {
        T result = params;
        System.out.println(result);
        return;
    }

    /**
     * 范型方法3
     *
     * @param <T>
     * @return
     */
    public <T> void getParam3(Integer params) {
        T result = (T) params;
        System.out.println(result);
        return;
    }


    /**
     * 非范型方法：只是一个使用了范型类的方法
     * <p>
     * 范型参数需要声明范型类型:返回值<T>或者使用范型类
     *
     * @param
     * @return
     */
    public void getParam4(T params) {
        T result = params;
        System.out.println(result);
        return;
    }

    /**
     * 如果在类中定义使用泛型的静态方法，需要添加额外的泛型声明（将这个方法定义成泛型方法）
     * 即使静态方法要使用泛型类中已经声明过的泛型也不可以
     *
     * @param t
     * @param <T>
     */
    public static <T> void showStaticType(T t) {
        System.out.println(t.getClass().getTypeName());
    }

    /**
     * <E>:声明了一个范型类型
     *
     * @param params
     * @param <E>
     * @return
     */
    public <E> String getParam5(E params) {
        E result = params;
        System.out.println(result);
        return "";
    }

    /**
     * 返回值是声明的范型类型
     *
     * @param params
     * @param <E>
     * @return
     */
    public <E> E getParam6(E params) {
        E result = params;
        System.out.println(result);
        return result;
    }


}
