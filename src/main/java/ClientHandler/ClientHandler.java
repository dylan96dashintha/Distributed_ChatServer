package ClientHandler;
import org.json.JSONObject;
import org.json.JSONException;

//Handling types 


public class ClientHandler {
	String type;
	JSONObject jsnObj;
	public ClientHandler(JSONObject jsnObj) {
		this.type = jsnObj.getString("type");
		this.jsnObj = jsnObj;
		
	}
	
	public void getTypeFunctionality() {
		switch (type) {
		case "newidentity":
			NewIdentity newIdentity = new NewIdentity(jsnObj.getString("identity"));
			boolean isApproved = newIdentity.validation();
			String res;
			if (isApproved) {
				res = new JSONObject().put("approved", "true").put("type", "newidentity").toString();
				//TODO-List
				//Message this response 
			} else {
				res = new JSONObject().put("approved", "false").put("type", "newidentity").toString();
			}
			
			System.out.println("new identity :: "+ res );
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
