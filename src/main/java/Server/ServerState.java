package Server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.json.JSONObject;

import ClientHandler.ClientHandler;
import ClientHandler.User;
import Connection.Server2ServerConnection;
import Messaging.Sender;

public class ServerState {
	
	/* Maintaining current running server state*/
	
	private static final Logger logger = LogManager.getLogger(ServerState.class);

	private String serverName, serverAddress;
	
	private int clientPort, serverPort;
	
//	private Server currentServer;
	private Server leaderServer;

	private AtomicBoolean ongoingConsensus = new AtomicBoolean(false);
	
	private ConcurrentLinkedQueue<String> chatRoomsRequestIDs = new ConcurrentLinkedQueue<String>();
	private ConcurrentLinkedQueue<String> identityRequestIDs = new ConcurrentLinkedQueue<String>();
	private ConcurrentLinkedQueue<String> gossipingIDs = new ConcurrentLinkedQueue<String>();
	private ConcurrentHashMap<String, Integer> heartbeatCountList = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> suspectList = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Integer> voteSet = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> previousHeartbeatHashmap = new ConcurrentHashMap<>();
	
	private static ServerState serverState;
	
	private ConcurrentHashMap<String, Server> serversHashmap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ChatRoom> chatRoomHashmap = new ConcurrentHashMap<>();
	private  ConcurrentLinkedQueue<User> identityList = new ConcurrentLinkedQueue<>();
	
	private static ConcurrentHashMap<String, String> otherServersChatRooms = new ConcurrentHashMap<String, String>();
//	otherServersChatRooms<ChatroomName, server_name>
	private static ConcurrentHashMap<String, String> otherServersUsers = new ConcurrentHashMap<String, String>();
//	otherServersUsers<user_identity, server_name>
	
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
	

	public ServerState initializeServer(String serverName, ArrayList<String> conf) {
		
		for (String serverConf: conf) {
			JSONObject server = new JSONObject(serverConf);
			serversHashmap.put(server.getString("server-name"), new Server(
																		server.getString("server-name"),
																		server.getString("address"),
																		server.getInt("server-port"),	
																		server.getInt("client-port")));
			if(server.getString("server-name").equals(serverName)) {
				this.serverName = serverName;
				this.serverAddress = server.getString("address");
				this.clientPort = server.getInt("client-port");
				this.serverPort = server.getInt("server-port");
			}
			
			if(server.getString("server-name").equals("s1")) {
				this.leaderServer = new Server(server.getString("server-name"), 
											server.getString("address"), 
											server.getInt("server-port"),
											server.getInt("client-port") );
			}
						
		}
	
		//create a mainhall room
		String mainHall = "MainHall-"+this.serverName;
		ChatRoom chatRoom = new ChatRoom();
	    chatRoom.createChatRoom(mainHall, "");
	    chatRoomHashmap.put("MainHall", chatRoom);
		
	    createServer2ServerConnection();
	    
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
	
	public Server getServerByName(String serverName) {
		return this.serversHashmap.get(serverName);
	}
	
	public void replaceServerbByName(Server server) {
		this.serversHashmap.put(server.getServerName(), server);
	}
	
	public ConcurrentHashMap<String, Server> getServersHashmap() {
		return this.serversHashmap;
	}

	public ConcurrentHashMap<String, String> getOtherServersChatRooms() {
		return otherServersChatRooms;
	}

	public void setOtherServersChatRooms(ConcurrentHashMap<String, String> otherServersChatRooms) {
		this.otherServersChatRooms = otherServersChatRooms;
	}

	public ConcurrentHashMap<String, String> getOtherServersUsers() {
		return otherServersUsers;
	}

	public void setOtherServersUsers(ConcurrentHashMap<String, String> otherServersUsers) {
		this.otherServersUsers = otherServersUsers;
	}

	public Server getLeaderServer() {
		return leaderServer;
	}

	public void setLeaderServer(Server leaderServer) {
		this.leaderServer = leaderServer;
	}
	
	public boolean checkChatRoomRequestCompleted(String id) {
		if (this.chatRoomsRequestIDs == null)
			return false;
		
		return this.chatRoomsRequestIDs.contains(id);
	}
	
	public void addChatRoomRequestID(String id) {
		this.chatRoomsRequestIDs.add(id);
	}
	
	public boolean checkIdentityRequestCompleted(String id) {
		if (this.identityRequestIDs == null)
			return false;
		return this.identityRequestIDs.contains(id);
	}
	
	public void addIdentityRequesID(String id) {
		this.identityRequestIDs.add(id);
	}
	
	public boolean isGossipingIDContains(String id) {
		if (this.gossipingIDs == null)
			return false;
		return this.gossipingIDs.contains(id);
	}
	
	public void addGossipingID(String id) {
		this.gossipingIDs.add(id);
	}
	
	public ConcurrentHashMap<String, Integer> getHeartbeatCountList() {
		return heartbeatCountList;
	}
	
	public synchronized void removeServer(String serverName) {
		serversHashmap.remove(serverName);
	}
	
    public synchronized void removeServerInCountList(String serverName) {
        heartbeatCountList.remove(serverName);
    }
    
    public synchronized void removeServerInSuspectList(String serverName) {
        suspectList.remove(serverName);
    }

    public ConcurrentHashMap<String, String> getSuspectList() {
        return suspectList;
    }
    
    public AtomicBoolean onGoingConsensus() {
        return ongoingConsensus;
    }
    
    public ConcurrentHashMap<String, Integer> getVoteSet() {
        return voteSet;
    }
    
    public ConcurrentHashMap<String, String> getPreviousHeartbeatHashmap() {
		return previousHeartbeatHashmap;
	}
    
    //check leader is available
    public boolean isLeaderElected() {
    	if(leaderServer != null) {
    		return true;
    	}
    	return false;
    }
    
    //remove suspect server data
    public static void removeSuspectServer(String suspectServerName) {
    	//remove server from serverHashMap
    	if(ServerState.getServerState().getServersHashmap().containsKey(suspectServerName)) {
    		ServerState.getServerState().removeServer(suspectServerName);
    	}
    	
    	//remove server from heartbeatCountList
    	if(ServerState.getServerState().getHeartbeatCountList().containsKey(suspectServerName)) {
    		ServerState.getServerState().removeServerInCountList(suspectServerName);
    	}
    	
        //remove server from suspectList
    	if(ServerState.getServerState().getSuspectList().containsKey(suspectServerName)) {
    		ServerState.getServerState().removeServerInSuspectList(suspectServerName);
    	}
    	
    	//remove chatrooms of suspect server
    	Iterator<ConcurrentHashMap.Entry<String, String>> chatroomIterator = ServerState.getServerState().otherServersChatRooms.entrySet().iterator();
		while (chatroomIterator.hasNext()) {
		    if (chatroomIterator.next().getValue().equals(suspectServerName))
		    	chatroomIterator.remove();
		}
		
		//remove clients of suspect server
		Iterator<ConcurrentHashMap.Entry<String, String>> userIterator = ServerState.getServerState().otherServersUsers.entrySet().iterator();
		while (userIterator.hasNext()) {
		    if (userIterator.next().getValue().equals(suspectServerName))
		    	userIterator.remove();
		}
    }
	
	public void createServer2ServerConnection() {
		for (ConcurrentHashMap.Entry<String,Server> entry : serversHashmap.entrySet()) {
			if (!(entry.getKey().equals(this.serverName))) {
				try {
					logger.debug("check entry server :: "+ entry.getValue().getServerName());
					Socket socket = new Socket(entry.getValue().getServerAddress(), entry.getValue().getServerPort());
//					logger.debug("socket :: "+ socket.toString());
					logger.info("Server "+ this.serverName + " is connected to Server "+entry.getValue().getServerName()
							+ " using address " +entry.getValue().getServerAddress() 
							+ " port " + entry.getValue().getServerPort());
					JSONObject obj = new JSONObject();
					obj.put("type","server-connection-request").put("server", this.serverName).put("server-address", this.serverAddress).put("server-port", this.serverPort).put("client-port", this.clientPort);
					Sender.sendRespond(socket, obj);
					Server s = entry.getValue();
					s.setServerSocketConnection(socket);
					replaceServerbByName(s);
					Server2ServerConnection s2sc = new Server2ServerConnection(socket);
					s2sc.start();
				}catch (UnknownHostException u)
		        {
		            logger.error(u.getMessage());
		        }
		        catch(IOException i)
		        {
		            logger.error(i.getMessage());
		        }
				
			}
		}
	}
	
	

}
