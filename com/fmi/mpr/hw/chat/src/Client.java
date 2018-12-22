import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {
	
	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 6565;
	final static int MAX_LENGTH = 1024;
	
	public static void main(String[] args) throws IOException {
		InetAddress address = InetAddress.getByName(INET_ADDR);
		
		byte[] buff = new byte[MAX_LENGTH];
		try(MulticastSocket clientSocket = new MulticastSocket(PORT)) {
			clientSocket.joinGroup(address);
			
			System.out.println("Client is waiting for messages:");
			
			while(true) {
				DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
				clientSocket.receive(receivedPacket);
				
				String receivedMsg = new String(buff, 0, buff.length);
				System.out.println("Client received: " + receivedMsg);
			}
		}
	}
}
