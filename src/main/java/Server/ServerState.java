package Server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ClientHandler.ClientHandler;
import ClientHandler.User;

public class ServerState {
	
	/* Maintaining current running server state*/
	private static final Logger logger = LogManager.getLogger(ServerState.class);
	private String serverName, serverAddress;
	
	private int clientPort, serverPort;
	
	private Server currentServer;
	
	private static ServerState serverState;
	

	
	private ConcurrentHashMap<String, Server> serversHashmap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ChatRoom> chatRoomHashmap = new ConcurrentHashMap<>();
	private  ConcurrentLinkedQueue<User> identityList = new ConcurrentLinkedQueue<>();
	
	private ServerState() {}
	
	//create single ServerState object
	public static ServerState getServerState() {
        if (serverState == null) {
            synchronized (ServerState.class) {
                if (serverState == null) {
                    serverState = new ServerState();
                }
            }
        }
        return serverState;
    }
	
	
	//initialize server
	

	public ServerState initializeServer(String serverName, String confFilePath) {
		//TODO
		//Have to initialize server using configure file
		//hard coded start
		this.serverName = "s1";
		this.serverAddress = "localhost";
		this.clientPort = 4444;
		this.serverPort = 5555;
		
		serversHashmap.put("s2", new Server("s2", "localhost", 4445, 5556));
		serversHashmap.put("s3", new Server("s3", "localhost", 4446, 5557));
		//hard coded end
		
		//create a mainhall room
		String mainHall = "MainHall-"+this.serverName;
	    ChatRoom chatRoom = new ChatRoom(mainHall);
	    chatRoomHashmap.put("MainHall", chatRoom);
		
		return serverState;
	}
	
	public ConcurrentHashMap<String, ChatRoom> getChatRoomHashmap() {
		return chatRoomHashmap;
	}

	public void setChatRoomHashmap(ConcurrentHashMap<String, ChatRoom> chatRoomHashmap) {
		this.chatRoomHashmap = chatRoomHashmap;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public ConcurrentLinkedQueue<User> getIdentityList() {
		return identityList;
	}

	public void setIdentityList(ConcurrentLinkedQueue<User> identityList) {
		this.identityList = identityList;
	}
	
	

}
