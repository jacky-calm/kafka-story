package story.kv.master;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderKey;
import io.etcd.jetcd.election.LeaderResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

import java.util.concurrent.CompletableFuture;

import static story.helper.LogHelper.getLogger;

public class EtcdController implements Runnable {
    private static Logger logger = getLogger(EtcdController.class);
    private String brokerId;
    private String host;
    private Client client;

    public EtcdController(String host, String brokerId) {
        this.host = host;
        this.brokerId = brokerId;
    }

    private void tryElectController() {
        if (this.client == null) {
            this.client = new EtcdConnection().connect(host);
        }
        try {
            election();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void election() throws Exception {
        Election electionClient = client.getElectionClient();
        ByteSequence electionName = ByteSequence.from("controller".getBytes());

        long leaseId = grantLease();
        String data = String.format("{\"version\":1,\"brokerid\":%s,\"timestamp\":\"%d\"}", brokerId, System.currentTimeMillis());
        ByteSequence proposal = ByteSequence.from(data.getBytes());

        CompletableFuture<CampaignResponse> campaign = electionClient.campaign(electionName, leaseId, proposal);
        CampaignResponse campaignResponse = campaign.get();
        LeaderKey leader = campaignResponse.getLeader();
        logger.info("become the controller " + leader.getName().toString("UTF-8"));
        logCurrentLeder(electionClient, electionName);
    }

    private void logCurrentLeder(Election electionClient, ByteSequence electionName) throws Exception {
        CompletableFuture<LeaderResponse> leader1 = electionClient.leader(electionName);
        logger.info("current leader is " + new String(leader1.get().getKv().getValue().getBytes()));
    }

    private long grantLease() throws Exception {
        Lease leaseClient = client.getLeaseClient();
        CompletableFuture<LeaseGrantResponse> grant = leaseClient.grant(2);
        LeaseGrantResponse leaseGrantResponse = grant.get();
        long id = leaseGrantResponse.getID();
        leaseClient.keepAlive(id, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse value) {
                logger.info("lease keeps alive, time to live: "  + value.getTTL());
            }

            @Override
            public void onError(Throwable t) {
                logger.info("Exception!", t.fillInStackTrace());
            }

            @Override
            public void onCompleted() {
                logger.info("lease renewal completed!");
            }
        });
        return id;
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
        EtcdController controller = new EtcdController("http://localhost:2379", args[0]);
        Thread thread = new Thread(controller);
        thread.start();
    }
}
