package ClientHandler;

import java.net.Socket;

public class User {
	String name;
	Socket socket;
	private String roomName;
	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public User(String name, Socket socket) {
		
		this.name = name;
		this.socket = socket;
	}
	
	public String getName() {
		return name;
	}
	
	public Socket getUserSocket() {
		return socket;
	}
}
