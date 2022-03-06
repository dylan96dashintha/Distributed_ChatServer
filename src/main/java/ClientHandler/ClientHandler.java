package ClientHandler;
import org.json.JSONObject;

import Server.ChatRoom;
import Server.ServerState;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

//Handling types 


public class ClientHandler {
	private static final Logger logger = LogManager.getLogger(ClientHandler.class);
	String type;
	JSONObject jsnObj;
	protected ConcurrentHashMap<String, ChatRoom> chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
	protected String mainHall = (chatRoomHashMap.get("MainHall")).getRoomName();
	public ClientHandler(JSONObject jsnObj) {
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		
	}
	
	public void getTypeFunctionality() {
		switch (type) {
		case "newidentity":
			NewIdentity newIdentity = new NewIdentity(jsnObj.getString("identity"));
			boolean isApproved = newIdentity.validation();
			String res;
			String roomChangeRes;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity").toString();
				
				//Broadcast res to MainHall
				roomChangeRes = new JSONObject().put("roomid" , mainHall).put("former" , "").put("identity", newIdentity).put("type", "roomchange").toString();
			} else {
				res = new JSONObject().put("approved", "false").put("type", "newidentity").toString();
			}
			//TODO-List
			//Message this response 
			logger.debug("New Identity: "+ res);
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
