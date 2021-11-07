package tigase.shiku;

import tigase.server.AbstractMessageReceiver;
import tigase.server.Packet;

/**
 * @author lidaye
 *
 */
public class ShikuAckComponent extends AbstractMessageReceiver{

	/**
	 * 
	 */
	public ShikuAckComponent() {
		super();
		setName("shiku-ack");
	}
	@Override
	public void processPacket(Packet packet) {
		
		
		
		
	}
	@Override
	public int processingInThreads() {
		// TODO Auto-generated method stub
		return Runtime.getRuntime().availableProcessors();
	}
	@Override
	public int processingOutThreads() {
		// TODO Auto-generated method stub
		return Runtime.getRuntime().availableProcessors();
	}
	@Override
	public String getDiscoDescription() {
		return "ShiKu ACK Support";
	}

}
