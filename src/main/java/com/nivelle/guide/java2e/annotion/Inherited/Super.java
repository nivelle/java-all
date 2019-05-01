package com.nivelle.guide.java2e.annotion.Inherited;

@DBTable1
class Super {
    private int superPrivateFiled;
    public int superPublicFiled;

    public Super() {
    }

    private int superPrivateMethod() {
        return 0;
    }

    public int superPubliceMethod() {
        return 0;
    }
}
