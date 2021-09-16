package com.nivelle.core.javacore.lang.invoke;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/09
 */
import java.lang.invoke.*;

class Horse {
    public void race() {
        System.out.println("Horse.race()");
    }
}

class Deer {
    public void race() {
        System.out.println("Deer.race()");
    }
}

// javac Circuit.java
// java Circuit
public class Circuit {

    public static void startRace(Object obj) {
        // aload obj
        // invokedynamic race()
    }

    public static void main(String[] args) {
        startRace(new Horse());
        startRace(new Deer());
    }

    public static CallSite bootstrap(MethodHandles.Lookup l, String name, MethodType callSiteType) throws Throwable {
        MethodHandle mh = l.findVirtual(Horse.class, name, MethodType.methodType(void.class));
        return new ConstantCallSite(mh.asType(callSiteType));
    }
}
