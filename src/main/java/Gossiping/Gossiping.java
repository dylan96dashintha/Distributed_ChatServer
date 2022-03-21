package Gossiping;

import java.io.IOException;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Iterator;

import Server.Server;
import Server.ServerState;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ClientHandler.User;
import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

public class Gossiping {

	private static final Logger logger = LogManager.getLogger(Gossiping.class);

	int serverCountForGossip = 1;

	public JSONObject createChatRoomGossipingMsg() {
		JSONObject msg = new JSONObject();
		String gossipID = UUID.randomUUID().toString();

		ConcurrentHashMap<String, ChatRoom> currentRooms = ServerState.getServerState().getChatRoomHashmap();
		List<String> chatRoomNames = new ArrayList<String>();
		for (ConcurrentHashMap.Entry<String, ChatRoom> e : currentRooms.entrySet()) {
			chatRoomNames.add(e.getValue().getRoomName());
		}
		msg.put("type", "gossiping").put("purpus", "gossiping-chatroom").put("id", gossipID).put("sent-time", new java.util.Date())
				.put("from", ServerState.getServerState().getServerName()).put("room-list", chatRoomNames);
		
		ServerState.getServerState().addGossipingID(gossipID);
		
		return msg;

	}

	public JSONObject createNewIdentityGossipingMsg() {
		JSONObject msg = new JSONObject();
		String gossipID = UUID.randomUUID().toString();

		ConcurrentLinkedQueue<User> userList = ServerState.getServerState().getIdentityList();
		
		List<String> identityNames = new ArrayList<String>();
		for (User user : userList) {
			identityNames.add(user.getName());
		}

		msg.put("type", "gossiping").put("purpus", "gossiping-new-identity").put("id", gossipID).put("sent-time", new java.util.Date())
				.put("from", ServerState.getServerState().getServerName()).put("identities", identityNames);
		
		ServerState.getServerState().addGossipingID(gossipID);
		
		return msg;
	}

	public JSONObject createLeaderChangedGossipingMsg() {
		JSONObject msg = new JSONObject();
		String gossipID = UUID.randomUUID().toString();
		return msg;
	}
	
	public void updateUsingChatRoomGossipingMsg(JSONObject obj) {
		logger.debug("updateUsingChatRoomGossipingMsg() " + obj.toString());
		
		ServerState currentServer = ServerState.getServerState();
		ConcurrentHashMap<String, String> otherServersChatRooms = currentServer.getOtherServersChatRooms();
		
		for (ConcurrentHashMap.Entry<String, String> e: otherServersChatRooms.entrySet()) {
			logger.debug("Before " + e.getKey() + " - " + e.getValue());
		}
		
		Iterator<ConcurrentHashMap.Entry<String, String>> iterator = otherServersChatRooms.entrySet().iterator();
		while (iterator.hasNext()) {
		    if (iterator.next().getValue().equals(obj.getString("from")))
		        iterator.remove();
		}
			
		JSONArray chatroomArray = obj.getJSONArray("room-list");
		
		for (int i=0; i<chatroomArray.length(); i++) {
			otherServersChatRooms.put(chatroomArray.getString(i), obj.getString("from"));
		}
		
		for (ConcurrentHashMap.Entry<String, String> e: otherServersChatRooms.entrySet()) {
			logger.debug("After " + e.getKey() + " - " + e.getValue());
		}
		
		ServerState.getServerState().setOtherServersChatRooms(otherServersChatRooms);
		
		for (ConcurrentHashMap.Entry<String, String> e: ServerState.getServerState().getOtherServersChatRooms().entrySet()) {
			logger.debug("Updated " + e.getKey() + " - " + e.getValue());
		}
		
		ServerState.getServerState().addGossipingID(obj.getString("id"));
				
	}
	
	public void updateUsingNewIdentityGossipingMsg(JSONObject obj) {
		logger.debug("updateUsingNewIdentityGossipingMsg() " + obj.toString());
		
		ServerState currentServer = ServerState.getServerState();
		ConcurrentHashMap<String, String> otherServersIdentities = currentServer.getOtherServersUsers();
		
		for (ConcurrentHashMap.Entry<String, String> e: otherServersIdentities.entrySet()) {
			logger.debug("Before " + e.getKey() + " - " + e.getValue());
		}
		
		Iterator<ConcurrentHashMap.Entry<String, String>> iterator = otherServersIdentities.entrySet().iterator();
		while (iterator.hasNext()) {
		    if (iterator.next().getValue().equals(obj.getString("from")))
		        iterator.remove();
		}
			
		JSONArray chatroomArray = obj.getJSONArray("identities");
		
		for (int i=0; i<chatroomArray.length(); i++) {
			otherServersIdentities.put(chatroomArray.getString(i), obj.getString("from"));
		}
		
		for (ConcurrentHashMap.Entry<String, String> e: otherServersIdentities.entrySet()) {
			logger.debug("After " + e.getKey() + " - " + e.getValue());
		}
		
		ServerState.getServerState().setOtherServersUsers(otherServersIdentities);
		
		for (ConcurrentHashMap.Entry<String, String> e: ServerState.getServerState().getOtherServersUsers().entrySet()) {
			logger.debug("Updated " + e.getKey() + " - " + e.getValue());
		}
		
		ServerState.getServerState().addGossipingID(obj.getString("id"));
	}
	
	public void updateUsingLeaderChangedGossipingMsg(JSONObject obj) {
		
	}
	public boolean sholdUpdateGossip(JSONObject obj) {
		// Check the received msg should spread
		// @param gossip msg in JSONObject
		// if gossip id contains, no need to update, otherwise should update
		if (ServerState.getServerState().isGossipingIDContains(obj.getString("id")))
			return false;
		return true;
	}
	
	public void spreadGossipMsg(JSONObject obj) throws IOException {
		ArrayList<Server> randomServers = getRandomServers();
		for (Server server : randomServers) {
			Sender.sendRespond(server.getServerSocketConnection(), obj);
		}
	}

	public ArrayList<Server> getRandomServers() {
		ConcurrentHashMap<String, Server> currentServers = ServerState.getServerState().getServersHashmap();
		String[] servers = new String[currentServers.size()];
		int j = 0;
		for (String s : currentServers.keySet()) {
			servers[j] = s;
			j++;
		}

		
		String currentServerName = ServerState.getServerState().getServerName();
		ArrayList<Server> randomServers = new ArrayList<Server>();
		int i = 0;
		while (i < serverCountForGossip) {
			String server = servers[new Random().nextInt(servers.length)];
			if ((!(server.equals(currentServerName))) && (!(randomServers.contains(currentServers.get(server))))) {
				i++;
				randomServers.add(currentServers.get(server));
			}
		}
		return randomServers;

	}

	public void sendServerBroadcast(JSONObject obj, ArrayList<Server> serverList){
        for (Server server : serverList) {
        	try {
				Sender.sendRespond(server.getServerSocketConnection(), obj);
			} catch (IOException e) {
				logger.debug("sendServerBroadcast is failed to : "+ server.getServerName());
				ServerState.getServerState().getSuspectList().put(server.getServerName(), "SUSPECTED");
			}
        }
    }
}
