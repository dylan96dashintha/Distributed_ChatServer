package Gossiping;

import java.io.IOException;
import java.util.ArrayList;
import Server.Server;
import Server.ServerState;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

public class Gossiping {

	private static final Logger logger = LogManager.getLogger(Gossiping.class);

	int serverCountForGossip = 2;

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

		return msg;

	}

	public JSONObject createNewIdentityGossipingMsg() {
		JSONObject msg = new JSONObject();
		String gossipID = UUID.randomUUID().toString();
		return msg;
	}

	public JSONObject createLeaderChangedGossipingMsg() {
		JSONObject msg = new JSONObject();
		String gossipID = UUID.randomUUID().toString();
		return msg;
	}
	
	public void updateUsingChatRoomGossipingMsg(JSONObject obj) {
		
	}
	
	public void updateUsingNewIdentityGossipingMsg(JSONObject obj) {
		
	}
	
	public void updateUsingLeaderChangedGossipingMsg(JSONObject obj) {
		
	}
	public boolean sholdSpredGossip(JSONObject obj) {
		// Check the received msg should spread
		// @param gossip msg in JSONObject
		return false;
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
				logger.debug("Random selected server: "+currentServers.get(server).getServerName());
			}
		}
		logger.debug("Random Server Count: "+randomServers.size());
		return randomServers;

	}
}
