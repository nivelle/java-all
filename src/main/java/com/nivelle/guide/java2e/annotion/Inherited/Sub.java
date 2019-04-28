package com.nivelle.guide.java2e.annotion.Inherited;

@DBTable2
class Sub extends Super {
    private int subPrivateField;
    public int subPublicField;

    private Sub() {
    }

    public Sub(int i) {
    }

    private int subPrivateMethod() {
        return 0;
    }

    public int subPubliceMethod() {
        return 0;
    }
}
