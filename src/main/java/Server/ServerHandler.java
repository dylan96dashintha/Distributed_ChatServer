package Server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ClientHandler.ClientHandler;
import ClientHandler.User;
import Gossiping.GossipingHandler;
import Messaging.Sender;
import Server.ServerState;



public class ServerHandler {
	
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);
	
	public void newServerConnection(Socket socket, JSONObject response) throws IOException {
//		create server
		Server server = new Server(response.getString("server"), response.getString("server-address"), response.getInt("server-port"), response.getInt("client-port"));
		
		server.setServerSocketConnection(socket);
		ServerState.getServerState().replaceServerbByName(server);
		
		logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+server.getServerName());
//		set main hall
		ConcurrentHashMap<String, String> otherServersChatRooms = ServerState.getServerState().getOtherServersChatRooms();
		otherServersChatRooms.put(response.getString("mainhall"), response.getString("server"));
		ServerState.getServerState().setOtherServersChatRooms(otherServersChatRooms);
		
//		create response
		JSONObject obj = new JSONObject();    
		obj.put("type","server-connection-response").put("connected", true).put("server", ServerState.getServerState().getServerName()).put("leader-server",ServerState.getServerState().getLeaderServer().getServerName());
		ArrayList<String> chatrooms = new ArrayList<String>();
		for (ConcurrentHashMap.Entry<String, ChatRoom> e: ServerState.getServerState().getChatRoomHashmap().entrySet()) {
			chatrooms.add(e.getValue().getRoomName());
		}
		obj.put("chatrooms", chatrooms);
		ArrayList<String> users = new ArrayList<String>();
		for (User e: ServerState.getServerState().getIdentityList()) {
			users.add(e.getName());
		}
		obj.put("identity", users);
		
		Sender.sendRespond(socket, obj);

	}
	
	public void newServerConnectionConfirm(JSONObject response) {
		if (response.getBoolean("connected")) {
			Server leaderServer = ServerState.getServerState().getServerByName(response.getString("leader-server"));
			ServerState.getServerState().setLeaderServer(leaderServer);
			logger.info("Current server " +ServerState.getServerState().getServerName()+ " connected with server "+ response.getString("server"));

			//Set chat rooms
			JSONArray chatrooms = response.getJSONArray("chatrooms");
			String server = response.getString("server");
			ConcurrentHashMap<String, String> otherServersChatRooms = ServerState.getServerState().getOtherServersChatRooms();
			for (int x=0; x<chatrooms.length(); x++) {
				otherServersChatRooms.put(chatrooms.getString(x), server);
			}
			ServerState.getServerState().setOtherServersChatRooms(otherServersChatRooms);
			
			// set identities	
			JSONArray identities = response.getJSONArray("identity");
			ConcurrentHashMap<String, String> otherServersIdentities = ServerState.getServerState().getOtherServersUsers();
			for (int x=0; x<identities.length(); x++) {
				otherServersIdentities.put(identities.getString(x), server);
			}
			ServerState.getServerState().setOtherServersUsers(otherServersIdentities);
			
			
		    
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
		logger.debug("respond "+msg.toString()+ " " + socket.getPort());
		Sender.sendRespond(socket, msg);
		logger.debug("respond sent"+msg.toString());
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
	
	public void deleteChatroom(JSONObject response) {
		String chatroomToDelete = response.getString("roomid");
		
		ConcurrentHashMap<String, String> otherServers =  ServerState.getServerState().getOtherServersChatRooms();
		otherServers.remove(chatroomToDelete);
		ServerState.getServerState().setOtherServersUsers(otherServers);
	}
	
	

	
	
}
