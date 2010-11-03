package backend;

import java.util.HashMap;
import java.util.HashSet;

import networking.CometMessage;

import org.eclipse.jetty.continuation.Continuation;

public class WebPollHandler {
	private HashMap<String, HashSet<Continuation>> channels = new HashMap<String, HashSet<Continuation>>();

	public synchronized void addContinuation(String channel, Continuation c) {
		if (!channels.containsKey(channel)) {
			channels.put(channel, new HashSet<Continuation>());
		}
		channels.get(channel).add(c);
	}

	public synchronized void broadcastMsg(String channel, CometMessage o) {
		if (!channels.containsKey(channel)) {
			channels.put(channel, new HashSet<Continuation>());
		}
		for (Continuation c: channels.get(channel)) {
			if (c.isSuspended()) {
				c.setAttribute("response", o);
				c.resume();
			}
		}
		channels.get(channel).clear();
	}

}
