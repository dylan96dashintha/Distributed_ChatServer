package ClientHandler;

import org.json.JSONObject;

import Gossiping.GossipingHandler;
import Messaging.LeaderChannel;
import Messaging.Sender;
import Server.ChatRoom;
import Server.ServerState;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Server.Server;
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
	public GossipingHandler gossipingHandle;
	public ConcurrentHashMap<String, String> otherServersChatRooms;

	public ClientHandler(Socket socket) {

		this.socket = socket;
		chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		serverId = ServerState.getServerState().getServerName();
		ChatRoom chatRoomMainHall = chatRoomHashMap.get("MainHall");
		mainHall = chatRoomMainHall.getRoomName();
		chatRoom = new ChatRoom();
		gossipingHandle = new GossipingHandler();



	}

	public void getTypeFunctionality(JSONObject jsnObj) {
//		SUDESH - ADDED
		otherServersChatRooms = LeaderChannel.getGlobalChatRooms();
//		SUDESH - REMOVED
//		otherServersChatRooms = ServerState.getServerState().getOtherServersChatRooms();
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		switch (type) {

		case "newidentity":
			identityName = jsnObj.getString("identity");
			newIdentity = new NewIdentity(identityName, socket);
			boolean isApproved = newIdentity.validation();
			JSONObject res;
			JSONObject roomChangeResNewIdentity = null;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity");
				logger.debug("new identity  ::  " + newIdentity.getName());
				roomChangeResNewIdentity = changeRoom(newIdentity.getName(), "", mainHall);
				chatRoom.addUsersToMainHall(newIdentity.getUserList().getUser());
				newIdentity.getUserList().getUser().setRoomName(mainHall);

			} else {
				res = new JSONObject().put("approved", "false").put("type", "newidentity");
			}

			logger.debug("New Identity: " + res);
			try {
				Sender.sendRespond(socket, res);
				if (isApproved) {
					// TODO- Messaging
					// Broadcast roomChangeResNewIdentity to all the users in MainHall including the
					// connecting clientS
					Sender.sendMessageChatroom(mainHall, roomChangeResNewIdentity);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "message":
			String content = jsnObj.getString("content");
			String identityMessage = newIdentity.getName();
			JSONObject messageRes = new JSONObject().put("content", content).put("identity", identityMessage)
					.put("type", "message");

			// TODO-done
			// broadcast the messageRes to all the users in the room
			String roomIdTypeMessage = newIdentity.getUserList().getUser().getRoomName();

			try {
				Sender.sendMessageChatroom(roomIdTypeMessage, messageRes);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			break;
		case "list":
			JSONObject resList;
			List roomList = new ArrayList<String>();
//			JSONObject req = new JSONObject().put("type", "gossip-chatrooms-request");
//			for (ConcurrentHashMap.Entry<String, Server> e: ServerState.getServerState().getServersHashmap().entrySet()) {
//				try {
//					Sender.sendRespond(e.getValue().getServerSocketConnection(), req);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
			
			// TODO - Done
			// Global chat rooms to be applied here
//			SUDESH - REMOVED
//			otherServersChatRooms = ServerState.getServerState().getOtherServersChatRooms();
//			SUDESH - ADDED
			otherServersChatRooms = LeaderChannel.getGlobalChatRooms();
			for (String roomNameList : otherServersChatRooms.keySet()) {
				logger.debug("List :: otherserverrooms :: "+roomNameList);
				roomList.add(roomNameList);
			}
//			SUDESH - REMOVED
//			for (ChatRoom chatRoom : chatRoomHashMap.values()) {
//				roomList.add(chatRoom.getRoomName());
//			}
			resList = new JSONObject().put("type", "roomlist").put("rooms", roomList);
			logger.debug("Room List: " + resList);
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
			ArrayList<String> userInRoom = new ArrayList();
			for (User user : userListTypeWho) {
				userInRoom.add(user.getName());
			}
			whoRes = new JSONObject().put("owner", newIdentity.getName()).put("identities", userInRoom)
					.put("roomid", roomIdTypeWho).put("type", "roomcontents");
			try {
				logger.debug("Case who :: " + whoRes);
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
				logger.debug("new identity  ::  " + identityName + " Room id :: " + roomId);
				boolean isRoomApproved = chatRoom.createChatRoom(roomId, newIdentity.getName());
				if (isRoomApproved) {
					logger.debug("Approved");

//					ConcurrentHashMap<String, String> seett =  LeaderChannel.getGlobalChatRooms();
//					ConcurrentHashMap<String, String> seett22 =  LeaderChannel.getGlobalIdentities();
//					for (ConcurrentHashMap.Entry<String, String> e : seett.entrySet()) {
//						logger.debug("Server " + e.getValue() + " room " + e.getKey());
//					}
//					
//					for (ConcurrentHashMap.Entry<String, String> e : seett22.entrySet()) {
//						logger.debug("Server " + e.getValue() + " user " + e.getKey());
//					}
					
					chatRoomHashMap.put(roomId, chatRoom);
					chatRoom.setChatRoomHashMap(chatRoomHashMap);
					ServerState.getServerState().setChatRoomHashmap(chatRoomHashMap);
					try {
						gossipingHandle.sendChatRoomCreateGossip();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					createRoomRes = new JSONObject().put("approved", "true").put("roomid", roomId).put("type",
							"createroom");
					try {
						Sender.sendRespond(socket, createRoomRes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO - Done
					// Broadcast roomchange message to teh clients that are members of the chat room
					String formerRoomName = newIdentity.getUserList().getUser().getRoomName();
					createRoomRoomChangeRes = changeRoom(newIdentity.getName(), formerRoomName, roomId);
					newIdentity.getUserList().getUser().setRoomName(roomId);

					try {
						logger.debug("Inside createroom try");
						Sender.sendMessageChatroom(formerRoomName, createRoomRoomChangeRes);
						chatRoom.removeUsersFromChatRoom(newIdentity.getUserList().getUser(), formerRoomName);
						chatRoom.addUsersToChatRoom(newIdentity.getUserList().getUser(), roomId);
						chatRoom.setChatRoomHashMap(chatRoomHashMap);
						ServerState.getServerState().setChatRoomHashmap(chatRoomHashMap);
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.debug("createroom Error has occured!");
					}

					newIdentity.getUserList().getUser().setRoomName(roomId);
				} else {
					logger.debug("create room failed");
					createRoomRes = new JSONObject().put("approved", "false").put("roomid", roomId).put("type",
							"createroom");
					try {
						Sender.sendRespond(socket, createRoomRes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

			break;
		case "joinroom":
			String roomIdJoinRoom = jsnObj.getString("roomid");
			String identityJoinRoom = newIdentity.getName();
			JSONObject joinRoomRes;
			JSONObject joinRoomUnsuccessRes;
			String formerRoomJoinRoom = newIdentity.getUserList().getUser().getRoomName();
			logger.debug("Other server checkup :: " + otherServersChatRooms.containsKey(roomIdJoinRoom));
			boolean isRoomChangeSuccess = true;
			if (chatRoomHashMap.containsKey(roomIdJoinRoom)) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdJoinRoom);
				String owner = chatRoomJoinRoom.getOwner();
				if (!owner.equals(identityJoinRoom)) {
					chatRoomJoinRoom.joinRoom(newIdentity.getUserList().getUser());
					// joinRoomRes = new JSONObject().put("type", "roomchange").put("identity",
					// identityJoinRoom).put("former", mainHall).put("roomid", roomIdJoinRoom);
					joinRoomRes = changeRoom(identityJoinRoom, formerRoomJoinRoom, roomIdJoinRoom);
					chatRoom.removeUsersFromChatRoom(newIdentity.getUserList().getUser(), newIdentity.getUserList().getUser().getRoomName());
					newIdentity.getUserList().getUser().setRoomName(roomIdJoinRoom);
					logger.debug("JoinRoom :: " + joinRoomRes);
					// TODO - Done
					// send the joinRoomRes to members of the former chat room, members of the new
					// chat room, and to the client

					String username = newIdentity.getUserList().getUser().getName();

					try {

						// send new join message to former chat room
						logger.debug("formerRoomJoinRoom :: " + formerRoomJoinRoom);
						Sender.sendNotificationChatroom(formerRoomJoinRoom, joinRoomRes, username);

						// send new join message to new chat room
						logger.debug("roomIdJoinRoom :: " + roomIdJoinRoom);
						Sender.sendNotificationChatroom(roomIdJoinRoom, joinRoomRes, username);

						Sender.sendRespond(socket, joinRoomRes);
						//chatRoom.removeUsersFromChatRoom(newIdentity.getUserList().getUser(), formerRoomJoinRoom);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					// joinRoomRes = new JSONObject().put("type", "roomchange").put("identity",
					// identityJoinRoom).put("former", roomIdJoinRoom).put("roomid",
					// roomIdJoinRoom);
					isRoomChangeSuccess = false;

				}
			} else if (otherServersChatRooms.containsKey(roomIdJoinRoom)) {
				// TODO - Done
				// Check the global rooms of the user where he exist and reply
				String serverId = otherServersChatRooms.get(roomIdJoinRoom);
				Server otherServer = ServerState.getServerState().getServerByName(serverId);
				JSONObject otherServerChatRoomJoin = new JSONObject().put("host", otherServer.getServerAddress())
						.put("port", String.valueOf(otherServer.getClientPort())).put("roomid", roomIdJoinRoom)
						.put("type", "route");
				JSONObject roomChangeResOtherServer = changeRoom(identityJoinRoom, formerRoomJoinRoom, roomIdJoinRoom);
				try {
					chatRoom.removeUsersFromChatRoom(newIdentity.getUserList().getUser(), formerRoomJoinRoom);
					newIdentity.removeUser(newIdentity.getUserList().getUser());
					Sender.sendRespond(socket, otherServerChatRoomJoin);
					Sender.sendMessageChatroom(formerRoomJoinRoom, roomChangeResOtherServer);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// joinRoomRes = new JSONObject().put("type", "roomchange").put("identity",
				// identityJoinRoom).put("former", roomIdJoinRoom).put("roomid",
				// roomIdJoinRoom);
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
			String identityMoveJoin = jsnObj.getString("identity");
			// User userMoveJoin = new User(identityMoveJoin, socket);
			newIdentity = new NewIdentity(identityMoveJoin, socket);
			boolean isApprovedMoveJoin = newIdentity.validation();
			JSONObject serverChangeRes;
			JSONObject moveJoinRes;
			if (chatRoomHashMap.containsKey(roomIdMoveJoin)) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdMoveJoin);
				newIdentity.getUserList().getUser().setRoomName(roomIdMoveJoin);
				chatRoomJoinRoom.joinRoom(newIdentity.getUserList().getUser());
				moveJoinRes = changeRoom(identityMoveJoin, formerRoomId, roomIdMoveJoin);
				// TODO -Done
				
				//chatRoom.addUsersToChatRoom(newIdentity.getUserList().getUser(), roomIdMoveJoin);
				
				moveJoinSendMsg(roomIdMoveJoin);
				try {
					Sender.sendMessageChatroom(roomIdMoveJoin, moveJoinRes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				moveJoinRes = changeRoom(identityMoveJoin, formerRoomId, mainHall);
				// TODO
				// broadcast the message to the all the users in mainHall				
				newIdentity.getUserList().getUser().setRoomName(mainHall);
				chatRoom.addUsersToMainHall(newIdentity.getUserList().getUser());
				moveJoinSendMsg(mainHall);
				
				try {
					Sender.sendMessageChatroom(mainHall, moveJoinRes);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			break;
		case "deleteroom":
			String roomIdDelRoom = jsnObj.getString("roomid");
			String identityDelRoom = newIdentity.getName();
			boolean isUserDelRoom = chatRoom.isUserOwnRoomReturnBool(identityDelRoom);
			if (isUserDelRoom) {
				ChatRoom chatRoomJoinRoom = chatRoomHashMap.get(roomIdDelRoom);
				String owner = chatRoomJoinRoom.getOwner();
				deleteRoom(owner, identityDelRoom, roomIdDelRoom, false);
			} else {
				JSONObject delRoomUnsucess = new JSONObject().put("approved", "false").put("roomid", roomIdDelRoom)
						.put("type", "deleteroom");
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
			logger.debug("case quit");
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
				// TODO - Done
				// Server closes the connection
				deleteRoom(identityQuit, identityQuit, chatRoomQuit.getRoomName(), false);
			}
			
			try {
				Sender.sendRespond(socket, roomChangeQuit);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case "quitctrl":
			logger.debug("case quit-ctrl");
			String identityQuitCtrl = newIdentity.getName();
			JSONObject roomChangeQuitCtrl;
			boolean isUserCtrl = chatRoom.isUserOwnRoomReturnBool(identityQuitCtrl);
			ChatRoom chatRoomQuitCtrl = chatRoom.isUserOwnRoom(identityQuitCtrl);
			chatRoom.removeUsersFromChatRoom(newIdentity.getUserList().getUser(), newIdentity.getUserList().getUser().getRoomName());
			if (!isUserCtrl) {
				boolean isUserQuitCtrl = newIdentity.removeUser(newIdentity.getUserList().getUser());
				//roomChangeQuitCtrl = changeRoom(identityQuitCtrl, newIdentity.getUserList().getUser().getRoomName(), "");
			} else {
				//roomChangeQuitCtrl = changeRoom(identityQuitCtrl, chatRoomQuitCtrl.getRoomName(), "");

				boolean isUserQuitCtrl = newIdentity.removeUser(newIdentity.getUserList().getUser());
				// TODO - Done
				// Server closes the connection
				deleteRoom(identityQuitCtrl, identityQuitCtrl, chatRoomQuitCtrl.getRoomName(), true);
			}
			
			break;
		}
		
	}

	public JSONObject changeRoom(String identity, String formerRoom, String newRoom) {
		JSONObject roomChangeRes;
		roomChangeRes = new JSONObject().put("roomid", newRoom).put("former", formerRoom).put("identity", identity)
				.put("type", "roomchange");
		return roomChangeRes;
	}

	public void deleteRoom(String owner, String identityDelRoom, String roomIdDelRoom, boolean isCtrl) {
		JSONObject delRoomRes;
		JSONObject delRoomUnsucessRes;
		JSONObject delRoomClientRes;
		JSONObject RoomChangeDelRoomRes;
		ArrayList<JSONObject> resList = new ArrayList<>();
		boolean isDelSuccess = true;
		if (owner.equals(identityDelRoom)) {

			ConcurrentLinkedQueue<User> userList = chatRoom.deleteRoom(roomIdDelRoom);
			Object[] UserListMainHall = chatRoom.getUserListInRoom(mainHall).toArray();
			
			delRoomRes = new JSONObject().put("roomid", roomIdDelRoom).put("serverid", serverId).put("type", "deleteroom");
			// TODO
			// send delRoomRes to other servers
			//Notify other servers
			for (ConcurrentHashMap.Entry<String, Server> e : ServerState.getServerState().getServersHashmap().entrySet()) {
//				JSONObject obj = new JSONObject()
//						.put("type" , "deleteroom")
//						.put("serverid", ServerState.getServerState().getServerName())
//						.put("roomid", roomIdDelRoom);
				try {
					if (!(e.getValue().getServerName().equals(ServerState.getServerState().getServerName())))
						Sender.sendRespond(e.getValue().getServerSocketConnection(), delRoomRes);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			for (User user : userList) {
				RoomChangeDelRoomRes = changeRoom(user.getName(), roomIdDelRoom, mainHall);
				resList.add(RoomChangeDelRoomRes);
				user.setRoomName(mainHall);
				chatRoom.addUsersToMainHall(user);

			}

			// TODO - Done
			// RoomChangeDelRoomRes message to all members of the deleted room showing each
			// member id moving

			// RoomChangeDelRoomRes message to client of the deleted room
			try {
				
				for (JSONObject jsn : resList) {
					
					Sender.sendMessageToUserList(userList, jsn);
					Sender.sendMessageToUserList(UserListMainHall, jsn);
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			delRoomClientRes = new JSONObject().put("approved", "true").put("roomid", roomIdDelRoom).put("type", "deleteroom");
			try {
				if (!isCtrl) {
					Sender.sendRespond(socket, delRoomClientRes);
				}
				
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
				if (!isCtrl) {
					Sender.sendRespond(socket, delRoomUnsucessRes);	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block.
				e.printStackTrace();
			}
		}

	}

	public void moveJoinSendMsg(String chatRoom) {
		JSONObject serverChangeRes = new JSONObject().put("serverid", serverId).put("approved", "true").put("type", "serverchange");
		try {
			logger.debug("movejoin ::15 :: socket ::" + socket);
			Sender.sendRespond(socket, serverChangeRes);
			;

		} catch (IOException e) {
			logger.debug("movejoin ::16");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
