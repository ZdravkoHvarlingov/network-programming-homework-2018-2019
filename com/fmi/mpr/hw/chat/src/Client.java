import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements Runnable {

	final static String MULTICAST_INET_ADDR = "224.0.0.3";
	final static int MULTICAST_PORT = 6565;
	
	final static String SERVER_INET_ADDR = "localhost";
	final static int SERVER_PORT = 6566;
	
	final static int MAX_LENGTH = 1024;
	static Boolean KEEP_READING = true;
	
	static String name = null;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket sendSocket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(SERVER_INET_ADDR);
		
		Scanner scanner = new Scanner(System.in);
		HandleNameRegistration(scanner, sendSocket, address);
		
		Thread readMessages = new Thread(new Client());
		readMessages.start();
		
		String message;
		message = scanner.nextLine();
		
		while(!message.equals("exit"))
		{
			message = name + ": " + message;
			DatagramPacket packetToSend = new DatagramPacket(message.getBytes(), message.getBytes().length, address, SERVER_PORT);
			sendSocket.send(packetToSend);
			
			message = scanner.nextLine();
		}
		
		KEEP_READING = false;
		HandleLogout(sendSocket, address);
		readMessages.join();
		scanner.close();
		sendSocket.close();
	}
	
	public static void HandleNameRegistration(Scanner scanner, DatagramSocket sendSocket, InetAddress address) throws IOException {
		
		System.out.println("Enter chat name(only letters and digits): ");
		name = scanner.nextLine();
		name = "#" + name; 
		
		DatagramPacket regPacket = new DatagramPacket(name.getBytes(), name.getBytes().length, address, SERVER_PORT);
		sendSocket.send(regPacket);
		
		byte[] buff = new byte[MAX_LENGTH];
		DatagramPacket regAnswer = new DatagramPacket(buff, buff.length);
		sendSocket.receive(regAnswer);
		String regAnswerToString = new String(regAnswer.getData(), regAnswer.getOffset(), regAnswer.getLength());

		while(!regAnswerToString.equals("Success")) {
			System.out.println("Chat name is already used, enter another one: ");
			name = scanner.nextLine();
			name = "#" + name; 
			
			regPacket = new DatagramPacket(name.getBytes(), name.getBytes().length, address, SERVER_PORT);
			sendSocket.send(regPacket);
			regAnswer = new DatagramPacket(buff, buff.length);
			sendSocket.receive(regAnswer);
			regAnswerToString = new String(regAnswer.getData(), regAnswer.getOffset(), regAnswer.getLength());
		}
		
		System.out.println("Chat entered successfuly!");
		name = name.substring(1);
	}
	
	private static void HandleLogout(DatagramSocket sendSocket, InetAddress address) throws IOException {
		
		String msg = "?" + name;
		DatagramPacket logoutPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, SERVER_PORT);
		sendSocket.send(logoutPacket);
	}
	
	@Override
	public void run() {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(MULTICAST_INET_ADDR);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] buff = new byte[MAX_LENGTH];
		try(MulticastSocket clientSocket = new MulticastSocket(MULTICAST_PORT)) {
			clientSocket.joinGroup(address);
			
			while(KEEP_READING) {
				DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
				clientSocket.receive(receivedPacket);
				
				String receivedMsg = new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
				if(!receivedMsg.split(":")[0].equals(name)) {

					System.out.println(receivedMsg);	
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
