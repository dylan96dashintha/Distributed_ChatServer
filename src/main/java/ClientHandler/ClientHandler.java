package ClientHandler;
import org.json.JSONObject;

import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

//Handling types 


public class ClientHandler {
	private static final Logger logger = LogManager.getLogger(ClientHandler.class);
	String type;
	JSONObject jsnObj;
	Socket socket;
	protected String mainHall;
	public ClientHandler(JSONObject jsnObj, Socket socket) {
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		this.socket = socket;
		ConcurrentHashMap<String, ChatRoom> chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		ChatRoom chatRoom = chatRoomHashMap.get("MainHall"); 
		mainHall = chatRoom.getRoomName();
		
	}
	
	public void getTypeFunctionality() {
		switch (type) {
		case "newidentity":
			NewIdentity newIdentity = new NewIdentity(jsnObj.getString("identity"));
			boolean isApproved = newIdentity.validation();
			JSONObject res;
			JSONObject roomChangeRes;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity");
				
				//Broadcast res to MainHall
				roomChangeRes = new JSONObject().put("roomid" , mainHall).put("former" , "").put("identity", newIdentity).put("type", "roomchange");
				
			} else {
				res = new JSONObject().put("approved", "false").put("type", "newidentity");
			}
			//TODO-List
			//Message this response 
			logger.debug("New Identity: "+ res);
			try {
				Sender.sendRespond(socket, res);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "message":
			System.out.println("message");
			break;
		case "list":
			System.out.println("list");
			break;
		case "who":
			System.out.println("who");
			break;
		case "createroom":
			System.out.println("createroom");
			break;
		case "joinroom":
			System.out.println("joinroom");
			break;
		case "deleteroom":
			System.out.println("deleteroom");
			break;
		case "quit":
			System.out.println("quit");
			break;
		}
	} 
}
