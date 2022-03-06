
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


public class App 
{
	private static final Logger logger = LogManager.getLogger(App.class);
	
    public static void main( String[] args )
    {
    	
    	logger.info("Stating Server");

    	try {
    		ServerSocket ss = new ServerSocket(4444);
    		while (true) {
    			System.out.println("Run wenawada");
        		Socket s=ss.accept();
        		InputStream inputFromClient = s.getInputStream();
        		System.out.println("Run wenawasasasaaaaaaaaaaaaa");
        		ClientServerConnection clientServerConnection = new ClientServerConnection(inputFromClient);
        		clientServerConnection.start();
        	}
		} catch (Exception e) {
			// TODO: handle exception
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
