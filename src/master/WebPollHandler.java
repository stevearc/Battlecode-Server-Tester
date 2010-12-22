package master;

import java.util.HashMap;
import java.util.HashSet;

import networking.CometMessage;

import org.eclipse.jetty.continuation.Continuation;

/**
 * Handles communication of Comet events from Web Server to Server
 * There is a set of channels, represented by Strings, that can be subscribed to.
 * When a notable event occurs, the Server will broadcast a message on the
 * appropriate channel.  This will send that message to all clients subscribing to
 * the channel.
 * @author stevearc
 *
 */
public class WebPollHandler {
	// Number of broadcasts to save in recent history
	private static final int buffersize = 20;
	// Mapping of channel to all subscribed web users
	private HashMap<String, HashSet<Continuation>> channels = new HashMap<String, HashSet<Continuation>>();
	// Mapping of channel to an array of recently broadcast messages
	private HashMap<String, CometMessage[]> buffer = new HashMap<String, CometMessage[]>();
	// Mapping of channel to last broadcast value
	private HashMap<String, Integer> bufferIndex = new HashMap<String, Integer>();

	/**
	 * Attaches a web connection to a channel
	 * @param channel The channel to attach to
	 * @param c The Jetty Continuation of the servlet subscribing
	 * @param lastheard The id of the last message this user received
	 */
	public synchronized void subscribe(String channel, Continuation c, int lastheard) {
		// Create channel if not exists
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

	/**
	 * Send a message to all clients on a channel
	 * @param channel The Channel to broadcast on
	 * @param msg The message to send
	 */
	public synchronized void broadcastMsg(String channel, CometMessage msg) {
		// Create channel if not exists
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

	/**
	 * Safely send the message to a web user
	 * @param c
	 * @param msg
	 */
	private void sendMsg(Continuation c, CometMessage msg) {
		if (c.isSuspended()) {
			c.setAttribute("response", msg);
			c.resume();
		}
	}
}

