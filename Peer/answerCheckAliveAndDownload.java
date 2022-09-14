import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class answerCheckAliveAndDownload extends Thread{
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private boolean finished = false;
	
	private boolean alive = true;
	
	answerCheckAliveAndDownload(Socket connection){
		try {
			in = new ObjectInputStream(connection.getInputStream());
			out = new ObjectOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			alive = false;
		}
	}
	
	public void answerCheckAlive(){
		String message;
		try {
			if(alive){				
				message = (String) in.readObject();				
	
				if(message.equals("alive?")){
					out.writeBoolean(true);
					out.flush();
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void simpleDownload(){
		String filename = "";
		
		try {
			filename = (String) in.readObject();
			
			File file = new File("shared_directory/" + filename);
			
			InputStream inputStream = new FileInputStream(file);
			
			long length = file.length();
			
			out.writeLong(length);
			out.flush();
			
			byte[] bytes = new byte[16 * 1024];
			
			int count;
			int k = 0;
			while (k < length) {
				count = inputStream.read(bytes);
				
				k = k + count;
				
				out.write(bytes, 0, count);
	            out.flush();	            
	        }
			
			System.out.println("A Peer downloaded file with filename: " + filename + ".");
			System.out.println();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {		
		boolean flag;
		try {
			flag = in.readBoolean();
			
			if(flag){
				answerCheckAlive();
			}
			else{
				simpleDownload();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
