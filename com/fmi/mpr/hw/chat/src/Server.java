import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

public class Server {
	
	final static String MULTICAST_INET_ADDR = "224.0.0.3";
	final static int MULTICAST_PORT = 6565;
	final static int SERVER_PORT = 6566;
	final static int MAX_LENGTH = 50000;
	
	static Set<String> connectedNames;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		connectedNames = new TreeSet<String>();
		
		try(DatagramSocket receiveSocket = new DatagramSocket(SERVER_PORT)) {		
			
			System.out.println("Chat room server started!");
			while(true) {
				byte[] buff = new byte[MAX_LENGTH];	
				DatagramPacket receivedPacket  = new DatagramPacket(buff, buff.length);
				receiveSocket.receive(receivedPacket);
				
				Boolean hasChange = false;
				Boolean shouldSend = true;
				String msg = new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
				String name = null;
				
				//New user registration
				if(msg.startsWith("#")) {
					name = msg.substring(1);
					shouldSend = !connectedNames.contains(name);
					ProcessNewName(name, receiveSocket, receivedPacket.getAddress(), receivedPacket.getPort());
					msg = name.concat(" entered the chat...");
					hasChange = true;
				}
				//User is logging out
				else if(msg.startsWith("?")) {
					name = msg.substring(1);
					LogoutUser(name);
					msg = name.concat(" left the chat...");
					hasChange = true;
				}
				else {
					name = msg.split(":")[0];
				}
				
				if(shouldSend && (hasChange || connectedNames.contains(name))) {
					
					byte[] finalBytes = hasChange ? msg.getBytes() : receivedPacket.getData();
					
					InetAddress address = InetAddress.getByName(MULTICAST_INET_ADDR);
					DatagramPacket multicastPacket = new DatagramPacket(finalBytes, finalBytes.length, address, MULTICAST_PORT);
					receiveSocket.send(multicastPacket);
				}
			}
		}
		
	}
	
	private static void ProcessNewName(String name, DatagramSocket socket, InetAddress address, int port) throws IOException {
		String regAnswer;
		if(connectedNames.contains(name)) {
			regAnswer = "Name is already used";
		}
		else {
			regAnswer = "Success";
			connectedNames.add(name);
			System.out.println(name + " entered the chat!");
		}
		
		DatagramPacket answer = new DatagramPacket(regAnswer.getBytes(), regAnswer.getBytes().length, address, port);
		socket.send(answer);
	}
	
	private static void LogoutUser(String name) {
		if(connectedNames.contains(name)) {
			connectedNames.remove(name);
			System.out.println(name + " left the chat...");
		}
	}

}
