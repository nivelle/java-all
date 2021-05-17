package com.nivelle.core.jdk.lang.annotion.inherited;

@DBTable2(name = " i am sub")
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

    protected int subProtedMethod() {
        return 0;
    }


    public int subPubliceMethod() {
        return 0;
    }
}
