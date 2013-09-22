package homecan;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.plaf.SliderUI;

public class BootloaderControl {
	
	public final static int BOOTLOADER_VERSION = 0x02;
	
	public final static int HOMECAN_UDP_PORT = 15000;
	public final static int HOMECAN_UDP_PORT_BOOTLOADER = 15001;
	
	public DatagramSocket socketBootloader;
	public Integer pagesize;
	public Integer pages;
	public int msgNumber;

	public BootloaderControl() {		
		this.msgNumber = 0;		
		try {			
			socketBootloader = new DatagramSocket(HOMECAN_UDP_PORT_BOOTLOADER);
			socketBootloader.setSoTimeout(3000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}		
	}
	
	private void updateMsgNumber(BootloaderMsg msg) {
		msg.setMsgNumber(msgNumber++);		
		if (msgNumber>=256) msgNumber = 0;		
	}
	
	private void resetMsgNumber() {
		msgNumber = 0;		
	}
	
	/*
	private void clearRx(DatagramSocket socket,String ip)   {
		byte[] buffer = new byte[4096];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			packet.setAddress(InetAddress.getByName(ip));
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		packet.setPort(HOMECAN_UDP_PORT_BOOTLOADER);
		int timeout = 0;
		try {
			timeout = socket.getSoTimeout();
			socket.setSoTimeout(100);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while (true) {
				socket.receive(packet);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			socket.setSoTimeout(timeout);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	private void identifyDevice(Device device) throws IOException {
		BootloaderMsg msg = new BootloaderMsg(device.getAddress(),BootloaderMsg.MSGTYPE_REQUEST|BootloaderMsg.MSGTYPE_IDENTIFY);				
		resetMsgNumber();
		System.out.println("identifying device ...");
		updateMsgNumber(msg);
		DatagramPacket p =  msg.getPacket(device.getIP(),HOMECAN_UDP_PORT_BOOTLOADER);		
		socketBootloader.send(p);				
		byte[] buffer = new byte[32];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		wait(1);
		socketBootloader.receive(packet);
		msg = new BootloaderMsg(packet);
		byte[] data = msg.getData();		
		switch (data[1]) {
			case 0:pagesize = 32; break;
			case 1:pagesize = 64; break;
			case 2:pagesize = 128; break;
			case 3:pagesize = 256; break;
		}
		pages = data[2]*256+data[3];		
		System.out.println("Device identified: Bootloader version "+data[0]+" pagesize="+pagesize+" pages="+pages);
		if (data[0]!=BOOTLOADER_VERSION) {
			System.out.println("Incompatible BOOTLOADER version detected!!!");
			System.exit(0);
		}
	}

	private boolean flashPage(Device device,int page, byte[] data) throws IOException {
		//128 words per page, 512 pages (davon 480 RWW und 32 NRWW)
		//System.out.print("transmitting page "+page+" ... ");
		for (int i=0;i<pagesize;i+=BootloaderMsg.DATA_PER_MSG) {				
			BootloaderMsg msg = new BootloaderMsg(device.getAddress(),BootloaderMsg.MSGTYPE_REQUEST|BootloaderMsg.MSGTYPE_DATA);
			int batchsize = pagesize; //BootloaderMsg.DATA_PER_MSG; //pagesize;
			if ((i%batchsize)==0) {
				//first msg of page
				msg.setDataCounter(BootloaderMsg.START_OF_MSG_MASK | (batchsize/BootloaderMsg.DATA_PER_MSG-1));
			} else {
				msg.setDataCounter((batchsize-i%batchsize)/BootloaderMsg.DATA_PER_MSG-1);
			}				
			
			msg.setData(Arrays.copyOfRange(data, i, i+BootloaderMsg.DATA_PER_MSG));			
			updateMsgNumber(msg);
			DatagramPacket p =  msg.getPacket(device.getIP(),HOMECAN_UDP_PORT_BOOTLOADER);		
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			socketBootloader.send(p);				
			if ((msg.getDataCounter()&~BootloaderMsg.START_OF_MSG_MASK)==0) {
				//last msg of page, check reply
				byte[] buffer = new byte[8];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				try {
					socketBootloader.receive(packet);
					msg = new BootloaderMsg(packet);
					byte[] replyData = msg.getData();
					//int replyPage =  replyData[0]*256+replyData[1];
					if (msg.getType()==(BootloaderMsg.MSGTYPE_DATA|BootloaderMsg.MSGTYPE_SUCCESSFULL_RESPONSE) && (replyData.length==2?page==(replyData[0]*256+replyData[1]):true)) {
						System.out.print(".");	
					} else if (msg.getType()==(byte)(BootloaderMsg.MSGTYPE_DATA|BootloaderMsg.MSGTYPE_WRONG_NUMBER_REPSONSE)) {					
						System.out.print("R");
						//how many missing, rewind
						int rewindCount = msgNumber - msg.getMsgNumber().byteValue(); // 1,255 
						if (rewindCount<0) rewindCount += 256; else if (rewindCount>255) rewindCount-=256;
						i -= rewindCount*BootloaderMsg.DATA_PER_MSG;
						msgNumber = msg.getMsgNumber().byteValue();
					} else {
						System.out.println("unsuccessfull response");
						System.out.println(msg.toString());
						return false;
					}
				} catch (SocketTimeoutException ste) {
					System.out.print("T");
					int rewindCount = 1;
					i -= rewindCount*BootloaderMsg.DATA_PER_MSG;
					msgNumber = msgNumber>0?msgNumber-1:255;
				}
			}	
			
		}
		return true;
	}

	private boolean flashSegment(Device device,Segment segment) throws IOException {		
		BootloaderMsg msg = new BootloaderMsg(device.getAddress(),BootloaderMsg.MSGTYPE_REQUEST|BootloaderMsg.MSGTYPE_SET_ADDRESS);
		int startPage = segment.getFirstPage(pagesize);
		int endPage = segment.getLastPage(pagesize);
		byte[] data = new byte[4];
		data[0] = (byte)(startPage/256);
		data[1] = (byte)(startPage&0xFF);
		data[2] = 0;	//TODO check if segment starts always on pagestart

		System.out.print("setting segment address ... ");
		msg.setData(data);
		updateMsgNumber(msg);
		DatagramPacket p =  msg.getPacket(device.getIP(),HOMECAN_UDP_PORT_BOOTLOADER);		
		socketBootloader.send(p);	
		byte[] buffer = new byte[8];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		wait(1);
		socketBootloader.receive(packet);
		msg = new BootloaderMsg(packet);
		if (msg.getType()==(BootloaderMsg.MSGTYPE_SET_ADDRESS|BootloaderMsg.MSGTYPE_SUCCESSFULL_RESPONSE)) {
			System.out.println("done");
			System.out.print("flashing ");	
			for (int pg=startPage;pg<=endPage;pg++) {
				flashPage(device,pg,segment.getPage(pg, pagesize));
			}
			System.out.println(" done");
		} else {
			System.out.println("unsuccessfull response");
			System.out.println(msg.toString());
			return false;
		}	
		return true;
	}

	private void startApplication(Device device)
			throws UnknownHostException, IOException {
		//start application
		System.out.print("starting application ... ");
		BootloaderMsg msg = new BootloaderMsg(device.getAddress(),BootloaderMsg.MSGTYPE_REQUEST|BootloaderMsg.MSGTYPE_START_APP);	
		updateMsgNumber(msg);
		DatagramPacket p =  msg.getPacket(device.getIP(),HOMECAN_UDP_PORT_BOOTLOADER);		
		socketBootloader.send(p);	
		byte[] buffer = new byte[8];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socketBootloader.receive(packet);
		msg = new BootloaderMsg(packet);
		if (msg.getType()==(BootloaderMsg.MSGTYPE_START_APP|BootloaderMsg.MSGTYPE_SUCCESSFULL_RESPONSE)) {
			System.out.println("done");		    	
		} else {
			System.out.println("unsuccessfull response");
			System.out.println(msg.toString());
			System.exit(0);
		}
	}
	
	public void flash(Device device,String hexfile) {
		Vector<Segment> segments;		
		try {				
			wait(1);
			identifyDevice(device);

			HexFileParser hexParser = new HexFileParser(new File(hexfile));
			segments = hexParser.parseFile();			
			for (Iterator<Segment> i = segments.iterator();i.hasNext();) {
				Segment seg = i.next();
				if (!flashSegment(device,seg)) 
					if (!flashSegment(device,seg))
						if (!flashSegment(device,seg))				 
							System.out.println("Flashing failed (3 retries)");
			}
			startApplication(device);	
		} catch (FileNotFoundException e) {
			System.out.println("Hexfile "+hexfile+" not found");
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				System.out.println("Response failed: timeout");				
			} else {
				e.printStackTrace();
			}
		}				
	}
	
	public void changeID(Device device,int newDeviceID) {	
		try {			
			wait(2);
			identifyDevice(device);
			BootloaderMsg msg = new BootloaderMsg(device.getAddress(),BootloaderMsg.MSGTYPE_REQUEST|BootloaderMsg.MSGTYPE_CHANGE_ID);		
			byte[] data = new byte[4];
			data[0] = (byte)newDeviceID;					

			System.out.print("changeing device ID ... ");
			msg.setData(data);
			updateMsgNumber(msg);
			DatagramPacket p =  msg.getPacket(device.getIP(),HOMECAN_UDP_PORT_BOOTLOADER);		
			socketBootloader.send(p);	
			byte[] buffer = new byte[8];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socketBootloader.receive(packet);
			msg = new BootloaderMsg(packet);
			if (msg.getType()==(BootloaderMsg.MSGTYPE_CHANGE_ID|BootloaderMsg.MSGTYPE_SUCCESSFULL_RESPONSE)) {
				System.out.println("done ");						
			} else {
				System.out.println("unsuccessfull response");
				System.out.println(msg.toString());
				System.exit(0);
			}
			startApplication(new Device(device.getIP().getHostAddress(),Integer.toString(newDeviceID)));
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				System.out.println("Response failed: timeout");				
			} else {
				e.printStackTrace();
			}
		}	
	}
	
	public void justCallBootloader(Device device) {	
		try {			
			wait(2);
			identifyDevice(device);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startApplication(device);
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				System.out.println("Response failed: timeout");				
			} else {
				e.printStackTrace();
			}
		}	
	}


	private void wait(int s) {
		try {
			Thread.sleep(1000*s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}