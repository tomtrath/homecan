package homecan;

import java.io.IOException;
import java.net.UnknownHostException;


public class ChangeID {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		if (args.length!=3) {
			System.out.println("usage: changeID <IP-Address> <oldID> <newID>");
			System.exit(0);
		} 		
		try {
			Device device = new Device(args[0],args[1]);
			HomecanControl control = new HomecanControl();
			control.enterBootloader(device);			
			BootloaderControl bootloadercontrol = new BootloaderControl();
			bootloadercontrol.changeID(device,new Integer(args[2]));
		} catch (UnknownHostException e) {
			System.out.println("invalid IP-Adress!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
