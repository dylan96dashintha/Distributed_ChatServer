package ClientHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import Gossiping.GossipingHandler;
import Messaging.LeaderChannel;
import Server.ServerState;

public class UserList {
	//public ArrayList<User> userArrayList = new ArrayList();
	public ConcurrentLinkedQueue<User> identityList = ServerState.getServerState().getIdentityList();
	public ConcurrentHashMap<String, String> otherServerUsers;
	public GossipingHandler gossipingHandle = new GossipingHandler();
	private User user;
	public LeaderChannel leaderchannel;
	public boolean addUser(String name, Socket socket) {
		if (isUnique(name) && isUniqueOtherServer(name)) {
			//TODO - Done
			//check with other servers
			//Done
			user = new User(name, socket);
			setUser(user);
			
			//TODO - Done
			//Add user to other servers users list
			identityList.add(user);
			ServerState.getServerState().setIdentityList(identityList);
			
			try {
				gossipingHandle.sendNewIdentityGossip();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		} else {
			return false;
		}
		
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
	
	//TODO - Done
	//Check with other servers to check the user is unique
	public boolean isUniqueOtherServer(String name) {
		leaderchannel = new LeaderChannel();
		otherServerUsers =  leaderchannel.getGlobalIdentities();
		
		boolean isUniOtherserver = true;
		if (otherServerUsers.containsKey(name)) {
			isUniOtherserver = false;
		} 
		return isUniOtherserver;
	}
	
	public boolean removeUser(User user) {
		if (identityList.contains(user)) {
			identityList.remove(user);
			ServerState.getServerState().setIdentityList(identityList);
			try {
				gossipingHandle.sendNewIdentityGossip();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}
