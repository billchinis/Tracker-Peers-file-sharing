import java.util.ArrayList;

public class InfoFile {
	private String fileName;
	private ArrayList<Integer> token_id_list = new ArrayList<Integer>();
	
	InfoFile(String fileName, ArrayList<Integer> token_id_list){
		this.fileName = fileName;
		this.token_id_list = token_id_list;
	}
	
	// Setters
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void setToken_id_list(ArrayList<Integer> token_id_list){
		this.token_id_list = token_id_list;
	}
	
	// Getters
	public String getFileName(){
		return fileName;
	}
	
	public ArrayList<Integer> getToken_id_list(){
		return token_id_list;
	}
}
