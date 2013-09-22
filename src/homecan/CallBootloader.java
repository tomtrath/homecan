package homecan;

import java.io.IOException;
import java.net.UnknownHostException;


public class CallBootloader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		if (args.length!=2) {
			System.out.println("usage: callBootloader <IP-Address> <ID>");
			System.exit(0);
		} 		
		try {
			Device device = new Device(args[0],args[1]);
			HomecanControl control = new HomecanControl();
			control.enterBootloader(device);			
			BootloaderControl bootloadercontrol = new BootloaderControl();
			bootloadercontrol.justCallBootloader(device);
		} catch (UnknownHostException e) {
			System.out.println("invalid IP-Adress!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
