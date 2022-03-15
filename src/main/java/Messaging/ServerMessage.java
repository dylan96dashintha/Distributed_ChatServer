package Messaging;

import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;

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
}
