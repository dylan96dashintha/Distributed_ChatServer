package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ClientHandler.ClientHandler;
import Gossiping.GossipingHandler;
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
		try {

			while (true) {
				String line = this.scanner.nextLine();				
				handleResponse(line);
				
			}

		} catch (Exception e) {
			System.out.println(e);
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
				this.serverHandeler.newServerConnection(this.socket, response.getString("server"));
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
