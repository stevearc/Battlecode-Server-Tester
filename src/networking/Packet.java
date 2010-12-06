package networking;

import java.io.Serializable;

/**
 * Wrapper object to send data between client and server
 * @author stevearc
 *
 */
public class Packet implements Serializable {
	private static final long serialVersionUID = -5973999222662430037L;
	private PacketCmd cmd;
	private Object[] args;
	
	
	public Packet(PacketCmd cmd, Object[] args) {
		this.cmd = cmd;
		this.args = args;
	}
	
	public PacketCmd getCmd() {
		return cmd;
	}
	
	public Object get(int index) {
		return args[index];
	}
}
