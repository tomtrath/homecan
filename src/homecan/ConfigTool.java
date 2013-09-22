package homecan;

import homecan.ChannelConfig.Function;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigTool {
	
	NodeList devices;
	String hexPath;
	String cmd;
	String[] options;
	HomecanControl control;
	BootloaderControl bootloadercontrol;
	
	public ConfigTool(String[] args) throws SAXException, IOException, ParserConfigurationException {		
		control = new HomecanControl();
		bootloadercontrol = new BootloaderControl();		
		cmd = args[2];
		options = new String[args.length-3];
		System.arraycopy(args, 3, options, 0, args.length-3);
		ConfigFileParser parser = new ConfigFileParser(args[0],args[1]);		
		devices = parser.getXmlDocument().getElementsByTagName("device");		
	}
	
	private String getTagValue(String sTag, Element eElement) {
		return eElement.getElementsByTagName(sTag).item(0).getChildNodes().item(0).getNodeValue();
	}
		
	public Device getDevice(Element element) throws UnknownHostException, DOMException {				
		return new Device(element.getParentNode().getAttributes().getNamedItem("ip").getNodeValue(),element.getAttributes().getNamedItem("address").getNodeValue());		
	}
	
	private ChannelConfig getChannelConfig(Element elementChannel) {
		String s = elementChannel.getAttribute("function");
		Function function = Function.valueOf(s);		
		byte[] ports;
		switch (ChannelConfig.getPortCount(function)) {			
			case 1:
				ports = new byte[1];
				ports[0] = Byte.parseByte(elementChannel.getAttribute("portA"));
				break;
			case 2:
				ports = new byte[2];
				ports[0] = Byte.parseByte(elementChannel.getAttribute("portA"));
				ports[1] = Byte.parseByte(elementChannel.getAttribute("portB"));
				break;
			default:
				ports = null;
		}		
		Vector<String> params = new Vector<String>();
		NodeList paramList = elementChannel.getElementsByTagName("parameter");
		for (int param = 0; param < paramList.getLength(); param++) {
			Node nNode = paramList.item(param);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nNode;
				params.add(element.getChildNodes().item(0).getNodeValue());				
			}
		}
		return new ChannelConfig(function,ports,params);
	}
	
	private void flashDevice(Device selectedDevice, String hardware) throws IOException {
		for (int dev = 0; dev < devices.getLength(); dev++) {
			Node nNode = devices.item(dev);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nNode;
				if (getTagValue("bootloader", element).equalsIgnoreCase("true")) {
					if (hardware==null?true:getTagValue("hardware", element).equals(hardware)) {
						Device device = getDevice(element);
						if (selectedDevice == null || selectedDevice.equals(device)) {	
							control.enterBootloader(device);
							bootloadercontrol.flash(device,hexPath + "/" + getTagValue("firmware", element));
						}
					}
				}
			}
		}
	}

	private void configureChannels(Device selectedDevice,String hardware) throws UnknownHostException, IOException {
		for (int dev = 0; dev < devices.getLength(); dev++) {
			Node nNode = devices.item(dev);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nNode;
				if (hardware==null?true:getTagValue("hardware", element).equals(hardware)) {
					Device device = getDevice(element);
					if (selectedDevice == null || selectedDevice.equals(device)) {
						control.clearConfig(device);
						NodeList channels = element.getElementsByTagName("channel");
						for (int ch = 0; ch < channels.getLength(); ch++) {
							Node nNodeChannel = channels.item(ch);
							if (nNodeChannel.getNodeType() == Node.ELEMENT_NODE) {
								Element elementChannel = (Element) nNodeChannel;
								ChannelConfig config = getChannelConfig(elementChannel);
								control.sendConfigForChannel(device, Byte.parseByte(elementChannel.getAttribute("number")), config);
							}
						}
						control.storeConfig(device);
					}
				}
			}
		}
	}
	
	public void execute() throws IOException {
		switch (cmd) {
			case "flash_all":
				hexPath = options[0];
				flashDevice(null,null);				
				break;
			case "flash_devices":
				hexPath = options[0];
				flashDevice(null,options[1]);				
				break;
			case "flash":
				hexPath = options[0];
				flashDevice(new Device(options[1].split(":")[0], options[1].split(":")[1]),null);				
				break;
			case "config_all":
				configureChannels(null,null);
				break;
			case "config_devices":
				configureChannels(null,options[0]);
				break;
			case "config":
				configureChannels(new Device(options[0].split(":")[0], options[0].split(":")[1]),null);				
				break;			
		}
	}	
		
	/**
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String[] args) {
		if (args.length<3) {
			System.out.println("usage: ConfigTool <xsd-file> <xml-file> <cmd> [<ip:address>] [<path-to-hex-files]");
			System.exit(0);
		} 
		
		ConfigTool configTool;
		try {
			configTool = new ConfigTool(args);
			configTool.execute();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
}
