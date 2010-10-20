package networking;

/**
 * Interface for objects that use Networks
 * @author steven
 *
 */
public interface Controller {

	/**
	 * Add a packet to the queue of waiting packets.  Used by attached networks.
	 * @param network The Network that received this packet
	 * @param p The packet that was received
	 */
	public void addPacket(Packet p);
	
	public void onDisconnect();
	
}
