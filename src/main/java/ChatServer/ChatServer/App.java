package ChatServer.ChatServer;
import java.io.*;  
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner; 
import org.json.JSONObject;
import org.json.JSONException;
import ClientHandler.ClientHandler;


public class App 
{
    public static void main( String[] args )
    {

        try{  
        	ServerSocket ss=new ServerSocket(4444);  
        	Socket s=ss.accept();
        	InputStream inputFromClient = s.getInputStream();
            Scanner scanner = new Scanner(inputFromClient, String.valueOf(StandardCharsets.UTF_8));
            while (true) {
                String line = scanner.nextLine();
                System.out.println("Line == "+line);
                ClientHandler clientHandler = new ClientHandler(getType(line));
                clientHandler.getTypeFunctionality();
                
            }
           // ss.close();  
        	}catch(Exception e){System.out.println(e);}  
      }
    
    protected static JSONObject getType(String line) {
    	JSONObject jsnObj = new JSONObject(line);
        //String type = jsnObj.getString("type");
        return jsnObj;
    }
    
}
