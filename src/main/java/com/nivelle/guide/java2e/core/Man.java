package com.nivelle.guide.java2e.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class Man extends Human {

    private int score;

    public Man(int age, String name, int score) {
        super(age, name);
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
