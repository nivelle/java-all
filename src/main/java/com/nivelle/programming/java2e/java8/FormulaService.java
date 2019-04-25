package com.nivelle.programming.java2e.java8;

public interface FormulaService {

    double calculate(int a);

    default double sqrt(int a){//默认实现
        return Math.sqrt(a);
    }
}
