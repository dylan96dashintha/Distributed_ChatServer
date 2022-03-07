package ClientHandler;

import java.net.Socket;

public class NewIdentity {
	String name;
	Socket socket;
	public NewIdentity(String name, Socket socket) {
		this.name = name;
		this.socket = socket;
	}
	
	public String getName () {
		return name;
	}
	
	
	
	public boolean validation() {
		int size = name.length();
		if (size>3 && size <16 && isAlphaNumeric()) {
			UserList userList = new UserList();
			boolean isApproved = userList.addUser(name, socket);
			return isApproved;
		} else {
			return false;
		}
	}
	
	   public boolean isAlphaNumeric () {
	        return  name.matches("^[a-zA-Z0-9]*$");
	    }
	

}
