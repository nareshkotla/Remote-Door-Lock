
public class RaspberryPiConnection {
	
	public static void main(String[] args) {
              
        RaspberryPiClient raspberryPiClient = new RaspberryPiClient("iot.naresh.com",4000);

        raspberryPiClient.start();
//        ControlGpioOnPi controlGpioOnPi = new ControlGpioOnPi();
//        controlGpioOnPi.LockDoor();
//        controlGpioOnPi.unLockDoor();
    }

}
