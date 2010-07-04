package networking;

/**
 * This is the class that is actually transferred over the socket.
 * Contains all information about the match, but no actual match data.
 * @author steven
 *
 */
class NetworkPacket {
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

	public NetworkPacket(String map, String team_a, String team_b, 
			int matchLength, PacketType type, boolean a_wins) {
		this.map = map;
		this.team_a = team_a;
		this.team_b = team_b;
		this.matchLength = matchLength;
		this.type = type;
		this.a_wins = a_wins;
	}

	public NetworkPacket (NetworkPacket packet) {
		this.map = packet.map;
		this.team_a = packet.team_a;
		this.team_b = packet.team_b;
		this.matchLength = packet.matchLength;
		this.type = packet.type;
		this.a_wins = packet.a_wins;
	}

	public NetworkPacket (Packet packet) {
		this.map = packet.map;
		this.team_a = packet.team_a;
		this.team_b = packet.team_b;
		this.matchLength = packet.matchLength;
		this.type = packet.type;
		this.a_wins = packet.a_wins;
	}

	/**
	 * Check to see if the data in this packet agrees with itself
	 * @return True if the packet is valid.  False otherwise.
	 */
	public boolean validate() {
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
		return sb.toString();
	}
}
