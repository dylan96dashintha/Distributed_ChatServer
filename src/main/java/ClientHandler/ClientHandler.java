package ClientHandler;


//Handling types 


public class ClientHandler {
	String type;
	public ClientHandler(String type) {
		this.type = type;
	}
	
	public void getTypeFunctionality() {
		switch (type) {
		case "newidentity":
			System.out.println("new identity");
			break;
		case "message":
			System.out.println("message");
			break;
		case "list":
			System.out.println("list");
			break;
		case "who":
			System.out.println("who");
			break;
		case "createroom":
			System.out.println("createroom");
			break;
		case "joinroom":
			System.out.println("joinroom");
			break;
		case "deleteroom":
			System.out.println("deleteroom");
			break;
		case "quit":
			System.out.println("quit");
			break;
		}
	} 
}
