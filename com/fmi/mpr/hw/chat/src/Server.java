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
	final static int MAX_LENGTH = 1024;
	
	static Set<String> connectedNames;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		connectedNames = new TreeSet<String>();
		
		try(DatagramSocket receiveSocket = new DatagramSocket(SERVER_PORT)) {		
			byte[] buff = new byte[MAX_LENGTH];	
			
			while(true) {
				DatagramPacket receivedPacket  = new DatagramPacket(buff, buff.length);
				receiveSocket.receive(receivedPacket);
				
				String msg = new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
				if(msg.startsWith("#")) {
					ProcessNewName(msg.substring(1), receiveSocket, receivedPacket.getAddress(), receivedPacket.getPort());
					msg = msg.substring(1) + " entered the chat...";
				}
				else if(msg.startsWith("?")) {
					LogoutUser(msg.substring(1));
					msg = msg.substring(1) + " left the chat...";
				}
				else if(connectedNames.contains(msg.split(":")[0])) {
					System.out.println(msg);
				}
				
				InetAddress address = InetAddress.getByName(MULTICAST_INET_ADDR);
				DatagramPacket multicastPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, MULTICAST_PORT);
				receiveSocket.send(multicastPacket);
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
