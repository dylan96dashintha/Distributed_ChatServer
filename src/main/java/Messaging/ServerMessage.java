package Messaging;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import Server.Server;

public class ServerMessage {
	
	private static ServerMessage instance = null;
	
	private ServerMessage() {
    }
	
	public static synchronized ServerMessage getInstance() {
        if (instance == null) instance = new ServerMessage();
        return instance;
    }
	
    public static JSONObject gossipMessage(String serverName, HashMap<String, Integer> heartbeatCountList) {
        // {"type":"gossip","serverId":"s1","heartbeatcountlist":{"s1":0,"s2":1,"s3":1,"s4":2}}
        JSONObject jsonObject = new JSONObject();
        String gossipID = UUID.randomUUID().toString();
        jsonObject.put("type", "heartbeat-gossip");
        jsonObject.put("serverId", serverName);
        jsonObject.put("id", gossipID);
        jsonObject.put("heartbeatCountList", heartbeatCountList);
        return jsonObject;
    }
    
    public static JSONObject startVoteMessage(String serverName, String suspectServerName) {
    	// {"type":"startVote","serverId":"s1","suspectServerId":"s2"}
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "startVote");
        jsonObject.put("serverId", serverName);
        jsonObject.put("suspectServerId", suspectServerName);
        return jsonObject;
    }
    
    public static JSONObject notifyServerDownMessage(String suspectServerName) {
        // {"type":"notifyserverdown", "serverid":"s2"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "notifyserverdown");
        jsonObject.put("serverId", suspectServerName);
        return jsonObject;
    }
    
    public static JSONObject answerVoteMessage(String suspectServerName, String vote, String votedBy){
        // {"type":"answervote","suspectserverid":"1","vote":"YES", "votedby":"1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "answervote");
        jsonObject.put("suspectServerId", suspectServerName);
        jsonObject.put("votedBy", votedBy);
        jsonObject.put("vote", vote);
        return jsonObject;
    }

    public static JSONObject heartbeatMessage(String sender) {
        // {"option": "heartbeat", "sender": "s1"}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "heartbeat");
        jsonObject.put("sender", sender);
        return jsonObject;
    }

	public static JSONObject startElectionRequestMessage(Server server, ConcurrentHashMap<String, Boolean> electionStatusHashMap) {
		// {"option": "electionProgressUpdate", "electionStatusHashMap": {"s1":false,"s2":false,"s3":true,"s4":false}}
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "electionProgressUpdate");
        jsonObject.put("newLeader", server);
        jsonObject.put("electionStatusHashMap", electionStatusHashMap);
        return jsonObject;
	}
}
