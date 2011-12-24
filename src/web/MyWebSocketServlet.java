package web;

import javax.servlet.http.HttpServletRequest;

import master.WebSocketChannelManager;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class MyWebSocketServlet extends WebSocketServlet {
	private static final long serialVersionUID = -1083471361521456794L;
	public static final String NAME = "/socket";

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		String channel = request.getParameter("channel");
		return new ListenerWebSocket(channel);
	}
	
	static class ListenerWebSocket implements WebSocket.OnTextMessage {
		private final String channel;
		private Connection connection;
		
		public ListenerWebSocket(String channel) {
			this.channel = channel;
		}

		@Override
		public void onMessage(String message) {
			
		}

		@Override
		public void onClose(int closeCode, String message) {
			WebSocketChannelManager.disconnect(channel, connection);
		}

		@Override
		public void onOpen(Connection connection) {
			this.connection = connection;
			WebSocketChannelManager.subscribe(channel, connection);
		}
	}

}
