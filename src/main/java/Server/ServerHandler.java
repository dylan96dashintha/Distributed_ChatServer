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
	
	public void newServerConnection(Socket socket, JSONObject response) throws IOException {
		
		Server server = new Server(response.getString("server"), response.getString("server-address"), response.getInt("server-port"), response.getInt("client-port"));
		
		server.setServerSocketConnection(socket);
		ServerState.getServerState().replaceServerbByName(server);
		logger.debug("LISTENING....." + ServerState.getServerState().getServersHashmap().toString());
		
		logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+server.getServerName());
		JSONObject obj = new JSONObject();
		obj.put("type","server-connection-response").put("connected", true).put("server", ServerState.getServerState().getServerName());
		Sender.sendRespond(socket, obj);
		logger.info("Message sent");
	}
	
	public void newServerConnectionConfirm(JSONObject response) {
		if (response.getBoolean("connected")) {
			logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+ response.getString("server"));
			//for testing
//			GossipingHandler gh = new GossipingHandler();
//			try {
//				gh.sendChatRoomCreateGossip();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
			i++;
		}
		msg.put("chatrooms", objArry);
		logger.info("respond "+msg.toString()+ " " + socket.getPort());
		Sender.sendRespond(socket, msg);
		logger.info("respond sent"+msg.toString());
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

	public void getGlobalIdentities(Socket socket, JSONObject response) throws IOException {
		JSONObject msg = new JSONObject();
		msg.put("type", "global-identity-response").put("id", response.getString("id"));
		JSONObject[] objArry = new JSONObject[ServerState.getServerState().getOtherServersUsers().size()];
		int i=0;
		for (ConcurrentHashMap.Entry<String, String> e: ServerState.getServerState().getOtherServersUsers().entrySet()) {
			objArry[i] = new JSONObject().put("user", e.getKey()).put("server", e.getValue());
			i++;
		}
		msg.put("identities", objArry);
		Sender.sendRespond(socket, msg);

	}
	
	public void setGlobalIdentities(JSONObject response) throws IOException {
		ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
		response.getJSONArray("identities");
		for (int i = 0; i < response.getJSONArray("identities").length(); i++) {
			users.put(
					response.getJSONArray("identities").getJSONObject(i).getString("user"),
					response.getJSONArray("identities").getJSONObject(i).getString("server"));
			}
		ServerState.getServerState().setOtherServersUsers(users);
		ServerState.getServerState().addIdentityRequesID(response.getString("id"));
	}
	
	

	
	
}
