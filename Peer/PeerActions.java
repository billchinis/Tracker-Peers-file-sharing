import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PeerActions extends Thread{
	private int port;
	private String ip;
	
	private ServerSocket providerSocket;
	private Socket connection = null;
	
	PeerActions(int port, String ip){
		this.port = port;
		this.ip = ip;
	}
	
	//@SuppressWarnings("deprecation")
	public void finish(){
		try {
			providerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stop();
	}
	
	public void run() {
		try {
			providerSocket = new ServerSocket(port, 10);
			
			while(true){
				connection = providerSocket.accept();
				
				Thread t = new answerCheckAliveAndDownload(connection);
				t.start();
			}

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
