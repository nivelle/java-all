package com.nivelle.starter;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/02
 */
public class MyStarterService {
    public String sayWhat;
    public String toWho;

    public MyStarterService(String sayWhat, String toWho) {
        this.sayWhat = sayWhat;
        this.toWho = toWho;
    }

    public String say() {
        return this.sayWhat + ":" + this
                .toWho;
    }

}
