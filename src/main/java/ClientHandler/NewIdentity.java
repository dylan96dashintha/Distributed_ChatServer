package ClientHandler;

import java.net.Socket;

public class NewIdentity {
	String name;
	Socket socket;
	private UserList userList;
	public NewIdentity(String name, Socket socket) {
		userList = new UserList();
		this.name = name;
		this.socket = socket;
	}
	
	public String getName () {
		return name;
	}
	
	
	
	public boolean validation() {
		int size = name.length();
		if (size>3 && size <16 && isAlphaNumeric()) {
			setUserList(userList);
			boolean isApproved = userList.addUser(name, socket);
			return isApproved;
		} else {
			return false;
		}
	}
	
	   public UserList getUserList() {
		return userList;
	}

	public void setUserList(UserList userList) {
		this.userList = userList;
	}

	public boolean isAlphaNumeric () {
	        return  name.matches("^[a-zA-Z0-9]*$");
	    }
	
	public boolean removeUser(User user) {
		boolean isUserRemoved = userList.removeUser(user);
		return isUserRemoved;
	}

}
