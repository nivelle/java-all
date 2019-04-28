package com.nivelle.guide.algorithms;

import com.nivelle.guide.algorithms.common.StackForQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StackForQueueTest {


    @Test
    public void testStackForQueue(){
        StackForQueue stackForQueue = new StackForQueue();

        stackForQueue.add(1);
        stackForQueue.add(2);
        stackForQueue.add(3);
        stackForQueue.add(4);
        stackForQueue.add(5);
        System.out.println(stackForQueue);

        stackForQueue.delete();

        System.out.println(stackForQueue);
    }
}
