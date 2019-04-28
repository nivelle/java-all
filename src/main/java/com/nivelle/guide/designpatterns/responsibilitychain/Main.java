package com.nivelle.guide.designpatterns.responsibilitychain;

public class Main {

    public static void main(String[] args) {
        HandlerUser oneHandler = new OneHandler(1);
        HandlerUser twoHandler = new TwoHandler(2);
        HandlerUser threeHandler = new ThreeHandler(3);
        oneHandler.setNext(twoHandler);
        twoHandler.setNext(threeHandler);

        oneHandler.chainResolve();
    }
}
