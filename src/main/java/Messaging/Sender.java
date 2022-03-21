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
		logger.debug("Send respond :: "+jsonObj+" socket :: "+socket);
		DataOutputStream opStream = new DataOutputStream(socket.getOutputStream());
		opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
		opStream.flush();
	}
	
	public static void sendMessageChatroom(String chatRoomName, JSONObject jsonObj) throws IOException {
		
		logger.debug("Chatroom name  ::  "+chatRoomName+jsonObj);
		
		ChatRoom chatRoom = new ChatRoom();
	
		ConcurrentLinkedQueue <User> UserList = chatRoom.getUserListInRoom(chatRoomName);
		
		
		   for(User user: UserList)
		   {
				DataOutputStream opStream = new DataOutputStream(user.getUserSocket().getOutputStream());
				opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
				opStream.flush();
		   }
		
		
	}
	
	
	public static void sendMessageToUserList(ConcurrentLinkedQueue<User> userList, JSONObject jsonObj)
			throws IOException {

		logger.debug("sendMessageToUserList() " + userList.toString());

		for (User user : userList) {
			DataOutputStream opStream = new DataOutputStream(user.getUserSocket().getOutputStream());
			opStream.write((jsonObj.toString() + "\n").getBytes(StandardCharsets.UTF_8));
			opStream.flush();
		}

	}
	
	public static void sendNotificationChatroom(String chatRoomName, JSONObject jsonObj,String username) throws IOException {
		
		
		
		ChatRoom chatRoom = new ChatRoom();
	
		ConcurrentLinkedQueue <User> UserList = chatRoom.getUserListInRoom(chatRoomName);
		
		
		   for(User user: UserList)
		   {
			   logger.debug("sendMessageFormerChatroom ::  "+chatRoomName+jsonObj+(user.getName()).equals(username)+user.getName()+username);
			   if (!(user.getName()).equals(username)) {
					DataOutputStream opStream = new DataOutputStream(user.getUserSocket().getOutputStream());
					opStream.write((jsonObj.toString()+ "\n").getBytes(StandardCharsets.UTF_8));
					opStream.flush();
			   }
			   
		   }
		
		
	}
	
	
	
	
	
}
