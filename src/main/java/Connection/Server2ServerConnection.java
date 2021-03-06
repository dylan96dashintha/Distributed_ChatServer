package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ClientHandler.ClientHandler;
import Gossiping.GossipingHandler;
import Server.LeaderElector;
import Heartbeat.ConsensusJob;
import Heartbeat.GossipJob;
import Heartbeat.Heartbeat;
import Server.Server;
import Server.ServerHandler;
import Server.ServerState;

public class Server2ServerConnection extends Thread{

	private static final Logger logger = LogManager.getLogger(Server2ServerConnection.class);
	
	private Socket socket;
	private InputStream inputFromClient;
	private Scanner scanner;
	private ServerHandler serverHandeler;
	
	public Server2ServerConnection(Socket socket) {
		this.serverHandeler = new ServerHandler();
		try {
			this.socket = socket;
			this.inputFromClient = this.socket.getInputStream();
			this.scanner = new Scanner(this.inputFromClient, String.valueOf(StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
		

	@Override
	public void run() {
		while (true) {
			try {
				String line = this.scanner.nextLine();				
				handleResponse(line);
			}catch(NoSuchElementException j) {
//				logger.debug("this is it");
				break;
			}
			
		}
	}
	
	
	public void handleResponse(String obj) {
		JSONObject response = new JSONObject(obj);
		GossipingHandler gossiping = new GossipingHandler();
		
		String type = response.getString("type");
//		logger.debug(response.toString());
		switch(type) {
		case "server-connection-request":	
			try {
				this.serverHandeler.newServerConnection(this.socket, response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "server-connection-response":
			this.serverHandeler.newServerConnectionConfirm(response);
			break;
		
			
		case "gossiping":			
			try {
				gossiping.doGossiping(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "global-chatrooms-request":
			try {
				this.serverHandeler.getGlobalChatRooms(this.socket, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "global-chatrooms-response":
			try {
				this.serverHandeler.setGlobalChatRooms(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "global-identity-request":
			try {
				this.serverHandeler.getGlobalIdentities(this.socket, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "global-identity-response":
			try {
				this.serverHandeler.setGlobalIdentities(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "election":
			String electionMsgType = response.getString("electionMsgType");
			switch(electionMsgType){
				case "start_election":
					LeaderElector.processStartElectionMsg(response);
					break;

				case "answer_election":
					LeaderElector.processAnswerElectionMsg(response);
					break;

				case "nomination":
					LeaderElector.processNominationMsg(response);
					break;

				case "inform_coordinator":
					LeaderElector.processInformCoordinatorMsg(response);
					break;

				case "IamUp":
					LeaderElector.processIamUpMsg(response);
					break;
					
				case "view":
					LeaderElector.processViewMsg(response);
					break;
			}
			break;
		case "heartbeat":
			Heartbeat.updateHeartbeat(response);
			break;
		
		case "heartbeat-gossip":
			GossipJob.receiveMessages(response);
			break;
			
		case "startVote":
			ConsensusJob.startVoteMessageHandler(response);
			break;
			
		case "answervote":
			ConsensusJob.answerVoteHandler(response);
			break;
			
		case "notifyserverdown":
			ConsensusJob.notifyServerDownMessageHandler(response);
			break;
		
		case "deleteroom":
			this.serverHandeler.deleteChatroom(response);
			break;
		
		case "gossip-chatrooms-request":
			try {
				gossiping.sendChatRoomCreateGossip();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		
		}	
	}
}
