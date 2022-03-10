package ClientHandler;
import org.json.JSONObject;

import Messaging.LeaderChannel;
import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	protected NewIdentity newIdentity;
	String identityName;
	protected ChatRoom chatRoom;
	protected String serverId;
	public ClientHandler(Socket socket) {
		
		this.socket = socket;
		chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		serverId = ServerState.getServerState().getServerName();
		ChatRoom chatRoomMainHall = chatRoomHashMap.get("MainHall"); 
		mainHall = chatRoomMainHall.getRoomName();
		chatRoom = new ChatRoom();
		
	}
	
	public void getTypeFunctionality(JSONObject jsnObj) {
		logger.info("AAAAAAAAAAAAAAAAAAAAAAA" + jsnObj.toString());
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		switch (type) {
		
		case "newidentity":
			identityName = jsnObj.getString("identity");
			newIdentity = new NewIdentity(identityName, socket);
			boolean isApproved = newIdentity.validation();
			JSONObject res;
			JSONObject roomChangeResNewIdentity;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity");
				logger.debug("new identity22  ::  "+newIdentity.getName());
				roomChangeResNewIdentity = changeRoom(newIdentity.getName(), "", mainHall);
				newIdentity.getUserList().getUser().setRoomName(mainHall);
				
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
			String content = jsnObj.getString("content");
			String identityMessage = newIdentity.getName();
			JSONObject messageRes = new JSONObject().put("content", content).put("identity", identityMessage).put("type", "message");
			//TODO
			//broadcast the messageRes to all the users in the room
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
			JSONObject whoRes;
			String roomIdTypeWho = newIdentity.getUserList().getUser().getRoomName();
			ConcurrentLinkedQueue<User> userListTypeWho = chatRoom.getUserListInRoom(roomIdTypeWho);
			whoRes = new JSONObject().put("owner", newIdentity.getName()).put("identities", userListTypeWho).put("roomid", roomIdTypeWho).put("type", "roomcontents");
			try {
				logger.debug("Case who :: "+whoRes);
				Sender.sendRespond(socket, whoRes);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case "createroom":
			String roomId = jsnObj.getString("roomid");
			JSONObject createRoomRes;
			JSONObject createRoomRoomChangeRes;
			if (!roomId.equals("MainHall")) {
				logger.info("new identity  ::  "+identityName);
				boolean isRoomApproved = chatRoom.createChatRoom(roomId, newIdentity.getName());
				if (isRoomApproved) {
					logger.info("Approved");
					
					for (ConcurrentHashMap.Entry<String, String> e : LeaderChannel.getGlobalChatRooms().entrySet()) {
						logger.info("Server " + e.getKey() + " room " + e.getValue());
					}
					
					chatRoomHashMap.put(roomId, chatRoom);
					createRoomRes = new JSONObject().put("approved", "true").put("roomid", roomId).put("type", "createroom");
					try {
						logger.info("createroom :: createRoomRes :: "+ createRoomRes);
						Sender.sendRespond(socket, createRoomRes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//TODO
					//Broadcast roomchange message to teh clients that are members of the chat room
					//Check former_room name
					
					createRoomRoomChangeRes = changeRoom(newIdentity.getName(), "former_room", roomId);
					newIdentity.getUserList().getUser().setRoomName(roomId);
				} else {
					createRoomRes = new JSONObject().put("approved", "false").put("roomid", roomId).put("type", "createrroom");
				}
				
			}
			
			break;
		case "joinroom":
			String roomIdJoinRoom = jsnObj.getString("roomid");
			String identityJoinRoom = newIdentity.getName();
			JSONObject joinRoomRes;
			JSONObject joinRoomUnsuccessRes;
			boolean isRoomChangeSuccess = true;
			if (chatRoomHashMap.containsKey(roomIdJoinRoom)) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdJoinRoom);
				String owner = chatRoomJoinRoom.getOwner();
				if (!owner.equals(identityJoinRoom)) {
					chatRoomJoinRoom.joinRoom(newIdentity.getUserList().getUser());
					//joinRoomRes = new JSONObject().put("type", "roomchange").put("identity", identityJoinRoom).put("former", mainHall).put("roomid", roomIdJoinRoom);
					joinRoomRes = changeRoom(identityJoinRoom, mainHall, roomIdJoinRoom);
					newIdentity.getUserList().getUser().setRoomName(roomIdJoinRoom);
					logger.debug("JoinRoom :: "+joinRoomRes);
					//TODO
					//send the joinRoomRes to members of the former chat room, members  of the new chat room, and to the client
					try {
						Sender.sendRespond(socket, joinRoomRes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else {
					//joinRoomRes = new JSONObject().put("type", "roomchange").put("identity", identityJoinRoom).put("former", roomIdJoinRoom).put("roomid", roomIdJoinRoom);
					isRoomChangeSuccess = false;
					
					
				}
			}
//			else if (false) {
//				//TODO
//				//Check the global rooms of the user where he exist	and reply
//			}
			else {
				//joinRoomRes = new JSONObject().put("type", "roomchange").put("identity", identityJoinRoom).put("former", roomIdJoinRoom).put("roomid", roomIdJoinRoom);
				isRoomChangeSuccess = false;
				
			}
			
			if (!isRoomChangeSuccess) {
				joinRoomUnsuccessRes = changeRoom(identityJoinRoom, roomIdJoinRoom, roomIdJoinRoom);
				try {
					Sender.sendRespond(socket, joinRoomUnsuccessRes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			break;
		case "movejoin":
			String roomIdMoveJoin = jsnObj.getString("roomid");
			String formerRoomId = jsnObj.getString("former");
			String identityMoveJoin = newIdentity.getName();
			JSONObject serverChangeRes;
			JSONObject moveJoinRes;
			if (chatRoomHashMap.containsKey(roomIdMoveJoin)) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdMoveJoin);
				chatRoomJoinRoom.joinRoom(newIdentity.getUserList().getUser());
				moveJoinRes = changeRoom(identityMoveJoin, formerRoomId, roomIdMoveJoin);
				//TODO
				//broadcast the message to the all the users in new room
				newIdentity.getUserList().getUser().setRoomName(roomIdMoveJoin);
			} else {
				moveJoinRes = changeRoom(identityMoveJoin, formerRoomId, mainHall);
				//TODO
				//broadcast the message to the all the users in mainHall
				newIdentity.getUserList().getUser().setRoomName(mainHall);
			}
			
			serverChangeRes = new JSONObject().put("serverid", serverId).put("approved", "true").put("type", "serverchange");
			try {
				Sender.sendRespond(socket, serverChangeRes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "deleteroom":
			String roomIdDelRoom = jsnObj.getString("roomid");
			String identityDelRoom = newIdentity.getName();
			boolean isUserDelRoom = chatRoom.isUserOwnRoomReturnBool(identityDelRoom);
			if (isUserDelRoom) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdDelRoom);
				String owner = chatRoomJoinRoom.getOwner();
				deleteRoom(owner, identityDelRoom, roomIdDelRoom);	
			} else {
				JSONObject delRoomUnsucess = new JSONObject().put("approved", "false").put("roomid", roomIdDelRoom).put("type", "deleteroom");
				try {
					Sender.sendRespond(socket, delRoomUnsucess);
				} catch (IOException e) {
					// TODO Auto-generated catch block.
					e.printStackTrace();
				}
			}
			
//			
//			JSONObject delRoomRes;
//			JSONObject delRoomUnsucessRes;
//			JSONObject delRoomClientRes;
//			JSONObject RoomChangeDelRoomRes;
//			boolean isDelSuccess = true;
//			if (owner.equals(identityDelRoom)) {
//				ConcurrentLinkedQueue<User> userListDeleteRoom = chatRoom.deleteRoom(roomIdDelRoom);
//				delRoomRes = new JSONObject().put("roomid", roomIdDelRoom).put("serverid", serverId).put("type", "deleteroom");
//				//TODO
//				//send delRoomRes to other servers
//				RoomChangeDelRoomRes = changeRoom(identityDelRoom, roomIdDelRoom, mainHall);
//				newIdentity.getUserList().getUser().setRoomName(mainHall);
//				//TODO
//				//RoomChangeDelRoomRes message to all members of the deleted room showing each member id moving
//				//RoomChangeDelRoomRes message to client of the deleted room
//				delRoomClientRes = new JSONObject().put("approved", "true").put("roomid", roomIdDelRoom).put("type", "deleteroom");
//					try {
//						Sender.sendRespond(socket, delRoomClientRes);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				
//			} else {
//				isDelSuccess = false;
//			}
//			
//			if (!isDelSuccess) {
//				delRoomUnsucessRes = new JSONObject().put("approved", "false").put("roomid", roomIdDelRoom).put("type", "deleteroom");
//				try {
//					Sender.sendRespond(socket, delRoomUnsucessRes);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block.
//					e.printStackTrace();
//				}
//			}
			
			break;
		case "quit":
			String identityQuit = newIdentity.getName();
			JSONObject roomChangeQuit;
			boolean isUser = chatRoom.isUserOwnRoomReturnBool(identityQuit);
			ChatRoom chatRoomQuit = chatRoom.isUserOwnRoom(identityQuit);
			if (!isUser) {
				boolean isUserQuit = newIdentity.removeUser(newIdentity.getUserList().getUser());
				roomChangeQuit = changeRoom(identityQuit, newIdentity.getUserList().getUser().getRoomName(), "");
			} else {
				roomChangeQuit = changeRoom(identityQuit, chatRoomQuit.getRoomName(), "");
				
				boolean isUserQuit = newIdentity.removeUser(newIdentity.getUserList().getUser());
				//TODO
				//Server closes the connection
				deleteRoom(identityQuit, identityQuit, chatRoomQuit.getRoomName());
			}
			try {
				Sender.sendRespond(socket, roomChangeQuit);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		}
	}
	
	
	public JSONObject changeRoom(String identity, String formerRoom, String newRoom) {
		JSONObject roomChangeRes;
		roomChangeRes = new JSONObject().put("roomid" , newRoom).put("former" , formerRoom).put("identity", identity).put("type", "roomchange");
		return roomChangeRes;
	}
	
	public void deleteRoom(String owner, String identityDelRoom, String roomIdDelRoom) {
		JSONObject delRoomRes;
		JSONObject delRoomUnsucessRes;
		JSONObject delRoomClientRes;
		JSONObject RoomChangeDelRoomRes;
		boolean isDelSuccess = true;
		if (owner.equals(identityDelRoom)) {
			ConcurrentLinkedQueue<User> userListDeleteRoom = chatRoom.deleteRoom(roomIdDelRoom);
			delRoomRes = new JSONObject().put("roomid", roomIdDelRoom).put("serverid", serverId).put("type", "deleteroom");
			//TODO
			//send delRoomRes to other servers
			RoomChangeDelRoomRes = changeRoom(identityDelRoom, roomIdDelRoom, mainHall);
			newIdentity.getUserList().getUser().setRoomName(mainHall);
			//TODO
			//RoomChangeDelRoomRes message to all members of the deleted room showing each member id moving
			//RoomChangeDelRoomRes message to client of the deleted room
			delRoomClientRes = new JSONObject().put("approved", "true").put("roomid", roomIdDelRoom).put("type", "deleteroom");
				try {
					Sender.sendRespond(socket, delRoomClientRes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		} else {
			isDelSuccess = false;
		}
		
		if (!isDelSuccess) {
			delRoomUnsucessRes = new JSONObject().put("approved", "false").put("roomid", roomIdDelRoom).put("type", "deleteroom");
			try {
				Sender.sendRespond(socket, delRoomUnsucessRes);
			} catch (IOException e) {
				// TODO Auto-generated catch block.
				e.printStackTrace();
			}
		}
		
	}
}
