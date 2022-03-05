package ClientHandler;

import java.util.ArrayList;

public class UserList {
	ArrayList<User> userArrayList = new ArrayList();
	
	public boolean addUser(String name) {
		if (isUnique(name)) {
			//TODO
			//check with other servers
			
			User user = new User(name);
			
			//TODO
			//Add user to other servers users list
			userArrayList.add(user);
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean isUnique(String name) {
		boolean isUni = true;
		for (User u : userArrayList) {
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
