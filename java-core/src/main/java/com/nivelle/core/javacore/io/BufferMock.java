package com.nivelle.core.javacore.io;

import java.nio.Buffer;

/**
 * java.nio.buffer
 *
 * @author fuxinzhong
 * @date 2020/05/24
 */
public class BufferMock {
    public static void main(String[] args) {
        /**
         * 核心属性:
         *
         * capacity:它表示一个 Buffer 包含的元素数量，它是非负且恒定不变的
         *
         * position:它是下一个要读或者写的元素的索引，它是非负的且不会超过 limit 的大小
         *
         * limit:它是可以读或者写的最后一个元素的索引，它是非负的且不会超过 capacity 的大小
         *
         * mark:当调用 reset() 方法被调用时，一个 Buffer 的 mark 值会被设定为当前的 position 值的大小
         *
         *
         * 一个新创建的 Buffer 具有以下几个性质:
         *
         * 1.它的 position 是 0;
         *
         * 2.mark 没有被定义（实际上是 -1);
         *
         * 3.Buffer 中每一个元素值都被初始化为 0;
         *
         * 4.而 limit 值可能是 0，也可能是其他值，这取决于这个 Buffer 的类型；
         *
         */

        Buffer buffer = null;

        /**
         * 核心方法:
         *
         *
         * ##flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。
         * 换句话说，position现在用于标记读的位置，limit表示之前写进了多少个byte、char等，现在也只能读取多少个byte、char等。
         *
         * public final Buffer flip() {
         *         limit = position;
         *         position = 0;
         *         mark = -1;
         *         return this;
         *     }
         *
         * ## Buffer.rewind()将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。
         * public final Buffer rewind() {
         *         position = 0;
         *         mark = -1;
         *         return this;
         * }
         *
         * ## 通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position
         * public final Buffer mark() {
         *         mark = position;
         *         return this;
         *     }
         *
         * ##
         * public final Buffer reset() {
         *         int m = mark;
         *         if (m < 0)
         *             throw new InvalidMarkException();
         *         position = m;
         *         return this;
         *     }
         */
    }
}
