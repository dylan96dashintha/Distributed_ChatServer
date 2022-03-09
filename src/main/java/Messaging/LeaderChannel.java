package Messaging;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import Gossiping.GossipingHandler;
import Server.Server;

import Server.ServerState;

public class LeaderChannel {

	private static final Logger logger = LogManager.getLogger(LeaderChannel.class);

	public static boolean getGlobalChatRooms() {
		
		ServerState currentServer = ServerState.getServerState();
		Server leaderServer = currentServer.getServerByName(currentServer.getLeaderServer().getServerName());
		if (currentServer.getServerName().equals(currentServer.getLeaderServer().getServerName())) {
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
			while ((!(ServerState.getServerState().checkChatRoomRequestCompleted(id))) || c>20) {
				try {
					TimeUnit.MILLISECONDS.sleep(200);
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
}
