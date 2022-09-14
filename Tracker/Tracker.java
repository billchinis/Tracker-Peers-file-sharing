import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Tracker {
	public static Map<String, InfoRegisteredPeer> registeredPeers =  new ConcurrentHashMap<String, InfoRegisteredPeer>();
	public static Map<Integer, InfoConnectedPeer> connectedPeers =  new ConcurrentHashMap<Integer, InfoConnectedPeer>();
	public static Map<Integer, FilesPeer> fp =  new ConcurrentHashMap<Integer, FilesPeer>();
		
	public static AtomicInteger token = new AtomicInteger(0);
	
	private ServerSocket providerSocket;
	private Socket connection = null;
	
	public static void main(String args[]) {
		new Tracker().openServer();
	}
	
	void openServer() {
		try {
			providerSocket = new ServerSocket(2222, 10);
			
			while(true){
				connection = providerSocket.accept();
				Thread t = new TrackerActionsForPeer(connection);
				t.start();
			}

		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
