import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
	
	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 6565;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		InetAddress address = InetAddress.getByName(INET_ADDR);
		
		try(DatagramSocket serverSocket = new DatagramSocket()) {
			System.out.println("Server started!");
			
			for (int i = 0; i < 5; i++) {
				String newMsg = "Hahaha, new message N:" + i;
				
				DatagramPacket sendPacket = new DatagramPacket(newMsg.getBytes(), newMsg.getBytes().length, address, PORT);
				serverSocket.send(sendPacket);
				
				System.out.println("Message sent: " + newMsg);
				Thread.sleep(500);
			}
		}
	}

}
