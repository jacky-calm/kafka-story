package story.kv.master;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

import static story.helper.LogHelper.getLogger;

public class ZkConnection {
    private static Logger logger = getLogger(ZkConnection.class);
    private final CountDownLatch connectedSignal = new CountDownLatch(1);
    private ZooKeeper zkClient;
    public ZooKeeper connect(String host) throws Exception {
        zkClient = new ZooKeeper(host, 5000, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == Event.KeeperState.SyncConnected) {
                    logger.info("zk connected.");
                    connectedSignal.countDown();
                } else if (we.getState() == Event.KeeperState.Disconnected) {
                    logger.warn("zk disconnected connected.");
                } else if (we.getState() == Event.KeeperState.Expired) {
                    logger.warn("zk session expired.");
                }
            }
        });
        connectedSignal.await();
        return zkClient;
    }
    public void close() throws InterruptedException {
        zkClient.close();
    }
}
