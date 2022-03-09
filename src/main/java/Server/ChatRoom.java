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
	
	public ChatRoom() {
		chatRoomHashMap = ServerState.getServerState().getChatRoomHashmap();
		this.userListInRoom = new ConcurrentLinkedQueue<User>();
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
		//TODO
		//Get the global chatroomhashmap and check the uniqueness
		if (chatRoomHashMap.containsKey(roomName)) {
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
	

}
