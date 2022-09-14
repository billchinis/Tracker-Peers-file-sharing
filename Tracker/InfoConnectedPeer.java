public class InfoConnectedPeer {
	private int token_id, port;
	private String ip_address, user_name;
	
	// Constructor
	InfoConnectedPeer(int token_id, String ip_address, int port, String user_name){
		this.token_id = token_id;
		this.ip_address = ip_address;
		this.port = port;
		this.user_name = user_name;
	}
	
	// Setter
	public void setToken_id(int token_id){
		this.token_id = token_id;
	}
	
	public void setIP_address(String ip_address){
		this.ip_address = ip_address;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public void setUser_name(String user_name){
		this.user_name = user_name;
	}
	
	// Getter
	public int getToken_id(){
		return token_id;
	}
	
	public String getIP_address(){
		return ip_address;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getUserName(){
		return user_name;
	}
}
