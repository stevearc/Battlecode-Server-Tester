package networking;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wrapper object to send data between worker and server
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Packet [" + cmd + ": ");
		sb.append(Arrays.toString(args));
		sb.append("]");
		return sb.toString();
	}
}
