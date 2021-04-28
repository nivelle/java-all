package com.nivelle.rpc.netty.protocol;


public class ClientTask implements Runnable {

    private static volatile int count = 1;

    /**
     *
     */
    public ClientTask() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            ProtocolClient client = new ProtocolClient("localhost", 8082);

            client.run();

            System.out.println("第" + count + "个客户端启动了！！");
            count++;

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
