package master;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.jetty.websocket.WebSocket.Connection;

/**
 * Allows Master to push events to web clients connected to the WebServer.
 * There is a set of channels, represented by Strings, that can be subscribed to.
 * When a notable event occurs, the Master will broadcast a message on the
 * appropriate channel.  This will send that message to all web clients subscribing to
 * the channel.
 * @author stevearc
 *
 */
public class WebSocketChannelManager {
	private static HashMap<String, Set<Connection>> socketChannels = new HashMap<String, Set<Connection>>();
	private static LinkedList<QueuedMessage> messageQueue = new LinkedList<QueuedMessage>();

	public static void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					sendQueuedMessages();
				}
			}
		}).start();
		startHeartbeatManager();
	}

	private static void startHeartbeatManager() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
					sendHeartbeat();
				}
			}
		}).start();
	}

	public static synchronized void sendQueuedMessages() {
		while (!messageQueue.isEmpty()) {
			QueuedMessage msg = messageQueue.poll();
			Set<Connection> failures = new HashSet<Connection>();
			Set<Connection> socketConnections = socketChannels.get(msg.channel);
			if (socketConnections != null) {
				for (Connection c: socketConnections) {
					try {
						c.sendMessage(msg.message);
					} catch (IOException e) {
						failures.add(c);
					}
				}
				for (Connection c: failures) {
					socketChannels.remove(c);
				}
			}
		}
	}

	public static synchronized void subscribe(String channel, Connection connection) {
		if (!socketChannels.containsKey(channel)) {
			socketChannels.put(channel, new HashSet<Connection>());
		}
		socketChannels.get(channel).add(connection);
	}

	private static synchronized void sendHeartbeat() {
		for (Set<Connection> connections: socketChannels.values()) {
			Set<Connection> failed = new HashSet<Connection>();
			for (Connection c: connections) {
				try {
					c.sendMessage("");
				} catch (IOException e) {
					failed.add(c);
				}
			}
			for (Connection c: failed) {
				connections.remove(c);
			}
		}
	}

	/**
	 * Send a message to all clients on a channel
	 * @param channel The Channel to broadcast on
	 * @param msg The message to send
	 */
	public static synchronized void broadcastMsg(String channel, String cmd, String message) {
		messageQueue.add(new QueuedMessage(channel, cmd + "," + message));
	}

	private static class QueuedMessage {
		public final String channel;
		public final String message;

		private QueuedMessage(String channel, String message) {
			this.channel = channel;
			this.message = message;
		}
	}

	public static synchronized void disconnect(String channel, Connection connection) {
		Set<Connection> socketConnections = socketChannels.get(channel);
		if (socketConnections != null) {
			socketConnections.remove(connection);
		}
	}
}

