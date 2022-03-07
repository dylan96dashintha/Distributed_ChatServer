package ClientHandler;
import org.json.JSONObject;

import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
	protected ConcurrentHashMap<String, ChatRoom> chatRoomHashMap;
	public ClientHandler(JSONObject jsnObj, Socket socket) {
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		this.socket = socket;
		chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		ChatRoom chatRoom = chatRoomHashMap.get("MainHall"); 
		mainHall = chatRoom.getRoomName();
		
	}
	
	public void getTypeFunctionality() {
		switch (type) {
		case "newidentity":
			NewIdentity newIdentity = new NewIdentity(jsnObj.getString("identity"));
			boolean isApproved = newIdentity.validation();
			JSONObject res;
			JSONObject roomChangeResNewIdentity;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity");
				roomChangeResNewIdentity = changeRoom(newIdentity.getName(), "", mainHall);
				
			} else {
				res = new JSONObject().put("approved", "false").put("type", "newidentity");
			}
			
			logger.debug("New Identity: "+ res);
			try {
				Sender.sendRespond(socket, res);
				if (isApproved) {
					//TODO- Messaging
					//Broadcast roomChangeResNewIdentity to all the users in MainHall including the connecting clientS
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "message":
			System.out.println("message");
			break;
		case "list":
			JSONObject resList;
			List roomList = new ArrayList<String>();
			//TODO
			//Global chat rooms to be applied here
			for (ChatRoom chatRoom: chatRoomHashMap.values()) {
				roomList.add(chatRoom.getRoomName());
			}
			resList = new JSONObject().put("type", "roomlist").put("rooms", roomList);
			logger.debug("Room List: "+ resList);
			try {
				Sender.sendRespond(socket, resList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case "who":
			System.out.println("who");
			break;
		case "createroom":
			
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
	
	
	public JSONObject changeRoom(String identity, String formerRoom, String newRoom) {
		JSONObject roomChangeRes;
		roomChangeRes = new JSONObject().put("roomid" , newRoom).put("former" , formerRoom).put("identity", identity).put("type", "roomchange");
		return roomChangeRes;
	}
}
