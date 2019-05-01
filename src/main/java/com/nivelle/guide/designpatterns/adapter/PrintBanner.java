package com.nivelle.guide.designpatterns.adapter;

public class PrintBanner extends Banner implements Print {


    public String printFix(String title) {

        String result = super.showWithTail(title);
        System.out.println(result);

        return result;

    }
}
