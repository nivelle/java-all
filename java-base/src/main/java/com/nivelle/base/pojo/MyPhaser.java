package com.nivelle.base.pojo;

import java.util.concurrent.Phaser;

/**
 * 阶段控制器
 *
 * @author fuxinzhong
 * @date 2020/11/10
 */
public class MyPhaser extends Phaser {

    /**
     * 在各个阶段执行完成后的回调通知
     *
     * @return
     */
    @Override
    protected boolean onAdvance(int phase, int registeredParties) {
        switch (phase) {
            case 0:
                return studentArrived();
            case 1:
                return finishFirstExercise();
            case 2:
                return finishSecondExercise();
            case 3:
                return finishExam();
            default:
                return true;
        }
    }

    private boolean studentArrived() {
        System.out.println("学生准备好了,学生人数：" + getRegisteredParties());
        return false;
    }

    private boolean finishFirstExercise() {
        System.out.println("第一题所有学生做完");
        return false;
    }

    private boolean finishSecondExercise() {
        System.out.println("第二题所有学生做完");
        return false;
    }

    private boolean finishExam() {
        System.out.println("第三题所有学生做完，结束考试");
        return true;
    }
}