package story.kv.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class ZkController implements Runnable {
    static final Logger logger = LoggerFactory.getLogger(ZkController.class.getName());
    public static final String CONTROLLER = "/controller";
    private final String brokerId;
    private ZooKeeper zk;
    private String host;

    public ZkController(String host, String brokerId) {
        this.host = host;
        this.brokerId = brokerId;
    }

    Watcher watcher = new Watcher() {
        public void process(WatchedEvent we) {
            logger.info("got event of /controller:" + we);
            if (we.getState() == Event.KeeperState.SyncConnected) {
                electController();
            }
        }
    };

    private void electController() {
        try {
            Stat exists = zk.exists(CONTROLLER, watcher);
            if (exists == null) {
                logger.info("no controller exists, try to be the controller ...");
                String data = String.format("{\"version\":1,\"brokerid\":%s,\"timestamp\":\"%d\"}", brokerId, System.currentTimeMillis());
                String s = zk.create(CONTROLLER, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info(s);
            } else {
                logger.info(exists.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            zk = new ZooKeeper(host, 2000, new Watcher() {
                @Override
                public void process(WatchedEvent we) {
                    logger.info("default watch:" + we);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        connect();
        electController();
        while (true) {
            try {
                logger.info("sleep ... ");
                Thread.sleep(10000);
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
