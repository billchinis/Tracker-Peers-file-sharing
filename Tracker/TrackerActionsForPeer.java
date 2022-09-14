import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class TrackerActionsForPeer extends Thread{
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	boolean loggedIn = false;
	
	public TrackerActionsForPeer(Socket connection) {		
		try {
			in = new ObjectInputStream(connection.getInputStream());
			out = new ObjectOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			int choice = 0;
			
			do{
				choice = in.readInt();
				
				if(choice == 1){
					Register();
				}
				if(choice == 2){
					Login();
					loggedIn = true;
				}
				if(choice == 4){
					search();
					updateFromDownload();
				}
				if(choice == 3 || choice == 5){
					if(loggedIn){
						Logout();
						loggedIn = false;
					}
				}
			}while(choice != 5);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void Register(){
		String username = "";
		String password = "";
		
		boolean check;
		
		try {
			check = false;
			
			while(!check){
				username = (String) in.readObject();
				password = (String) in.readObject();
				
				check = true;
				
				// Check if registered
				if(Tracker.registeredPeers.get(username) != null){
					check = false;
				}
				
				out.writeBoolean(check);
				out.flush();
			}
			
			InfoRegisteredPeer newPeer = new InfoRegisteredPeer(username, password, 0);
			
			Tracker.registeredPeers.put(username, newPeer);
			
			System.out.println("User with username: " + username + " registered successfully to tracker");
			System.out.println();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Login(){
		String username = "";
		String password = "";
		
		boolean check = false;
		
		try {
			while(!check){
				username = (String) in.readObject();
				password = (String) in.readObject();
				
				InfoRegisteredPeer regPeer = Tracker.registeredPeers.get(username);
				
				// Check if registered
				if(regPeer != null){
					check = true;
				}
				
				// Check if connected
				Iterator<Integer> itConnectedPeers = Tracker.connectedPeers.keySet().iterator();
				
				// if connectedPeers is not empty
				if(itConnectedPeers.hasNext()){
					while(itConnectedPeers.hasNext()){
						InfoConnectedPeer connectedPeer = Tracker.connectedPeers.get(itConnectedPeers.next());
						if(username.equals(connectedPeer.getUserName())){
							check = false;
							break;
						}
					}
				}
				
				out.writeBoolean(check);
				out.flush();
			}
			
			Tracker.token.incrementAndGet();
			int token_id = Tracker.token.get();
			out.writeInt(Tracker.token.get());
			out.flush();
			
			String ip_address = (String) in.readObject();
			
			int port = (int) in.readObject();
			
			InfoConnectedPeer p = new InfoConnectedPeer(token_id, ip_address, port, username);
			Tracker.connectedPeers.put(token_id, p);
			
			FilesPeer f = new FilesPeer(token_id, (ArrayList<String>) in.readObject());
			Tracker.fp.put(token_id, f);
			
			System.out.println(username + " logged in successfully to tracker.");
			System.out.println();
			
			// if count_downloads = 0 sleep for 100ms
			InfoRegisteredPeer registeredPeer = Tracker.registeredPeers.get(username);
			if(registeredPeer != null){
				if(registeredPeer.getCount_downloads() == 0){
					try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
	
	public void Logout(){
		try {			
			int token_id = in.readInt();
			
			deleteConnectedPeer(token_id);
			
			out.writeBoolean(true);
			out.flush();
			
			System.out.println("Peer with token_id:" + token_id + " logged out successfully from tracker.");
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void search(){
		String filename = "";
		String username = "";
		ArrayList<InfoConnectedPeer> peers = new ArrayList<>();
		
		Iterator<Integer> itFP = Tracker.fp.keySet().iterator();
		
		try {
			filename = (String) in.readObject();
			
			while(itFP.hasNext()){
				FilesPeer f = Tracker.fp.get(itFP.next());
				
				ArrayList<String> files_list =  f.getFilesList();
				
				for(String file : files_list){
					if(filename.equals(file)){
						peers.add(Tracker.connectedPeers.get(f.token_id));
						break;
					}
				}
			}
			
			Iterator<Integer> itConnectedPeers = Tracker.connectedPeers.keySet().iterator();
			while(itConnectedPeers.hasNext()){
				InfoConnectedPeer connectedPeer = Tracker.connectedPeers.get(itConnectedPeers.next());
				boolean checkAlivePeer = checkAlive(connectedPeer.getPort(), connectedPeer.getIP_address());
				if(!checkAlivePeer){
					deleteConnectedPeer(connectedPeer.getToken_id());
					
					for(int i = 0; i < peers.size(); i++){
						if(peers.get(i).getToken_id() == connectedPeer.getToken_id()){
							peers.remove(i);
						}
					}
				}
			}
			
			if(peers.size() == 0){
				System.out.println("File with filename:" + filename + " not found.");
			}
			else{
				System.out.println("File with filename:" + filename + " found.");
			}
			System.out.println();
			
			out.writeObject(peers.size());
			out.flush();
			
			for(InfoConnectedPeer info : peers){
				out.writeObject(info.getUserName());
				out.flush();
				
				out.writeObject(info.getToken_id());
				out.flush();
				
				out.writeObject(info.getPort());
				out.flush();
				
				out.writeObject(info.getIP_address());
				out.flush();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteConnectedPeer(int token_id){
		// Remove Peer from connectedPeers
		Iterator<Integer> itConnectedPeers = Tracker.connectedPeers.keySet().iterator();
		
		while(itConnectedPeers.hasNext()){
			InfoConnectedPeer connectedPeer = Tracker.connectedPeers.get(itConnectedPeers.next());
			if(token_id == connectedPeer.getToken_id()){
				Tracker.connectedPeers.remove(token_id);
			}
		}
		
		// Remove Files List of Peer from Trackers fp
		Iterator<Integer> itFP = Tracker.fp.keySet().iterator();
		
		while(itFP.hasNext()){
			FilesPeer f = Tracker.fp.get(itFP.next());
			if(token_id == f.getID()){
				Tracker.fp.remove(token_id);
			}
		}
	}
	
	// checkAlive method
	public boolean checkAlive(int port, String ip){
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		boolean check = false;
		
		try {
			requestSocket = new Socket(ip, port);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			out.writeBoolean(true);
			out.flush();
			
			out.writeObject("alive?");
			out.flush();
			
			check = in.readBoolean();
		} catch (UnknownHostException e) {
			check = false;
		} catch (IOException e) {
			check = false;
		}
		
		return check;
	}
	
	// Update Lists after Download
	public void updateFromDownload(){
		try {
			String userNameOfSender = (String) in.readObject();
			Tracker.registeredPeers.get(userNameOfSender).setCount_downloads(Tracker.registeredPeers.get(userNameOfSender).getCount_downloads() + 1);
			
			int tokenIDOfReceiver = (int) in.readObject();
			
			String filenameSent = (String) in.readObject();
			
			Tracker.fp.get(tokenIDOfReceiver).addFile(filenameSent);			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
