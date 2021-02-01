package com.nivelle.base.patterns.balking;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 1. GuardedObject参与者是一个拥有被警戒的方法(guardedMethod)的类。
 * 2. 当线程执行guardedMethod时，只有满足警戒条件时，才会继续执行，否则会立即返回。
 * 3. 警戒条件的成立与否，会随着GuardedObject参与者的状态而变化。
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class Data {
    private String filename;     // 文件名
    private String content;      // 数据内容
    private boolean changed;     // 标识数据是否已修改

    public Data(String filename, String content) {
        this.filename = filename;
        this.content = content;
        this.changed = true;
    }

    // 修改数据,同时设置标识为true
    public synchronized void change(String newContent) {
        content = newContent;
        changed = true;
    }

    // 若数据有修改，则保存，否则直接返回
    // 设置修改标示为false
    public synchronized void save() throws IOException {
        if (!changed) {
            System.out.println(Thread.currentThread().getName() + " balks");
            return;
        }
        doSave();
        changed = false;
    }

    private void doSave() throws IOException {
        System.out.println(Thread.currentThread().getName() + " calls doSave, content = " + content);
        Writer writer = new FileWriter(filename);
        writer.write(content);
        writer.close();
    }
}
