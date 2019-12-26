package com.nivelle.base.javacore.java8;


import org.springframework.stereotype.Service;

@Service("formulaService")
public class FormulaServiceImpl implements FormulaService {


    @Override
   public double calculate(int a){
       return Math.sqrt(a*100);
   }
}
