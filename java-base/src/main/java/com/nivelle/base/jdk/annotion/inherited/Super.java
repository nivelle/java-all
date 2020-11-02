package com.nivelle.base.jdk.annotion.inherited;

@DBTable1(name = "i am super")
class Super {
    private int superPrivateFiled;
    public int superPublicFiled;

    public Super() {
    }

    private int superPrivateMethod() {
        return 0;
    }

    protected int superProtedMethod() {
        return 0;
    }

    public int superPubliceMethod() {
        return 0;
    }
}
