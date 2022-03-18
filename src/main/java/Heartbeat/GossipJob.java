package Heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.util.HashMap;
import java.util.Map;

import Server.Server;
import Server.ServerState;
import Messaging.ServerMessage;
import Gossiping.Gossiping;

public class GossipJob implements Job{
	
	private static final Logger logger = LogManager.getLogger(GossipJob.class);
	
    private ServerMessage serverMessage = ServerMessage.getInstance();
    private Gossiping gossiping = new Gossiping();
	
    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
    	JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String aliveErrorFactor = dataMap.get("aliveErrorFactor").toString();

        // first work on heart beat vector and suspect failure server list
        for (Server serverInfo : ServerState.getServerState().getServersHashmap().values()){
            String serverId = serverInfo.getServerName();
            String myServerId = ServerState.getServerState().getServerName();

            // get current heart beat count of a server
            Integer count = ServerState.getServerState().getHeartbeatCountList().get(serverId);
            
            // first update heart beat count vector
            if (serverId.equals(myServerId)) {
            	ServerState.getServerState().getHeartbeatCountList().put(serverId, 0); // reset my own vector always
            } else {
                // up count all others
                if (count == null) {
                	ServerState.getServerState().getHeartbeatCountList().put(serverId, 1);
                } else {
                	ServerState.getServerState().getHeartbeatCountList().put(serverId, count + 1);
                }
            }

            // FIX get the fresh updated current count again
            count = ServerState.getServerState().getHeartbeatCountList().get(serverId);

            if (count != null) {
                // if heart beat count is more than error factor
                if (count > Integer.parseInt(aliveErrorFactor)) {
                	ServerState.getServerState().getSuspectList().put(serverId, "SUSPECTED");
                } else {
                	ServerState.getServerState().getSuspectList().put(serverId, "NOT_SUSPECTED");
                }
            }

        }

        int numOfServers = ServerState.getServerState().getServersHashmap().size();

        if (numOfServers > 1) { // Gossip required at least 2 servers to be up

            // change concurrent hashmap to hashmap before sending
            HashMap<String, Integer> heartbeatCountList = new HashMap<>(ServerState.getServerState().getHeartbeatCountList());
            JSONObject gossipMessage = new JSONObject();
            gossipMessage = serverMessage.gossipMessage(ServerState.getServerState().getServerName(), heartbeatCountList);
            try {
            	gossiping.spreadGossipMsg(gossipMessage);
            	logger.info("send heartbeatcount is starting");
            } catch (Exception e){
            	logger.error("send heartbeatcount is failed : " + e);
            }
        }
	}
    
    public static void receiveMessages(JSONObject j_object) {
    	
    	JSONObject json = (JSONObject) j_object.get("heartbeatCountList");
    	Map<String, Object> gossipFromOthers = json.toMap();
    	
    	Integer a = (Integer) gossipFromOthers.get("s1");
    	        
        String fromServer = (String)j_object.get("serverId");
        
        logger.info("Receiving heartbeatcount : "+ j_object.get("heartbeatCountList").toString() + " from server: [" + fromServer.toString() + "]");

        //update the heartbeatcountlist by taking minimum
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = ServerState.getServerState().getHeartbeatCountList().get(serverId);
            Integer remoteHeartbeatCount = (Integer) gossipFromOthers.get(serverId);
            if (localHeartbeatCount != null && remoteHeartbeatCount < localHeartbeatCount) {
            	ServerState.getServerState().getHeartbeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        logger.info("Current cluster heart beat state is: " + ServerState.getServerState().getHeartbeatCountList());

        if (ServerState.getServerState().isLeaderElected() && ServerState.getServerState().getLeaderServer().getServerName().equals(ServerState.getServerState().getServerName())) {
            if (ServerState.getServerState().getHeartbeatCountList().size() < gossipFromOthers.size()) {
                for (String serverId : gossipFromOthers.keySet()) {
                    if (!ServerState.getServerState().getHeartbeatCountList().containsKey(serverId)) {
                    	ServerState.getServerState().getSuspectList().put(serverId, "SUSPECTED");
                    }
                }
            }
        }

    }
}
