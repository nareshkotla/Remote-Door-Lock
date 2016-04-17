import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class VMServerForPi extends Thread {

	private int port;
	private Socket clientsocket = null;


	public VMServerForPi(int port) {
		super();
		this.port = port;
	}

	public synchronized void run() {

		try {
			ServerSocket socket = new ServerSocket(this.port);
			System.out.println("VMServerForPi activated on port "+this.port);
			
			
			while(true) {
				
				this.clientsocket = socket.accept();

				if(this.port == 8500) {
					VMConnection.androidClientForPi = this.clientsocket;
					System.out.println("Connection established with the Android Client for Pi" + this.clientsocket.getInetAddress());
				}
				
			}
			
		} catch (IOException e) {
			try {
				System.out.println("Error activating VM server for Pi" + InetAddress.getLocalHost().getHostName() + " with port# " + this.port + " : " + e);
			} catch (UnknownHostException e1) {
				System.out.println(" Unknown hostname exception : " + e);
			}
		}
	}

}
