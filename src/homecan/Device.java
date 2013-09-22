package homecan;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Device {
	private InetAddress ip;
	private Integer address;

	public Device(String ip, String address) throws UnknownHostException {
		this.ip = InetAddress.getByName(ip);
		this.address = Integer.decode(address);
	}

	public InetAddress getIP() {
		return ip;
	}

	public byte getAddress() {
		return address.byteValue();
	}

	@Override
	public String toString() {
		return "Device [ip=" + ip + ", address=" + address + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Device dev = (Device) obj;
		return dev.address==address && ip.equals(dev.ip);
	}
	
	
}
