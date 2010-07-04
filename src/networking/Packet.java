package networking;

/**
 * This class is what the Client/Server interact with, so they have
 * a single class that contains match data and metadata.
 * @author steven
 *
 */
public class Packet extends NetworkPacket{
	/** The raw data of the match rms file */
	public byte[] match;

	public Packet(PacketType type, String map, String team_a, String team_b, 
			int matchLength, boolean a_wins, 
			byte[] match) {
		super(map, team_a, team_b, matchLength, type, a_wins);
		this.match = match;
	}

	public Packet (Packet packet) {
		super (packet.map, packet.team_a, packet.team_b, 
				packet.matchLength, packet.type, packet.a_wins);
		this.match = packet.match;
	}

	public Packet (NetworkPacket packet, byte[] match) {
		super (packet.map, packet.team_a, packet.team_b, 
				packet.matchLength, packet.type, packet.a_wins);
		this.match = match;
	}

	public boolean validate() {
		if (match == null) {
			if (matchLength != 0)
				return false;
		}
		else if (matchLength != match.length)
			return false;
		return super.validate();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nmatch: ");
		if (match == null)
			sb.append("null");
		else 
			sb.append("not null, len: " + match.length);
		return sb.toString();
	}
}
