package Server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import ClientHandler.User;

public class ChatRoom {
	
	private String roomName; //unique name
	private User owner;
	private ConcurrentLinkedQueue<User> userListInRoom;
	
	public ChatRoom(String roomName) {
		this.roomName = roomName;
		this.userListInRoom = new ConcurrentLinkedQueue<User>();
	}
	
	public ChatRoom(String roomName, User user) {
		this.roomName = roomName;
		this.owner = owner;
	}

	public String getRoomName() {
		return roomName;
	}

}
