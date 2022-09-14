import java.util.ArrayList;

public class FilesPeer {
	int token_id;
	private ArrayList<String> files_list = new ArrayList<String>();

	// Constructor
	public FilesPeer(int token_id, ArrayList<String> files_list){
		this.token_id = token_id;
		this.files_list = files_list;
	}
	
	// Setters
	public void setID(int token_id){
		this.token_id = token_id;
	}

	public void setFilesList(ArrayList<String> files_list){
		this.files_list = files_list;
	}
	
	public void addFile(String filename){
		files_list.add(filename);
	}

	// Gettters
	public int getID(){
		return token_id;
	}

	public ArrayList<String> getFilesList(){
		return files_list;
	}
}
