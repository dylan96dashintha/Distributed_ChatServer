package Server;

import java.net.Socket;
import java.util.ArrayList;

//functionalities of server
public class Server {
	


	private String serverName;
	private String serverAddress;
	private Integer serverId;
	private int serverPort, clientPort;
	
	private Socket serverSocketConnection;
	

	public Server(String serverName, String serverAddress, int serverPort, int clientPort) {
		this.serverName = serverName;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.clientPort = clientPort;
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
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public int getClientPort() {
		return clientPort;
	}
	public int getServerId(){
		return serverId;
	}
	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}
	public Socket getServerSocketConnection() {
		return serverSocketConnection;
	}
	public void setServerSocketConnection(Socket serverSocket) {
		this.serverSocketConnection = serverSocket;
	}

}
