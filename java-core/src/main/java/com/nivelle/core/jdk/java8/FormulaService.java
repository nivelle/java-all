package com.nivelle.core.jdk.java8;

/**
 * java8接口可以有默认实现方法
 */
public interface FormulaService {


    double calculate(int a);

    /**
     * @param a
     * @return
     */
    default double sqrt(int a) {//默认实现
        return Math.sqrt(a);
    }
}
