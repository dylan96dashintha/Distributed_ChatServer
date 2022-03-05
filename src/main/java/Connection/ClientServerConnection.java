package Connection;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.json.JSONObject;

import ClientHandler.ClientHandler;

public class ClientServerConnection extends Thread {
	InputStream inputFromClient;
	Scanner scanner;
	public ClientServerConnection(InputStream inputFromClient) {
		this.inputFromClient = inputFromClient;
	    scanner = new Scanner(inputFromClient, String.valueOf(StandardCharsets.UTF_8));
	}
	
	@Override
	public void run() {
		try {

	    while (true) {
	        String line = scanner.nextLine();
	        System.out.println("Line == "+line);
	        ClientHandler clientHandler = new ClientHandler(getType(line));
	        clientHandler.getTypeFunctionality();
	        
	    }
	   // ss.close();  
		}catch(Exception e){System.out.println(e);
		}
		
	}
	
	
	protected static JSONObject getType(String line) {
    	JSONObject jsnObj = new JSONObject(line);
        //String type = jsnObj.getString("type");
        return jsnObj;
    }

}
