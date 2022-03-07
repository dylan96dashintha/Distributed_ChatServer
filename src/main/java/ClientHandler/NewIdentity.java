package ClientHandler;

public class NewIdentity {
	String name;
	public NewIdentity(String name) {
		this.name = name;
	}
	
	public String getName () {
		return name;
	}
	
	
	
	public boolean validation() {
		int size = name.length();
		if (size>3 && size <16 && isAlphaNumeric()) {
			UserList userList = new UserList();
			boolean isApproved = userList.addUser(name);
			return isApproved;
		} else {
			return false;
		}
	}
	
	   public boolean isAlphaNumeric () {
	        return  name.matches("^[a-zA-Z0-9]*$");
	    }
	

}
