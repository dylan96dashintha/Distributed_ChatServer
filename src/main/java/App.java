
import java.io.*;  
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner; 
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ClientHandler.ClientHandler;
import Connection.ClientServerConnection;
import Server.ServerState;


public class App 
{
	private static final Logger logger = LogManager.getLogger(App.class);
	
    public static void main( String[] args )
    {
    	
    	logger.info("Stating Server");
    	String confFilePath = "conf.txt"; 
    	ServerState currentServer = ServerState.getServerState().initializeServer("s1", confFilePath);
    	
    	//Create client connection
    	ServerSocket serverSocket = null;
    	Socket socket = null;
    	try {
    		serverSocket = new ServerSocket();
    		SocketAddress socketAddress = new InetSocketAddress(currentServer.getServerAddress(), currentServer.getClientPort());    		
    		serverSocket.bind(socketAddress);
    		logger.debug("Socket Address: "+ currentServer.getServerAddress()+ ", Port: "+ currentServer.getClientPort());
    	}catch (IOException e) {
    		logger.error(e.getMessage());		
    		}
    	
    	boolean isListening = true;
    	while (true) {
	       try {
               socket = serverSocket.accept();
               InputStream inputFromClient = socket.getInputStream();
               ClientServerConnection clientServerConnection = new ClientServerConnection(inputFromClient);
       			clientServerConnection.start();
           } catch (IOException e) {
        	   isListening = false;
        	   logger.error(e.getMessage());
        	   logger.error("Server Stop Listening");
        	   
           }
    	}


    	}
//        try{  
//        	ServerSocket ss=new ServerSocket(4444);  
//        	Socket s=ss.accept();
//        	InputStream inputFromClient = s.getInputStream();
//            Scanner scanner = new Scanner(inputFromClient, String.valueOf(StandardCharsets.UTF_8));
//            while (true) {
//                String line = scanner.nextLine();
//                System.out.println("Line == "+line);
//                ClientHandler clientHandler = new ClientHandler(getType(line));
//                clientHandler.getTypeFunctionality();
//                
//            }
//           // ss.close();  
//        	}catch(Exception e){System.out.println(e);}  
//      }
//    
//    protected static JSONObject getType(String line) {
//    	JSONObject jsnObj = new JSONObject(line);
//        //String type = jsnObj.getString("type");
//        return jsnObj;
//    }
    
}
