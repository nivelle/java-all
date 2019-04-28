package com.nivelle.guide.designpatterns.strategy;

public class Main {

    public static void main(String[] args) {
        WorkStrategy workStrategyLeft = new LeftRead();

        WorkStrategy workStrategyRight = new RightRead();

        workStrategyLeft.readName("nivelle");
        System.out.println();
        workStrategyRight.readName("nivelle");
    }
}
