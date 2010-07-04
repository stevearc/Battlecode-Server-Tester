package networking;


/**
 * Wrapper for packet that also contains the network that the packet was
 * received on.  Used for identifying the exact client that sent it.
 * @author steven
 *
 */
public class Message extends Packet{
	/** The Network that received this packet */
	public Network receivedBy;
	
	public Message(Network receivedBy, PacketType type, String map, String team_a, String team_b, 
			int matchLength, boolean a_wins, byte[] match) {
		super (type, map, team_a, team_b, matchLength, a_wins, match);
		this.receivedBy = receivedBy;
	}
	
	public Message(Packet p, Network receivedBy) {
		super(p);
		this.receivedBy = receivedBy;
	}

}
