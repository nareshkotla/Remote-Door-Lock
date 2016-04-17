import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class VMServer extends Thread {

	private int port;
	private Socket clientsocket = null;
	
	
	public VMServer(int port) {
		super();
		this.port = port;
	}
	

	public synchronized void run() {

		try {
			ServerSocket socket = new ServerSocket(this.port);
			System.out.println("VMServer activated on port "+this.port);			
			while(true) {
				
				this.clientsocket = socket.accept();

				if(this.port == 3001) {
					VMConnection.androidClient = this.clientsocket;
					System.out.println("Connection established with the Android Client " + this.clientsocket.getInetAddress());
				}
				else if(this.port == 4000) {
					VMConnection.raspberryPiClient = this.clientsocket;
					System.out.println("Connection established with the Raspberry Pi Client " + this.clientsocket.getInetAddress());
				}
				
				VMReceiverThread vMReceiverThread = new VMReceiverThread(this.clientsocket);
				vMReceiverThread.start();
			}
			
		} catch (IOException e) {
			try {
				System.out.println("Error activating VM server " + InetAddress.getLocalHost().getHostName() + " with port# " + this.port + " : " + e);
			} catch (UnknownHostException e1) {
				System.out.println(" Unknown hostname exception : " + e);
			}
		}
	}

}
