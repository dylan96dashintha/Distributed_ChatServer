package Connection;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ClientHandler.ClientHandler;

public class ClientServerConnection extends Thread {
	private static final Logger logger = LogManager.getLogger(ClientServerConnection.class);
	InputStream inputFromClient;
	Scanner scanner;
	Socket socket;
	private ClientHandler clientHandler;
	public ClientServerConnection(Socket socket) {
		try {
			this.socket = socket;
			clientHandler = new ClientHandler(socket);
			InputStream inputFromClient = this.socket.getInputStream();
			this.inputFromClient = inputFromClient;
		    scanner = new Scanner(inputFromClient, String.valueOf(StandardCharsets.UTF_8));
		    
		}catch(Exception e) {
			//TODO handle errors
		}
		
	}
	
	@Override
	public void run() {
		try {

	    while (true) {
	    	
	        String line = scanner.nextLine();
	        clientHandler.getTypeFunctionality(getType(line));
	        
	    }
	   // ss.close();  
		}catch(Exception e){System.out.println(e);
		logger.debug("CTRL+C");
		clientHandler.getTypeFunctionality(getType("{\"type\": \"quitctrl\"}"));
		
		}
		
	}
	
	
	protected static JSONObject getType(String line) {
    	JSONObject jsnObj = new JSONObject(line);
        //String type = jsnObj.getString("type");
        return jsnObj;
    }

}
