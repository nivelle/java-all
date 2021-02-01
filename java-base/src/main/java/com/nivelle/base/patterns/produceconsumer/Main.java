package com.nivelle.base.patterns.produceconsumer;

/**
 * 生成者消费者模式的优势：
 *
 * 1. 解耦
 *
 * 2. 支持异步，并且能够平衡生产者和消费者的速度差异;生产者 - 消费者模式恰好能支持你用适量的线程
 *
 * 3. 支持批量执行以提升性能
 *
 * 4. 支持分阶段提交以提升性能
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class Main {
    public static void main(String[] args) {
        Table table = new Table(3);
        new MakerThread("MakerThread-1", table, 31415).start();
        new MakerThread("MakerThread-2", table, 92653).start();
        new MakerThread("MakerThread-3", table, 58979).start();
        new EaterThread("EaterThread-1", table, 32384).start();
        new EaterThread("EaterThread-2", table, 62643).start();
        new EaterThread("EaterThread-3", table, 38327).start();
    }
}
