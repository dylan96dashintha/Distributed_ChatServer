package Gossiping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import Messaging.Sender;
import Server.ChatRoom;
import Server.Server;
import Server.ServerState;

public class GossipingHandler {

	private static final Logger logger = LogManager.getLogger(GossipingHandler.class);

	public void sendChatRoomCreateGossip() throws IOException {
		Gossiping gossip = new Gossiping();
		JSONObject obj = gossip.createChatRoomGossipingMsg();
		gossip.spreadGossipMsg(obj);
	}
	
	
	public void sendNewIdentityGossip() throws IOException {
		Gossiping gossip = new Gossiping();
		JSONObject obj = gossip.createNewIdentityGossipingMsg();
		gossip.spreadGossipMsg(obj);
	}
	
	
	public void sendLeaderChangedGossip() throws IOException {
		
	}
	
	public void doGossiping(JSONObject msg) throws IOException {
		Gossiping gossiping = new Gossiping();
		switch (msg.getString("purpus")) {
		
		case "gossiping-chatroom":
			if (gossiping.sholdUpdateGossip(msg)) {
				gossiping.updateUsingChatRoomGossipingMsg(msg);				
				gossiping.spreadGossipMsg(msg);
			}	
			break;
			
		case "gossiping-new-identity":
			if (gossiping.sholdUpdateGossip(msg)) {
				gossiping.updateUsingNewIdentityGossipingMsg(msg);			
				gossiping.spreadGossipMsg(msg);
			}
			break;
			
		case "gossiping-leader-changed":
			if (gossiping.sholdUpdateGossip(msg)) {
				gossiping.updateUsingLeaderChangedGossipingMsg(msg);			
				gossiping.spreadGossipMsg(msg);
			}
			break;
		}
	}
	
	
	
	
	
	
	
	
	

	
}
