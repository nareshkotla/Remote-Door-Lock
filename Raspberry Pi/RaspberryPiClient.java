
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class RaspberryPiClient extends Thread {

	private String serverAddress;
	private int port;
	public Socket socket = null;
	private BufferedReader input;
	public PrintWriter output;
	private ControlGpioOnPi controlGpioOnPi = new ControlGpioOnPi();
	private GpioController gpio = GpioFactory.getInstance();
	// provision gpio pin #01 as an output pin and turn off
	private GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

	public RaspberryPiClient(String serverAddress, int port) {
		super();
		this.serverAddress = serverAddress;
		this.port = port;
	}

	public synchronized void run() {

		System.out.println("RaspberryPiClient Thread " + Thread.currentThread().getName());
		//		Thread timerThread = new Thread();

		MyTimer myTimerTask; 
		Timer myTimer = null;

		try {
			this.socket = new Socket(this.serverAddress, this.port);

			System.out.println("Connection with VMServer established ... " + this.socket);

//			System.out.println("Door locked by default ... ");
			
			this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			while(true) {
				String read = this.input.readLine();

				System.out.println("Message received from VMServer " + read);

				if(read.contains("raspberrypi")) {
					this.output = new PrintWriter(this.socket.getOutputStream(), true);

					if(read.contains("unlock")){
						System.out.println("Command received for unlocking the door ... ");
						/*
						 * Logic for door unlock
						 */
						controlGpioOnPi.unLockDoor(pin);
						this.output.println("raspberrypi" + " " + "unlocked");
						//							timerThread.(10000);
						myTimerTask = new MyTimer();
						myTimer = new Timer();
						myTimer.schedule(myTimerTask, 10000);
						
					} else if(read.contains("lock")) {
						if(myTimer != null)
						{
							myTimer.cancel();
						}
						System.out.println("Command received for locking the door ... ");
						/*
						 * Logic for door lock
						 */
						controlGpioOnPi.LockDoor(pin);
						this.output.println("raspberrypi" + " " + "locked");
					} else {
						System.out.println("Command unknown ... ");
						this.output.println("Unknown Command");
					}
				}
			}

		} catch (UnknownHostException e) {
			System.out.println(" Unknown hostname exception : " + e);
		} catch (IOException e) {
			System.out.println("Error connecting to server " + this.serverAddress + " : " + e);
		} finally {
			try {
				this.socket.close();
			} catch (IOException e) {
				System.out.println("Error closing the connection with server " + this.serverAddress + " : " + e);
			}
		}


	}

	public class MyTimer extends TimerTask{
		@Override
		public void run()
		{
			if(pin.isHigh()) {
				controlGpioOnPi.LockDoor(pin);
				output.println("raspberrypi" + " " + "locked" + " force");
				System.out.println("~~~~~~~~~~~~~~~~~~~~forced by pi due to time out~~~~~~~~~~~~~~~~~");
			}
		}
	}

} 


