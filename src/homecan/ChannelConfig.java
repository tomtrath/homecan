package homecan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Vector;

public class ChannelConfig {

	//uint8_t function; uint8_t port0; uint8_t port1; uint8_t parameter0; uint8_t parameter1; ...
	
	
	public enum Function {
		NONE(0),
		INPUT(1),
		OUTPUT(2),
		RAFFSTORE(3),
		SSR(4),
		DIMMER(5),
		FTK(6),
		TEMPSENS(7),
		LED(8),
		BUZZER(9),
		IRTX(10),
		IRRX(11),
		MOTION(12),
		PUSHBUTTON(13),
		HUMIDITY(14),
		LUMINOSITY(15),
		KEYPAD(16),
		KWB_INPUT(17),
		KWB_TEMP(18),
		KWB_HK(19),
		BUSLOAD(20),
		FRW(21),
		ENOCEAN_SNIFFER(22),
		RESERVED(255);
		
		byte value;
		private Function(int val) {
			value = (byte)val;			
		}		
		public byte getValue() {
			return value;
		}				
	} 

	private Function function;
	private byte[] ports;
	private byte[] parameter;
	
	public static int getPortCount(Function function) {
		switch (function) {
			case BUZZER:				
			case DIMMER:
			case FTK:
			case HUMIDITY:
			case INPUT:
			case IRRX:
			case IRTX:
			case KEYPAD:
			case LED:
			case LUMINOSITY:
			case MOTION:
			case OUTPUT:
			case PUSHBUTTON:
			case TEMPSENS:
			case KWB_INPUT:
			case KWB_TEMP:
			case KWB_HK:
			case BUSLOAD:
			case FRW:
			case ENOCEAN_SNIFFER:
				return 1;
			case RAFFSTORE:				
			case SSR:
				return 2;				
			case NONE:
			case RESERVED:
				return 0;					
		}
		return 0;
	}
	
	public ChannelConfig(byte[] buffer) {
		String s = Byte.toString(buffer[0]);
		function = Function.valueOf(s);
		switch (getPortCount(function)) {
			case 0:
				ports = null;
				break;
			case 1:
				ports = new byte[1];
				ports[0] = buffer[1];
				break;			
			case 2:
				ports = new byte[2];
				ports[0] = buffer[1];
				ports[1] = buffer[2];
				break;
		}
		parameter = new byte[buffer.length-3];
		System.arraycopy(buffer,3,parameter,0,parameter.length);
	}	
	
	public ChannelConfig(Function function,byte[] ports, Vector<String> parameters) {
		this.function = function;
		this.ports = ports;
		parameter = new byte[parameters.size()];
		for (int i=0;i<parameter.length;i++) {
			try {
				parameter[i] = Integer.decode(parameters.elementAt(i)).byteValue();
			} catch (NumberFormatException nfe) {				
				ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
				buf.putFloat(Float.parseFloat(parameters.elementAt(i)));				
				byte[] temp = new byte[i+4];
				System.arraycopy(parameter, 0, temp, 0, i);
				System.arraycopy(buf.array(), 0, temp, i,buf.array().length);
				parameter = temp;
				i+=3;
			}
		}		
	}
	
	public byte[] getPayload() {
		int portCount = ports != null ? ports.length : 0;
		byte[] payload = new byte[1 + (parameter.length>0?2:portCount) + parameter.length];
		payload[0] = function.getValue();
		if (portCount>0)
			System.arraycopy(ports,0,payload,1,portCount);
		System.arraycopy(parameter,0,payload,1+(parameter.length>0?2:portCount),parameter.length);	
		return payload;
	}
	
	@Override
	public String toString() {
		return "Config [function=" + function + ", port=" + Arrays.toString(ports) + ", parameter=" + Arrays.toString(parameter) + "]";
	}
}
