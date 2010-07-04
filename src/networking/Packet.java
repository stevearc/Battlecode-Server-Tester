package networking;

import java.io.Serializable;

/**
 * This class is what the Client/Server interact with, so they have
 * a single class that contains match data and metadata.
 * @author steven
 *
 */
public class Packet implements Serializable {
	private static final long serialVersionUID = -5973999222662430037L;
	/** The raw data of the match rms file */
	public byte[] match;
	/** Name of map to run match on */
	public String map;
	/** Name of revision to use for Team A */
	public String team_a;
	/** Name of revision to use for Team B */
	public String team_b;
	/** Number of bytes in the match data file */
	public int matchLength;
	/** Type of packet */
	public PacketType type;
	/** Whether or not Team A won the match */
	public boolean a_wins;
	
	public Packet() {}

	public Packet(PacketType type, String map, String team_a, String team_b, 
			int matchLength, boolean a_wins, 
			byte[] match) {
		this.map = map;
		this.team_a = team_a;
		this.team_b = team_b;
		this.matchLength = matchLength;
		this.type = type;
		this.a_wins = a_wins;
		this.match = match;
	}

	public Packet (Packet packet) {
		this (packet.type, packet.map, packet.team_a, packet.team_b, 
				packet.matchLength, packet.a_wins, packet.match);
	}
	
	/**
	 * Check to see if the data in this packet agrees with itself
	 * @return True if the packet is valid.  False otherwise.
	 */
	public boolean validate() {
		if (match == null) {
			if (matchLength != 0)
				return false;
		}
		else if (matchLength != match.length)
			return false;
		switch (type) {
		case SEND_MAP:
			if (matchLength != 0)
				return false;
		case MAP_RESULT:
			if (map != null && team_a != null && team_b != null)
				return true;					
			break;
		case REQUEST_MAP:
			if (map == null && team_a == null && team_b == null &&
					matchLength == 0)
				return true;
			break;
		case RESET_RUNS:
			if (map == null && team_a == null && team_b == null &&
					matchLength == 0)
				return true;
			break;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type + "\n");
		sb.append("map: " + map + "\n");
		sb.append(team_a + " vs " + team_b + "\n");
		sb.append("match length: " + matchLength);
		sb.append("\nmatch: ");
		if (match == null)
			sb.append("null");
		else 
			sb.append("not null, len: " + match.length);
		return sb.toString();
	}
}
