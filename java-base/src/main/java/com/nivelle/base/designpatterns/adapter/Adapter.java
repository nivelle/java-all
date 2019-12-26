package com.nivelle.base.designpatterns.adapter;

public class Adapter implements Target {


    private Print printBanner;


    public Adapter(Print printBanner) {

        this.printBanner = printBanner;
    }

    @Override
    public String showAround(String head, String tail) {

        String haveTail = printBanner.printFix(tail);

        String result = "head" + haveTail;
        return result;

    }


}
