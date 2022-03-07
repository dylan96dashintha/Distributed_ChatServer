package Server;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ClientHandler.ClientHandler;



public class ServerHandler {
	
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);
	
	public void handleResponse(Socket socket, String obj) {
		JSONObject response = new JSONObject(obj);
		
		String type = response.getString("type");
		
		switch(type) {
		case "server-connection":
			logger.info(response.toString());
			Server server = ServerState.getServerState().getServerByName(response.getString("server"));
			server.setServerSocketConnection(socket);
			ServerState.getServerState().replaceServerbByName(server);
		}
	}
	
	
}
