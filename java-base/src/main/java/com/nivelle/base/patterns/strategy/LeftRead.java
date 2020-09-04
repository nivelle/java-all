package com.nivelle.base.patterns.strategy;

public class LeftRead implements WorkStrategy {

    public void readName(String userName) {
        char nameCharacter[] = userName.toCharArray();

        for (int i = 0; i < nameCharacter.length; i++) {
            System.out.print(nameCharacter[i]);
        }


    }
}
