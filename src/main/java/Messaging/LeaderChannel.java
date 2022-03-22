package Messaging;

import Gossiping.GossipingHandler;
import Server.Server;

import Server.ServerState;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;



public class LeaderChannel {

	private static final Logger logger = LogManager.getLogger(LeaderChannel.class);

	public static boolean updateGlobalChatRooms() {
		logger.info("Start communicate with Leader");
		ServerState currentServer = ServerState.getServerState();
		Server leaderServer = currentServer.getServerByName(currentServer.getLeaderServer().getServerName());
		if (currentServer.getServerName().equals(currentServer.getLeaderServer().getServerName())) {
			logger.debug("Same Leader");
			return true;
		} else if (leaderServer.getServerSocketConnection() != null) {
			
			JSONObject msg = new JSONObject();
			String id = UUID.randomUUID().toString();
			msg.put("type", "global-chatrooms-request").put("id", id);
			try {
				Sender.sendRespond(leaderServer.getServerSocketConnection(), msg);
			} catch (IOException e) {
				// TODO leader down
				logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
				return false;
			}
			int c = 0;
			while ((!(ServerState.getServerState().checkChatRoomRequestCompleted(id))) && c<20) {
				try {

					TimeUnit.MILLISECONDS.sleep(200);
					c++;

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(ServerState.getServerState().checkChatRoomRequestCompleted(id)) {
				return true;
			}else {
				// TODO leader down
//				DO leader election
			logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
			return false;
			}

		} else {
			// TODO leader down
//				DO leader election
			logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
			return false;
		}

	}
	
	
	public static ConcurrentHashMap<String, String> getGlobalChatRooms(){
		boolean hasupdated = LeaderChannel.updateGlobalChatRooms();
		if (hasupdated)
			return ServerState.getServerState().getOtherServersChatRooms();
		return null;
	}
	
	public static boolean updateGlobalIdentities() {
		logger.debug("in updateGlobalIdentities");
		ServerState currentServer = ServerState.getServerState();
		Server leaderServer = currentServer.getServerByName(currentServer.getLeaderServer().getServerName());
		if (currentServer.getServerName().equals(currentServer.getLeaderServer().getServerName())) {
			logger.debug("Current servre "+ currentServer.getServerName() +" is the leader server.");
			return true;
		} else if (leaderServer.getServerSocketConnection() != null) {
			
			JSONObject msg = new JSONObject();
			String id = UUID.randomUUID().toString();
			msg.put("type", "global-identity-request").put("id", id);
			try {
				Sender.sendRespond(leaderServer.getServerSocketConnection(), msg);
			} catch (IOException e) {
				// TODO leader down
				logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
				return false;
			}
			int c = 0;
			while ((!(ServerState.getServerState().checkIdentityRequestCompleted(id))) && c<20) {
				try {
		
					TimeUnit.MILLISECONDS.sleep(200);
					c++;

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(ServerState.getServerState().checkIdentityRequestCompleted(id)) {
				logger.debug("Recieved response from leader server");
				return true;
			}else {
				// TODO leader down
//				DO leader election
			logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
			return false;
			}

		} else {
			// TODO leader down
//				DO leader election
			logger.error("Leader server " + currentServer.getLeaderServer().getServerName() + " is down");
			return false;
		}

	}
	
	public static ConcurrentHashMap<String, String> getGlobalIdentities(){
		boolean hasupdated = LeaderChannel.updateGlobalIdentities();
		if (hasupdated)
			return ServerState.getServerState().getOtherServersUsers();
		return null;
	}
}
