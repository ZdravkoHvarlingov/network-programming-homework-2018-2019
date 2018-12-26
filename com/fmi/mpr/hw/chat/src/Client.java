import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Client implements Runnable {

	final static String MULTICAST_INET_ADDR = "224.0.0.3";
	final static int MULTICAST_PORT = 6565;
	
	final static String SERVER_INET_ADDR = "localhost";
	final static int SERVER_PORT = 6566;
	
	final static int MAX_LENGTH = 50000;
	static Boolean KEEP_READING = true;
	
	static String userName = null;
	
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
			if(message.startsWith("SND")) {
				String url = message.split(" ")[1];
				String[] split = url.split("\\\\");
				String fileName = split[split.length - 1];
				byte[] fileToByteArray = null;
				try {
					fileToByteArray = FileUtils.FileToByteArray(url);
				} catch (IOException e) {
					System.out.println("Invalid file, try again");
					
					message = scanner.nextLine();
					continue;
				}
				
				message = userName + ": I am sending the file " + fileName;
				DatagramPacket packetToSend = new DatagramPacket(message.getBytes(), message.getBytes().length, address, SERVER_PORT);
				sendSocket.send(packetToSend);
				
				message = userName + ":" + "SND " + fileName + ":";
				byte[] finalBytes = Arrays.copyOf(message.getBytes(), message.getBytes().length + fileToByteArray.length);
				System.arraycopy(fileToByteArray, 0, finalBytes, message.getBytes().length, fileToByteArray.length);
				packetToSend = new DatagramPacket(finalBytes, finalBytes.length, address, SERVER_PORT);
				sendSocket.send(packetToSend);
			}
			else {
				message = userName + ": " + message;
				DatagramPacket packetToSend = new DatagramPacket(message.getBytes(), message.getBytes().length, address, SERVER_PORT);
				sendSocket.send(packetToSend);
			}
			
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
		userName = scanner.nextLine();
		userName = "#" + userName; 
		
		DatagramPacket regPacket = new DatagramPacket(userName.getBytes(), userName.getBytes().length, address, SERVER_PORT);
		sendSocket.send(regPacket);
		
		byte[] buff = new byte[MAX_LENGTH];
		DatagramPacket regAnswer = new DatagramPacket(buff, buff.length);
		sendSocket.receive(regAnswer);
		String regAnswerToString = new String(regAnswer.getData(), regAnswer.getOffset(), regAnswer.getLength());

		while(!regAnswerToString.equals("Success")) {
			System.out.println("Chat name is already used, enter another one: ");
			userName = scanner.nextLine();
			userName = "#" + userName; 
			
			regPacket = new DatagramPacket(userName.getBytes(), userName.getBytes().length, address, SERVER_PORT);
			sendSocket.send(regPacket);
			regAnswer = new DatagramPacket(buff, buff.length);
			sendSocket.receive(regAnswer);
			regAnswerToString = new String(regAnswer.getData(), regAnswer.getOffset(), regAnswer.getLength());
		}

		userName = userName.substring(1);
		System.out.println("Chat entered successfuly!");
	}
	
	private static void HandleLogout(DatagramSocket sendSocket, InetAddress address) throws IOException {
		
		String msg = "?" + userName;
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
		
		try(MulticastSocket clientSocket = new MulticastSocket(MULTICAST_PORT)) {
			clientSocket.joinGroup(address);
			
			while(KEEP_READING) {
				
				byte[] buff = new byte[MAX_LENGTH];
				DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
				clientSocket.receive(receivedPacket);
				
				String receivedMsg = new String(receivedPacket.getData());
				if(!receivedMsg.split(":")[0].equals(userName)) {
					
					if(receivedMsg.split(":").length > 2 && receivedMsg.split(":")[1].startsWith("SND")) {
						
						int offsetLength = receivedMsg.split(":")[0].length() + receivedMsg.split(":")[1].length() + 2;
						byte[] fileBytes = Arrays.copyOfRange(receivedPacket.getData(), offsetLength, receivedPacket.getLength() - 1);
						String fileName = receivedMsg.split(":")[1].split(" ")[1];
						FileUtils.ByteArrayToFile("Downloads" + File.separator + userName, fileBytes, fileName);
					}
					else {
						System.out.println(receivedMsg);	
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
