package com.nivelle.base.patterns.responsibilitychain;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 责任链模式
 *
 * @author nivellefu
 */
public class Main {

    public static void main(String[] args) {
        //HandlerUser oneHandler = new OneHandler(1);
        HandlerUser twoHandler = new TwoHandler(2);
        HandlerUser threeHandler = new ThreeHandler(3);
        HandlerUser fiveHandler = new ThreeHandler(4);

//        oneHandler.setNext(twoHandler);
//        twoHandler.setNext(threeHandler);
//
//        oneHandler.chainResolve();
        List<HandlerUser> list = Lists.newArrayList();
        //list.add(oneHandler);
        list.add(twoHandler);
        list.add(threeHandler);
        list.add(fiveHandler);

        HandlerUser firstLimit = list.get(0);
        HandlerUser beforeLimit = firstLimit;
        for (int i = 0; i < list.size(); i++) {
            int index = i;
            int nextIndex = index + 1;
            if (nextIndex < list.size()) {
                HandlerUser nextLimit = list.get(nextIndex);
                beforeLimit.setNext(nextLimit);
                beforeLimit = nextLimit;
            }
        }
        System.out.println(firstLimit);
    }
}
