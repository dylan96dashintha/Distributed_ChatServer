package Messaging;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Sender {
	
	private static final Logger logger = LogManager.getLogger(Sender.class);
	
	public static void sendRespond(Socket socket, JSONObject jsonObj) throws IOException {
		DataOutputStream opStream = new DataOutputStream(socket.getOutputStream());
		opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
		opStream.flush();
	}
}
