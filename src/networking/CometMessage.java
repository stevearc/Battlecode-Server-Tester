package networking;

public class CometMessage {
	private CometCmd cmd;
	public int id;
	private Object[] args;
	
	
	public CometMessage(CometCmd cmd, Object[] args) {
		this.cmd = cmd;
		this.args = args;
	}
	
	public CometCmd getCmd() {
		return cmd;
	}
	
	public Object get(int index) {
		return args[index];
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(cmd);
		sb.append(",");
		sb.append(id);
		sb.append(",");
		for (Object o: args) {
			sb.append(o);
			sb.append(",");
		}
		return sb.toString();
	}
}
