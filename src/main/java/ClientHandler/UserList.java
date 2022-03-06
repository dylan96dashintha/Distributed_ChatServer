package ClientHandler;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.ServerState;

public class UserList {
	//public ArrayList<User> userArrayList = new ArrayList();
	public ConcurrentLinkedQueue<User> identityList = ServerState.getServerState().getIdentityList();
	
	public boolean addUser(String name) {
		if (isUnique(name)) {
			//TODO
			//check with other servers
			
			User user = new User(name);
			
			//TODO
			//Add user to other servers users list
			identityList.add(user);
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean isUnique(String name) {
		boolean isUni = true;
		for (User u : identityList) {
			if (u.getName().equals(name)) {
				isUni = false;
			}
		}
		return isUni;
	}
	
	//TODO 
	//Check with other servers to check the user is unique
	public boolean isUniqueOtherServer() {
		return true;
	}
}
