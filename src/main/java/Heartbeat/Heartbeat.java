package Heartbeat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import Gossiping.Gossiping;
import Messaging.Sender;
import Messaging.ServerMessage;
import Server.LeaderElector;
import Server.Server;
import Server.ServerState;
import model.Constants;

public class Heartbeat implements Runnable{
	
	private static final Logger logger = LogManager.getLogger(Heartbeat.class);
	
	private String option;
	private ServerMessage serverMessage;
	private Gossiping gossiping;
	
	public Heartbeat(String option){
		this.option = option.toLowerCase();
		this.serverMessage = ServerMessage.getInstance();
		this.gossiping = new Gossiping();
	}
	
	@Override
	public void run() {
		switch(option) {
		case "Heartbeat":
			while(true) {
				if(ServerState.getServerState().getServerName() != ServerState.getServerState().getLeaderServer().getServerName()) {
					try {
						Thread.sleep(Constants.REQUEST_INTERVAL);
						JSONObject heartbeatMessage = new JSONObject();
						heartbeatMessage = this.serverMessage.heartbeatMessage(ServerState.getServerState().getServerName());
						
						Sender.sendRespond(ServerState.getServerState().getLeaderServer().getServerSocketConnection(), heartbeatMessage);
						
						logger.debug("heartbeat sent from "+ ServerState.getServerState().getServerName() + "to " + ServerState.getServerState().getLeaderServer().getServerName()+"(Leader)");
					} catch (IOException e) {
						logger.debug("Heartbeat sending Failed from "+ ServerState.getServerState().getServerName() + "to " + ServerState.getServerState().getLeaderServer().getServerName()+"(Leader)");
						Runnable LeaderDown = new Heartbeat("LeaderDown");
		                new Thread(LeaderDown).start();
		                
					} catch (InterruptedException e) {
						logger.debug("Heartbeat request interval error");
					}
				}
			}
		case "LeaderDown":
			ServerState.getServerState().removeSuspectServer(ServerState.getServerState().getLeaderServer().getServerName());
			ServerState.getServerState().setLeaderServer(null);
			try {
				Thread.sleep(Constants.LEADER_DOWN_RECOGNITION_WAITING); // wait until all servers set as that leader is null
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(new HashSet<Boolean>(ServerState.getServerState().isInProgressLeaderElection().values()).size() == 1) { //check all are false
				
				ServerState.getServerState().setInProgressLeaderElection(ServerState.getServerState().getServerName(),true);
				sendProgressStatus();
				
				try {
					Thread.sleep(Constants.LEADER_DOWN_RECOGNITION_WAITING);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Server newLeader = LeaderElector.getLeader();
				ServerState.getServerState().setLeaderServer(newLeader);
				
				ServerState.getServerState().setInProgressLeaderElection(ServerState.getServerState().getServerName(),false);
				sendProgressStatus();
				break;
			}
		}
	}
	
	private void sendProgressStatus() {
		JSONObject startElectionRequestMessage = new JSONObject();
		startElectionRequestMessage = this.serverMessage.startElectionRequestMessage(ServerState.getServerState().getLeaderServer(), ServerState.getServerState().isInProgressLeaderElection());
		
		ArrayList<Server> serverList = (ArrayList<Server>) ServerState.getServerState().getServersHashmap().values();
		this.gossiping.sendServerBroadcast(startElectionRequestMessage, serverList);
	}

	public static void receiveLeaderProgress(JSONObject response) {
		
		//set new leader
		Server newLeader = (Server) response.get("newLeader");
		ServerState.getServerState().setLeaderServer(newLeader);
		
		//update isInProgressLeaderElection hashmap
		ConcurrentHashMap<String, Boolean> isInProgressLeaderElection = (ConcurrentHashMap<String, Boolean>) response.get("electionStatusHashMap");
		for (String serverName : isInProgressLeaderElection.keySet()) {
			if(ServerState.getServerState().isInProgressLeaderElection().containsKey(serverName)) {
				ServerState.getServerState().setInProgressLeaderElection(serverName, isInProgressLeaderElection.get(serverName));
			}
		}
	}

	
	public static void updateHeartbeat(JSONObject response) {
		if(ServerState.getServerState().getServerName().equals(ServerState.getServerState().getLeaderServer().getServerName())) {
			String senderID = response.get( "sender" ).toString();
			logger.debug("heartbeat sent to leader by "+ senderID);
			
//			String currentTime = getCurrentTime();
//			String previousTime = ServerState.getServerState().getPreviousHeartbeatHashmap().get(senderID);
//			long duration = 0;
//			if (previousTime != null) {
//				duration = getDuration(currentTime, previousTime);
//			}
//			else {
//				duration = getDuration(currentTime, "0");
//			}
//			
//			if(duration <= Constants.TIMEOUT_INTERVAL) {
//				
//				//update previousHeartbeatHashMap by current time
//				ServerState.getServerState().getPreviousHeartbeatHashmap().put(senderID, currentTime);
//				
//				//update heart beat count vector
//	            if (senderID.equals(ServerState.getServerState())) {
//	            	ServerState.getServerState().getHeartbeatCountList().put(senderID, 0); // reset my own vector always
//	            } else {
//	            	
//	            	// get current heart beat count of a server
//	                Integer count = ServerState.getServerState().getHeartbeatCountList().get(senderID);
//	                
//	                // up count all others
//	                if (count == null) {
//	                	ServerState.getServerState().getHeartbeatCountList().put(senderID, 1);
//	                } else {
//	                	ServerState.getServerState().getHeartbeatCountList().put(senderID, count + 1);
//	                }
//	            }
//			}
		}
		
		
		
	};
	
	public static String getCurrentTime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		return simpleDateFormat.format(now);
	}
	
	public static long getDuration(String time1, String time2) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

	    // Parsing the Time Period
		long differenceInMilliSeconds = 0;
		try {
			Date date1 = simpleDateFormat.parse(time1);
			Date date2 = simpleDateFormat.parse(time2);
			
			// Calculating the difference in milliseconds
			differenceInMilliSeconds= Math.abs(date2.getTime() - date1.getTime());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return differenceInMilliSeconds;
	};
	
}
