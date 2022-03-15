package Heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.concurrent.ThreadLocalRandom;

import Server.Server;
import Server.ServerState;
import Messaging.ServerMessage;
import Gossiping.Gossiping;

public class GossipJob implements Job{
	
	private static final Logger logger = LogManager.getLogger(GossipJob.class);
	
	private ServerState serverState = ServerState.getServerState();
    private Server leaderState = serverState.getLeaderServer();
    private ServerMessage serverMessage = ServerMessage.getInstance();
    private Gossiping gossiping = new Gossiping();
	
    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
    	JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String aliveErrorFactor = dataMap.get("aliveErrorFactor").toString(); //TODO : what is aliveErrorFactor

        // first work on heart beat vector and suspect failure server list
        for (Server serverInfo : serverState.getServersHashmap().values()){
            String serverId = serverInfo.getServerName();
            String myServerId = serverState.getServerName();

            // get current heart beat count of a server
            Integer count = serverState.getHeartbeatCountList().get(serverId);

            // first update heart beat count vector
            if (serverId.equals(myServerId)) {
                serverState.getHeartbeatCountList().put(serverId, 0); // reset my own vector always
            } else {
                // up count all others
                if (count == null) {
                    serverState.getHeartbeatCountList().put(serverId, 1);
                } else {
                    serverState.getHeartbeatCountList().put(serverId, count + 1);
                }
            }

            // FIX get the fresh updated current count again
            count = serverState.getHeartbeatCountList().get(serverId);

            if (count != null) {
                // if heart beat count is more than error factor
                if (count > Integer.parseInt(aliveErrorFactor)) {
                    serverState.getSuspectList().put(serverId, "SUSPECTED");
                } else {
                    serverState.getSuspectList().put(serverId, "NOT_SUSPECTED");
                }
            }

        }

        // next challenge leader election if a coordinator is in suspect list

//        if (leaderState.isLeaderElected()){
//
//            Integer leaderServerId = leaderState.getLeaderID();
//            System.out.println("Current coordinator is : " + leaderState.getLeaderID().toString());
//
//            // if the leader/coordinator server is in suspect list, start the election process
//            if (serverState.getSuspectList().get(leaderServerId).equals("SUSPECTED")) {
//
//                //initiate an election
//                BullyAlgorithm.initialize();
//            }
//        }

        // finally gossip about heart beat vector to a next peer

        int numOfServers = serverState.getServersHashmap().size();

        if (numOfServers > 1) { // Gossip required at least 2 servers to be up
        	
//            after updating the heartbeatCountList, randomly select a server and send
//            Integer serverIndex = ThreadLocalRandom.current().nextInt(numOfServers - 1);
//            ArrayList<Server> remoteServer = new ArrayList<>();
//            for (Server server : serverState.getServersHashmap().values()) {
//            	String serverId = server.getServerName();
//                String myServerId = serverState.getServerName();
//                if (!serverId.equals(myServerId)) {
//                    remoteServer.add(server);
//                }
//            }
            //Collections.shuffle(remoteServer, new Random(System.nanoTime())); // another way of randomize the list

            // change concurrent hashmap to hashmap before sending
            HashMap<String, Integer> heartbeatCountList = new HashMap<>(serverState.getHeartbeatCountList());
            JSONObject gossipMessage = new JSONObject();
            gossipMessage = serverMessage.gossipMessage(serverState.getServerName(), heartbeatCountList);
            try {
            	gossiping.spreadGossipMsg(gossipMessage);
//                MessageTransfer.sendServer(gossipMessage,remoteServer.get(serverIndex));
            	logger.debug("send heartbeatcount for two servers are starting");
            } catch (Exception e){
            	logger.error("send heartbeatcount is failed : " + e);
//                System.out.println("WARN : Server s"+remoteServer.get(serverIndex).getServerID() + " has failed");
            }

        }
		
	}
    
    public static void receiveMessages(JSONObject j_object) {

        ServerState serverState = ServerState.getServerState();

        HashMap<String, Integer> gossipFromOthers = (HashMap<String, Integer>) j_object.get("heartbeatCountList");
        String fromServer = (String)j_object.get("serverId");

        logger.debug("Receiving heartbeatcount : "+ gossipFromOthers + " from server: [" + fromServer.toString() + "]");

        //update the heartbeatcountlist by taking minimum
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = serverState.getHeartbeatCountList().get(serverId);
            Integer remoteHeartbeatCount = (Integer) gossipFromOthers.get(serverId);
            if (localHeartbeatCount != null && remoteHeartbeatCount < localHeartbeatCount) {
                serverState.getHeartbeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        logger.info("Current cluster heart beat state is: " + serverState.getHeartbeatCountList());

        if (serverState.getLeaderServer().isLeaderElected() && serverState.getLeaderServer().getServerName().equals(serverState.getServerName())) { //TODO : isLeaderElected yet to be implemented
            if (serverState.getHeartbeatCountList().size() < gossipFromOthers.size()) {
                for (String serverId : gossipFromOthers.keySet()) {
                    if (!serverState.getHeartbeatCountList().containsKey(serverId)) {
                        serverState.getSuspectList().put(serverId, "SUSPECTED");
                    }
                }
            }
        }

    }
}
