package networking;

/**
 * Interface for objects that use Networks
 * @author stevearc
 *
 */
public interface Controller {

	/**
	 * Add a packet to the queue of waiting packets.  Used as a callback attached networks.
	 * @param network The Network that received this packet
	 * @param p The packet that was received
	 */
	public void addPacket(Packet p);
	
	/**
	 * Called when the connection fails
	 */
	public void onDisconnect();
	
}
