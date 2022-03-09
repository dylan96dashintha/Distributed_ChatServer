package Server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

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
	
	public void getGlobalChatRooms(Socket socket, JSONObject response) throws IOException {
		JSONObject msg = new JSONObject();
		msg.put("type", "global-chatrooms-response").put("id", response.getString("id"));
		JSONObject[] objArry = new JSONObject[ServerState.getServerState().getOtherServersChatRooms().size()];
		int i=0;
		for (ConcurrentHashMap.Entry<String, String> e: ServerState.getServerState().getOtherServersChatRooms().entrySet()) {
			objArry[i] = new JSONObject().put("room", e.getKey()).put("server", e.getValue());
		}
		msg.put("chatrooms", objArry);
		Sender.sendRespond(socket, msg);
	}
	
	public void setGlobalChatRooms(JSONObject response) throws IOException {
		ConcurrentHashMap<String, String> rooms = new ConcurrentHashMap<>();
		response.getJSONArray("chatrooms");
		for (int i = 0; i < response.getJSONArray("chatrooms").length(); i++) {
			rooms.put(
					response.getJSONArray("chatrooms").getJSONObject(i).getString("room"),
					response.getJSONArray("chatrooms").getJSONObject(i).getString("server"));
			}
		ServerState.getServerState().setOtherServersChatRooms(rooms);
		ServerState.getServerState().addChatRoomRequestID(response.getString("id"));
	}
	
	
}
