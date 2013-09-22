package homecan;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;


public class BootloaderMsg {

	public static final int MSGTYPE_IDENTIFY = 1;
	public static final int MSGTYPE_SET_ADDRESS = 2;
	public static final int MSGTYPE_DATA = 3;
	public static final int MSGTYPE_START_APP = 4;
	public static final int MSGTYPE_CHIP_ERASE = 5;
	public static final int MSGTYPE_CHANGE_ID = 6;
	
	public static final int MSGTYPE_REQUEST = 0x00;	
	public static final int MSGTYPE_SUCCESSFULL_RESPONSE = 0x10;		
	public static final int MSGTYPE_WRONG_NUMBER_REPSONSE = 0x20;
	public static final int MSGTYPE_ERROR_RESPONSE = 0x30;	
	
	public static final int DATA_PER_MSG = 4;
	
	public static final int START_OF_MSG_MASK = 0x80;
	public static final int REQRESPONSE_MASK = 0x30;
	
	protected int msgtype;	
	protected byte address;
	protected int msgNumber;
	

	protected Integer dataCounter = null;
	protected byte[] data = null;	

	public BootloaderMsg(byte address,int msgType) {
		this.address = address;
		this.msgtype = msgType;
		msgNumber = 0;
		dataCounter = 0;
		data = new byte[4];
	}
	
	public BootloaderMsg(DatagramPacket packet) {
		byte[] buffer = packet.getData();
		this.address = buffer[0];
		msgtype = new Integer(buffer[1]);
		msgNumber = new Integer(buffer[2]);;
		dataCounter = new Integer(buffer[3]);
		int datalen = packet.getLength()-4;
		data = new byte[datalen];
		for (int i=0;i<datalen;i++) {	
			data[i] = buffer[i+4];
		}
	}
	
	public void setDataCounter(Integer dc) {
		dataCounter = dc;
	}
		
	public DatagramPacket getPacket(InetAddress ip, int port) {
		byte[] buffer = new byte[data.length+4];
		buffer[0] = address;
		buffer[1] = (byte)msgtype;
		buffer[2] = (byte)msgNumber;
		buffer[3] = dataCounter.byteValue();
		for (int i=0;i<data.length;i++) {	
			buffer[i+4] = data[i];
		}
        return new DatagramPacket(buffer, buffer.length,ip,port);
	}	
	
	public byte getType() {
		return (byte)msgtype;
	}

	public byte getAddress() {
		return address;
	}

	public Integer getMsgNumber() {
		return msgNumber;
	}

	public Integer getDataCounter() {
		return dataCounter;
	}

	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public void setMsgNumber(int msgNumber) {
		this.msgNumber = msgNumber;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BootloaderMsg [msgtype=" + msgtype + ", address=" + address + ", msgNumber=" + msgNumber + ", dataCounter=" + dataCounter + ", data="
				+ Arrays.toString(data) + "]";
	}
}
