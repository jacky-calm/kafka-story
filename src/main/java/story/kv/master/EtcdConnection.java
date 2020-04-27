package story.kv.master;

import io.etcd.jetcd.Client;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;

import static story.helper.LogHelper.getLogger;

public class EtcdConnection {
    private static Logger logger = getLogger(EtcdConnection.class);
    private Client client;

    public Client connect(String host) {
        this.client = Client.builder().endpoints(host).build();
        return this.client;
    }

    public void close() {
        client.close();
    }
}
