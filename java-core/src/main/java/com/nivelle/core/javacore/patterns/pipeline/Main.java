package com.nivelle.core.javacore.patterns.pipeline;

/**
 * 管道模式
 *
 * @author fuxinzhong
 * @date 2020/07/07
 */
public class Main {

    public static void main(String[] args) {
        String handling = "aabb1122zzyy";

        StandardPipeline pipeline = new StandardPipeline();

        BasicElement basicElement = new BasicElement();

        SecondElement secondElement = new SecondElement();

        ThirdElement thirdElement = new ThirdElement();

        pipeline.setBasic(basicElement);
        pipeline.addElement(secondElement);
        pipeline.addElement(thirdElement);

        pipeline.getFirst().invoke(handling);


    }
}
