package backend;

import java.util.HashMap;
import java.util.HashSet;

import networking.CometMessage;

import org.eclipse.jetty.continuation.Continuation;

public class WebPollHandler {
	private static final int buffersize = 20;
	private HashMap<String, HashSet<Continuation>> channels = new HashMap<String, HashSet<Continuation>>();
	private HashMap<String, CometMessage[]> buffer = new HashMap<String, CometMessage[]>();
	private HashMap<String, Integer> bufferIndex = new HashMap<String, Integer>();

	public synchronized void addContinuation(String channel, Continuation c, int lastheard) {
		if (!channels.containsKey(channel)) {
			channels.put(channel, new HashSet<Continuation>());
			buffer.put(channel, new CometMessage[buffersize]);
			bufferIndex.put(channel, 0);
		}
		int index = bufferIndex.get(channel);
		int nextid = (lastheard+1)%buffersize;
		// If this client missed a broadcast, send it what it missed
		if (nextid != index && lastheard != -1) {
			sendMsg(c, buffer.get(channel)[nextid]);
		} else {
			channels.get(channel).add(c);
		}
	}

	public synchronized void broadcastMsg(String channel, CometMessage msg) {
		if (!channels.containsKey(channel)) {
			channels.put(channel, new HashSet<Continuation>());
			buffer.put(channel, new CometMessage[buffersize]);
			bufferIndex.put(channel, 0);
		}
		int index = bufferIndex.get(channel);
		msg.id = index;
		for (Continuation c: channels.get(channel)) {
			sendMsg(c, msg);
		}
		buffer.get(channel)[index] = msg;
		bufferIndex.put(channel, (index+1)%buffersize);
		channels.get(channel).clear();
	}

	private void sendMsg(Continuation c, CometMessage msg) {
		if (c.isSuspended()) {
			c.setAttribute("response", msg);
			c.resume();
		}
	}
}

