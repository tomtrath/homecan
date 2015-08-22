package homecan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.openhab.binding.homecan.HomecanMsg;

public class HomecanControl {
	
	
	public final static int HOMECAN_UDP_PORT = 15000;
	
	public DatagramSocket socketApp;

	public HomecanControl() {				
		try {			
			socketApp = new DatagramSocket(HOMECAN_UDP_PORT);			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}		
	}
	
	private void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void enterBootloader(Device device) throws IOException {
		System.out.println("Entering Bootloader on "+device.toString());	
		wait(100);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.CALL_BOOTLOADER, (byte)0, null);		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));				
	}
	
		
	public void sendConfigForChannel(Device device, byte channel, ChannelConfig config) throws IOException {
		System.out.println("Sending to "+device.toString()+" channel: "+channel+" "+config.toString());		
		wait(1000);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.CHANNEL_CONFIG, channel, config.getPayload());		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));
	}
	
	public void clearConfig(Device device) throws IOException {
		System.out.println("Clearing config on "+device.toString());		
		wait(2000);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.CLEAR_CONFIG, (byte)0, null);		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));						
	}

	public void storeConfig(Device device) throws IOException {
		System.out.println("Storeing config on "+device.toString());
		wait(2000);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.STORE_CONFIG, (byte)0, null);		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));						
	}

	public ChannelConfig requestConfigForChannel(Device device, byte channel) throws IOException {
		System.out.println("Requesting channelconfig of "+device.toString()+" channel: "+channel);
		wait(500);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.GET_CONFIG, channel, null);		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));
		
		ChannelConfig config;
		byte[] buffer = new byte[32];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);		
		socketApp.receive(packet);
		msg = new HomecanMsg(packet);
		config = new ChannelConfig(msg.getData());
		System.out.println("received "+config.toString());
		return config;
	}
	
	public void sendDimmerLearn(Device device, byte channel) throws IOException {
		System.out.println("Sending DimmerLearn to "+device.toString()+" channel: "+channel);
		wait(100);
		HomecanMsg msg = new HomecanMsg(device.getAddress(), HomecanMsg.MsgType.DIMMER_LEARN, channel, null);		
		socketApp.send(msg.getPacket(device.getIP(),HOMECAN_UDP_PORT));
	}	
}