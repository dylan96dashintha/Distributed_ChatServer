package Heartbeat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	boolean running = true;
	
	public Heartbeat(String option){
		this.option = option.toLowerCase();
		this.serverMessage = ServerMessage.getInstance();
		this.gossiping = new Gossiping();
	}
	
	@Override
	public void run() {
		switch(option) {
		case "heartbeat":
			
			while(this.running) {
				if(!ServerState.getServerState().getServerName().equals(ServerState.getServerState().getLeaderServer().getServerName())) {
					try {
						Thread.sleep(Constants.REQUEST_INTERVAL);
						JSONObject heartbeatMessage = new JSONObject();
						heartbeatMessage = this.serverMessage.heartbeatMessage(ServerState.getServerState().getServerName());
						
						Sender.sendRespond(ServerState.getServerState().getServerByName(ServerState.getServerState().getLeaderServer().getServerName()).getServerSocketConnection(), heartbeatMessage);	
						
					} catch (IOException e) {
						this.running = false;
						logger.info("Heartbeat sending Failed from "+ ServerState.getServerState().getServerName() + "to " + ServerState.getServerState().getLeaderServer().getServerName()+"(Leader)");
						Runnable LeaderDown = new Heartbeat("LeaderDown");
		                new Thread(LeaderDown).start();
		                
					} catch (InterruptedException e) {
						this.running = false;
						logger.error("Heartbeat request interval error");
					
					} catch (NullPointerException e) {
						this.running = false;
						logger.error(ServerState.getServerState().getLeaderServer().getServerName()+"(leader) is not connected yet to ["+ServerState.getServerState().getServerName()+"]");
					}
				}
			}
		break;
		case "leaderdown":
			logger.info("LEADER DOWN..(" + ServerState.getServerState().getLeaderServer().getServerName()+")");
			logger.info("Leader Election is started..");
			
			String serveName = ServerState.getServerState().getLeaderServer().getServerName();
			ServerState.getServerState().removeSuspectServer(serveName);
			ServerState.getServerState().setLeaderServer(null);
			
			Server newLeader = LeaderElector.getLeader();				
			ServerState.getServerState().setLeaderServer(newLeader);
			
			logger.info("Assigned new leader "+ ServerState.getServerState().getLeaderServer().getServerName());
		
			// Start heart beat after leader election
			if (!(ServerState.getServerState().getServerName().equals(newLeader.getServerName()))) {
				Thread heartbeatThread = new Thread(new Heartbeat("heartbeat"));
				heartbeatThread.start();
			}
			Thread.currentThread().interrupt();
			
			break;
		}
	}

	public static void updateHeartbeat(JSONObject response) {
		if(ServerState.getServerState().getServerName().equals(ServerState.getServerState().getLeaderServer().getServerName())) {
			String senderID = response.get( "sender" ).toString();
//			logger.info("heartbeat sent to leader by "+ senderID);
		}
	};
	
}
