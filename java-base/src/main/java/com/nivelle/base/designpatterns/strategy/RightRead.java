package com.nivelle.base.designpatterns.strategy;

public class RightRead implements WorkStrategy {

    public void readName(String userName) {
        char nameCharacter[] = userName.toCharArray();

        for (int i = nameCharacter.length - 1; i >= 0; i--) {
            System.out.print(nameCharacter[i]);
        }


    }
}
