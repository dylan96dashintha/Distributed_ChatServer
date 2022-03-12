package Server;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ClientHandler.ClientHandler;
import ClientHandler.User;
import ClientHandler.UserList;

public class ChatRoom {
	private static final Logger logger = LogManager.getLogger(ChatRoom.class);
	private String roomName; //unique name
	private String owner;
	private ConcurrentLinkedQueue<User> userListInRoom;
	private ConcurrentHashMap<String, ChatRoom> chatRoomHashMap;
	public ConcurrentHashMap<String, String> otherServersChatRooms;
	public ConcurrentLinkedQueue<User> getUserListInRoom() {
		return userListInRoom;
	}
	
	
	
	public ConcurrentHashMap<String, ChatRoom> getChatRoomHashMap() {
		return chatRoomHashMap;
	}


	public ConcurrentLinkedQueue<User> getUserListInRoom(String roomId) {
		ChatRoom chatRoom;
		if (roomId.startsWith("MainHall")) {
			chatRoom = chatRoomHashMap.get("MainHall");
		} else {
			chatRoom = chatRoomHashMap.get(roomId);	
		}
		return chatRoom.getUserListInRoom();
	}

	public ChatRoom() {
		chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		this.userListInRoom = new ConcurrentLinkedQueue<User>();
	}
	
	public void setChatRoomHashMap(ConcurrentHashMap<String, ChatRoom> chatRoomHashMap) {
		this.chatRoomHashMap = chatRoomHashMap;
	}

	public boolean createChatRoom(String roomName, String owner) {
		this.roomName = roomName;
		this.owner = owner;
		if (roomValidation() && isOwnerVirgin()) {
			return true;
		} else {
			return false;
		}
	}

	public String getRoomName() {
		return roomName;
	}
	
	public boolean roomValidation() {
		logger.debug("roomName :: "+ this.roomName);
		int size = roomName.length();
		if (size>3 && size <16 && isAlphaNumeric() && isUnique()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isAlphaNumeric () {
	    return  roomName.matches("^[a-zA-Z0-9]*$");
	}
	
	public boolean isUnique() {
		//TODO - Done
		//Get the global chatroomhashmap and check the uniqueness
		otherServersChatRooms = ServerState.getServerState().getOtherServersChatRooms();
		if (chatRoomHashMap.containsKey(roomName) || otherServersChatRooms.containsKey(roomName)) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isOwnerVirgin() {
		boolean isOwner = true; 
		for (ChatRoom chatRoom: chatRoomHashMap.values()) {
			if ((chatRoom.getOwner()).equals(owner)) {
				isOwner = false;
			}
		}
		return isOwner;
	}

	public String getOwner() {
		return owner;
	}
	
	public void joinRoom(User user) {
		userListInRoom.add(user);
	}
	
	public ConcurrentLinkedQueue<User> deleteRoom(String chatRoom) {
		ChatRoom chatRoomBeforeDelete = chatRoomHashMap.get(chatRoom);
		ConcurrentLinkedQueue<User> userListDeletedRoom = chatRoomBeforeDelete.getUserListInRoom();
		chatRoomHashMap.remove(chatRoom);
		return userListDeletedRoom;
		
	}
	
	public ChatRoom isUserOwnRoom(String owner) {
		ChatRoom chatRoomQuit = null;
		for (ChatRoom chatRoom: chatRoomHashMap.values()) {
			if ((chatRoom.getOwner()).equals(owner)) {
				chatRoomQuit = chatRoom;
			}
		}
		return chatRoomQuit;
	}
	
	public boolean isUserOwnRoomReturnBool(String owner) {
		boolean isUserOwn = false;
		for (ChatRoom chatRoom: chatRoomHashMap.values()) {
			if ((chatRoom.getOwner()).equals(owner)) {
				isUserOwn = true;
			}
		}
		return isUserOwn;
	}
	
	public void addUsersToMainHall(User user) {
		ChatRoom chatRoom = chatRoomHashMap.get("MainHall");
		chatRoom.joinRoom(user);
	}
}
