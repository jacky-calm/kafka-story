package story.kv.master;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import static story.helper.LogHelper.getLogger;

public class ZkController implements Runnable {
    private static Logger logger = getLogger(ZkController.class);
    public static final String CONTROLLER = "/controller";
    private final String brokerId;
    private ZooKeeper zkClient;
    private String host;

    public ZkController(String host, String brokerId) {
        this.host = host;
        this.brokerId = brokerId;
    }

    Watcher watcher = we -> {
        logger.info("got event of /controller:" + we);
        // 这里对视频的代码改进了一下
        if (we.getType() == Watcher.Event.EventType.NodeDeleted) {
            tryElectController();
        }
    };

    private void tryElectController() {
        try {
            if (this.zkClient == null) {
                this.zkClient = new ZkConnection().connect(host);
            }
            Stat exists = zkClient.exists(CONTROLLER, watcher);
            if (exists == null) {
                logger.info("no controller exists, try to be the controller ...");
                String data = String.format("{\"version\":1,\"brokerid\":%s,\"timestamp\":\"%d\"}", brokerId, System.currentTimeMillis());
                String s = zkClient.create(CONTROLLER, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                logger.info(s);
            } else {
                logger.info("controller exists, " + exists.toString());
                byte[] controllerData = zkClient.getData(CONTROLLER, false, null);
                String s = new String(controllerData, "UTF-8");
                logger.info("current controller is " + s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        tryElectController();
        while (true) {
            try {
                logger.info("mock doing controller job ... ");
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        ZkController zkController = new ZkController("localhost", args[0]);
        Thread thread = new Thread(zkController);
        thread.start();
    }
}
