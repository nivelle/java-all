package com.nivelle.base.jdk.jvm;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * 垃圾清理
 *
 * @author fuxinzhong
 * @date 2020/11/06
 */
public class CleanerDemo extends PhantomReference {
    @Override
    public Object get() {
        return super.get();
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean isEnqueued() {
        return super.isEnqueued();
    }

    @Override
    public boolean enqueue() {
        return super.enqueue();
    }

    public CleanerDemo(Object referent, ReferenceQueue q) {
        super(referent, q);
    }
}
