package Messaging;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ClientHandler.User;
import Server.ChatRoom;


public class Sender {
	
	private static final Logger logger = LogManager.getLogger(Sender.class);
	
	public static void sendRespond(Socket socket, JSONObject jsonObj) throws IOException {
		DataOutputStream opStream = new DataOutputStream(socket.getOutputStream());
		opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
		opStream.flush();
	}
	
	public static void sendMessageChatroom(String chatRoomName, JSONObject jsonObj) throws IOException {
		
		ChatRoom chatRoom = new ChatRoom();
		ConcurrentHashMap<String, ChatRoom> chatRoomHashMap = chatRoom.getChatRoomHashMap();
		ChatRoom newChatRoom = chatRoomHashMap.get(chatRoomName);
		
		ConcurrentLinkedQueue <User> UserList = newChatRoom.getUserListInRoom();
		
		
		   for(User user: UserList)
		   {
				DataOutputStream opStream = new DataOutputStream(user.getUserSocket().getOutputStream());
				opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
				opStream.flush();
		   }
		
		
	}
}
