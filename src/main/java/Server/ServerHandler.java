package Server;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ClientHandler.ClientHandler;
import Gossiping.GossipingHandler;
import Messaging.Sender;
import Server.ServerState;



public class ServerHandler {
	
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);
	
	public void newServerConnection(Socket socket, String serveName) throws IOException {
		Server server = ServerState.getServerState().getServerByName(serveName);
		server.setServerSocketConnection(socket);
		ServerState.getServerState().replaceServerbByName(server);
		logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+serveName);
		JSONObject obj = new JSONObject();
		obj.put("type","server-connection-response").put("connected", true).put("server", ServerState.getServerState().getServerName());
		Sender.sendRespond(socket, obj);
		logger.info("Message sent");
	}
	
	public void newServerConnectionConfirm(JSONObject response) {
		if (response.getBoolean("connected")) {
			logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+ response.getString("server"));
			//for testing
			GossipingHandler gh = new GossipingHandler();
			try {
				gh.sendChatRoomCreateGossip();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			logger.info("Current server " +ServerState.getServerState().getServerName()+ " connection FAILED with server "+ response.getString("server"));
		}
	}
	
	
}
