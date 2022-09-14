import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Peer {
	private static Scanner scan = new Scanner(System.in);
	private static Random rn = new Random();
	
	private static InfoConnectedPeer infoPeer = null;
	private static int port = 1000 + rn.nextInt(3000);
	
	private static ArrayList<String> files_list = new ArrayList<String>();
	
	// Sockets for tracker
	static Socket trackerRequestSocket = null;
	static ObjectOutputStream trackerOut = null;
	static ObjectInputStream trackerIn = null;
	
	public static void main(String args[]) {
		// tracker
		String trackerIP = "192.168.1.4";
		int trackerPORT = 2222;
		
		try {
			trackerRequestSocket = new Socket(trackerIP, trackerPORT);
			trackerOut = new ObjectOutputStream(trackerRequestSocket.getOutputStream());
			trackerIn = new ObjectInputStream(trackerRequestSocket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createFilesList();
		
		PeerActions t = null;
		try {
			t = new PeerActions(port, Inet4Address.getLocalHost().getHostAddress());
			t.start();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int ans = 4;
		
		do{
			// Read choice
			do{
				showMenu();
				ans = scan.nextInt();
			}while(ans > 5 && ans < 1);
			
			try {
				trackerOut.writeInt(ans);
				trackerOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Call method
			if(ans == 1){
				Register();
			}
			if(ans == 2){
				if(infoPeer == null){
					Login();
				}
				else{
					System.out.println("Logout and try again.");
					System.out.println();
				}
			}
			if(ans == 4){
				simpleDownload();
			}
			if(ans == 3 || ans == 5){
				if (infoPeer != null){
					Logout();
				}
			}
		}while(ans != 5);
		
		t.finish();
	}
	
	// Create files_list of peer
	public static void createFilesList(){
		File folder = null;
		try{
			folder = new File("shared_directory/");
			
			for (File f : folder.listFiles()) {
			    if (f.isFile()) {
			    	files_list.add(f.getName());
			    }
			}
		}catch (NullPointerException e) {
			System.err.println ("File not found.");
		}
	}
	
	// Menu
	public static void showMenu(){
		System.out.println("Choose:");
		System.out.println("1. Register");
		System.out.println("2. Login");
		System.out.println("3. Logout");
		System.out.println("4. Simple Download");
		System.out.println("5. Exit");
	}
	
	// Register Method
	public static void Register(){
		boolean trackerAns = false;
		scan.nextLine();
		
		while(!trackerAns){
			System.out.print("Give username: ");
			String username = scan.nextLine();
			
			System.out.print("Give password: ");
			String password = scan.nextLine();
			
			try {
				trackerOut.writeObject(username);
				trackerOut.flush();
				
				trackerOut.writeObject(password);
				trackerOut.flush();
				
				trackerAns = trackerIn.readBoolean();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!trackerAns){
				System.out.println("Try another username and password.");
				System.out.println();
			}
		}
		
		System.out.println("Registered successfully.");
		System.out.println();
	}
	
	// Login Method
	public static void Login(){
		boolean trackerAns = false;
		int token_id;
		String username = "", password = "";
		scan.nextLine();
		
		while(!trackerAns){
			System.out.print("Give username: ");
			username = scan.nextLine();
			
			System.out.print("Give password: ");
			password = scan.nextLine();
			
			try {
				trackerOut.writeObject(username);
				trackerOut.flush();
				
				trackerOut.writeObject(password);
				trackerOut.flush();
				
				trackerAns = trackerIn.readBoolean();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!trackerAns){
				System.out.println("Try another username and password.");
				System.out.println();
			}
		}
		
		try {
			token_id = trackerIn.readInt();
			infoPeer = new InfoConnectedPeer(token_id, Inet4Address.getLocalHost().getHostAddress(), port, username);
			
			trackerOut.writeObject(infoPeer.getIP_address());
			trackerOut.flush();
			
			trackerOut.writeObject(infoPeer.getPort());
			trackerOut.flush();
			
			trackerOut.writeObject(files_list);
			trackerOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Logged in successfully.");
		System.out.println();
	}
	
	// Logout Method
	public static void Logout(){
		boolean trackerAns = false;
		try {
			while(!trackerAns){
				trackerOut.writeInt(infoPeer.getToken_id());
				trackerOut.flush();
				
				trackerAns = trackerIn.readBoolean();
			}
			
			infoPeer = null;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Logged out successfully.");
		System.out.println();
	}
	
	// checkAlive method
	public static boolean checkAlive(int port, String ip){
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
	
	// simpleDownload method
	public static void simpleDownload(){
		String filename = "";
		ArrayList<InfoConnectedPeer> peers = new ArrayList<>();
		
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		scan.nextLine();
		
		boolean flag = false;
				
		do{
			System.out.print("Give filename: ");
			filename = scan.nextLine();
			System.out.println();
			
			peers = search(filename);
			
			if(peers.size() == 0){
				System.out.println("File not found, try again");
			}			
			else{
				InfoConnectedPeer[] connectedPeerSorted = connectedPeerSort(peers);
				
				
				int counter = 0;
				InfoConnectedPeer peer = null;
				do{
					peer = connectedPeerSorted[counter];
					
					OutputStream outputStream = null;
					
					try {
						outputStream = new FileOutputStream("shared_directory/" + filename);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					byte[] bytes = new byte[16*1024];
					
					int count;
					
					try {
						requestSocket = new Socket(peer.getIP_address(), peer.getPort());
						out = new ObjectOutputStream(requestSocket.getOutputStream());
						in = new ObjectInputStream(requestSocket.getInputStream());
						
						out.writeBoolean(false);
						out.flush();
						
						out.writeObject(filename);
						out.flush();
						
						long length = in.readLong();
						int k = 0;
						
						while (k < length) {
							count = in.read(bytes);
							
							k = k + count;
							
							outputStream.write(bytes, 0, count);
						}
						
						outputStream.close();
						
						flag = true;
						counter++;
					} catch (IOException e) {
						flag = false;
					}
				}while(!flag && counter < connectedPeerSorted.length);
								
				if(!flag){
					System.out.println("Error! Download unsuccessful!");
					System.out.println();
				}
				else{
					try {
						trackerOut.writeObject(peer.getUserName());
						trackerOut.flush();
						
						trackerOut.writeObject(infoPeer.getToken_id());
						trackerOut.flush();
						
						trackerOut.writeObject(filename);
						trackerOut.flush();
						
						System.out.println("File with filename: " + filename + " downloaded successfully from Peer with token_id: " + infoPeer.getToken_id() + ".");
						System.out.println();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}while(peers.size() == 0);
	}
	
	// Sort connected peers from ArrayList peers
	public static InfoConnectedPeer[] connectedPeerSort(ArrayList<InfoConnectedPeer> peers){
		InfoConnectedPeer[] connectedPeerSorted = new InfoConnectedPeer[peers.size()];
		long[] checkAliveTime = new long[peers.size()];
		
		int i = 0;
		long curTime;
		for(InfoConnectedPeer p : peers){
			connectedPeerSorted[i] = p;
			
			curTime = System.nanoTime();
			
			checkAlive(p.getPort(), p.getIP_address());
			
			curTime = System.nanoTime() - curTime;
			checkAliveTime[i] = curTime;
			i++;
		}
		
		long temp;
		InfoConnectedPeer tempInfoConnectedPeer;
		
		for (i = 0; i < checkAliveTime.length; i++){
			for (int j = 1; j < (checkAliveTime.length - i); j++) {
				if (checkAliveTime[j - 1] > checkAliveTime[j]) {
	                temp = checkAliveTime[j - 1];
	                checkAliveTime[j - 1] = checkAliveTime[j];
	                checkAliveTime[j] = temp;
	                
	                tempInfoConnectedPeer = connectedPeerSorted[j - 1];
	                connectedPeerSorted[j - 1] = connectedPeerSorted[j];
	                connectedPeerSorted[j] = tempInfoConnectedPeer;
	            }
			}
		}
		
		return connectedPeerSorted;
	}
	
	// Search peers that have a file
	public static ArrayList<InfoConnectedPeer> search(String filename){
		ArrayList<InfoConnectedPeer> peers = new ArrayList<>();
		
		try {
			trackerOut.writeObject(filename);
			trackerOut.flush();
			
			int size = (int) trackerIn.readObject();
			
			for(int i = 0; i < size; i++){
				int token_id = 0, port = 0;
				String ip_address = "", user_name = "";
				
				user_name = (String) trackerIn.readObject();
				
				token_id = (int) trackerIn.readObject();
				
				port = (int) trackerIn.readObject();
				
				ip_address = (String) trackerIn.readObject();				
				
				InfoConnectedPeer p = new InfoConnectedPeer(token_id, ip_address, port, user_name);
				peers.add(p);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(peers.size() == 0){
			System.out.println("File with filename:" + filename + " not found.");
		}
		else{
			System.out.println("File with filename:" + filename + " found.");
		}
		System.out.println();
		
		return peers;
	}
}
