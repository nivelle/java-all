package com.nivelle.base.enums;

/**
 * 枚举类
 *
 * @author fuxinzhong
 * @date 2019/06/19
 */
public enum MyEnum {

    ONE(1,"one"),
    TWO(2,"two"),
    THIRD(3,"third");

    private int type;
    private String desc;


    MyEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }


}
