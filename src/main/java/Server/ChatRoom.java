package Server;

import java.util.ArrayList;
import ClientHandler.User;

public class ChatRoom {
	
	private String roomName; //unique name
	private User owner;
	private ArrayList<User> userListinRoom;
	
	public ChatRoom(String roomName) {
		this.roomName = roomName;
		this.userListinRoom = new ArrayList<User>();
	}
	
	public ChatRoom(String roomName, User user) {
		this.roomName = roomName;
		this.owner = owner;
	}
	
	

}
