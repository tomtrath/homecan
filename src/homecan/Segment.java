package homecan;
import java.util.Iterator;
import java.util.Vector;


public class Segment {

	protected short address;
	protected Vector<Byte> data;
	
	public Segment(short startAddress) {
		address = startAddress;
		data = new Vector<Byte>();
	}

	public short getAddress() {
		return address;
	}

	public void append(byte[] buffer) {
		for (int i=0;i<buffer.length;i++)  {
			data.addElement(new Byte(buffer[i]));
		}		
	}

	public void append(byte value) {
		data.addElement(new Byte(value));			
	}
	
	public short getSize() {
		return (short)data.size();
	}	

	public int getFirstPage(int pagesize) {
		return address/pagesize;
	}

	public int getLastPage(int pagesize) {
		return (address+data.size())/pagesize;
	}

	public byte[] getPage(int page, int pagesize) {
		byte[] pageBuffer = new byte[pagesize];
		int j;

		if (page<getFirstPage(pagesize) || page> getLastPage(pagesize)) throw new IndexOutOfBoundsException("page not within segment");

		int startIdx = page*pagesize-address;
		Iterator<Byte> i = data.listIterator(startIdx);		
		for (j=0;i.hasNext() && j<pagesize;j++) {
			pageBuffer[j] = i.next();
		}		
		return pageBuffer;
	}

	public int getPageOffset(int page, int pagesize) {
		if (page<getFirstPage(pagesize) || page> getLastPage(pagesize)) throw new IndexOutOfBoundsException("page not within segment");
		if (page==getFirstPage(pagesize)) {
			return address%pagesize;
		} else {						
			return 0;
		}
	}
	
}
