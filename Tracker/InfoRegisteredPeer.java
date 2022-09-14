
public class InfoRegisteredPeer {
	private String user_name, password;
	private int count_downloads;

	// Constructor
	public InfoRegisteredPeer(String user_name, String password, int count_downloads){
		this.user_name = user_name;
		this.password = password;
		this.count_downloads = count_downloads;
	}

	// Setters
	public void setUserName(String newName){
		user_name = newName;
	}

	public void setPass(String newPass){
		password = newPass;
	}

	public void setCount_downloads(int count_downloads){
		this.count_downloads = count_downloads;
	}

	// Getters
	public String getUserName(){
		return user_name;
	}

	public String getPass(){
		return password;
	}

	public int getCount_downloads(){
		return count_downloads;
	}
}
