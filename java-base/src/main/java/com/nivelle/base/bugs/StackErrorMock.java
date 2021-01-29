package com.nivelle.base.bugs;

/**
 * 栈益处
 */
public class StackErrorMock {

    private static int index =1;
    public void call(){
        index++;
        call();
    }

    public static void main(String []args){
        StackErrorMock stackErrorMock = new StackErrorMock();

        try
        {
            stackErrorMock.call();
        }catch (Throwable e){
            System.out.println("Stack deep : "+index);
            e.printStackTrace();
        }
    }
}
